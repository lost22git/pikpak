package lost.pikpak.client.http;

import lost.pikpak.client.Config;
import lost.pikpak.client.context.Context;
import lost.pikpak.client.context.WithContext;
import lost.pikpak.client.enums.HttpHeader;
import lost.pikpak.client.error.HttpError;
import lost.pikpak.client.error.InvalidCaptchaTokenError;
import lost.pikpak.client.error.UnAuthError;
import lost.pikpak.client.http.HttpResponse.Body;
import lost.pikpak.client.http.HttpResponse.ErrBody;
import lost.pikpak.client.http.HttpResponse.OkBody;
import lost.pikpak.client.http.body.BodyAdapters;
import lost.pikpak.client.util.Util;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodySubscriber;
import java.net.http.HttpResponse.ResponseInfo;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.WARNING;

public interface HttpClient extends WithContext {
    System.Logger LOG = System.getLogger(HttpClient.class.getName());

    static HttpClient create(Context context) {
        var implsClassName = List.of(
            "lost.pikpak.client.reactor.ReactorHttpClient",
            "lost.pikpak.client.helidon.NimaHttpClient"
        );
        for (String className : implsClassName) {
            if (Util.hasClass(className)) {
                try {
                    Class<?> cls = Class.forName(className);
                    return (HttpClient) cls
                        .getConstructor(new Class[]{Context.class})
                        .newInstance(context);
                } catch (Exception e) {
                    if (LOG.isLoggable(WARNING)) {
                        LOG.log(WARNING,
                            "failed to new http client instance," +
                            " className=" + className,
                            e);
                    }
                }
            }
        }

        // default impl
        return new HttpClientImpl(context);
    }


    BodyAdapters bodyAdapters();

    /**
     * SubTypes can implement this method or {@link HttpClient#doSend(HttpRequest, BodyHandler)}
     *
     * @param request the request
     * @param <T>     the type of Response of SubTypes
     * @return response of SubTypes
     * @throws Exception the Exception
     */
    default <T extends Response> T doSend(HttpRequest request) throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * SubTypes can implement this method or {@link HttpClient#doSend(HttpRequest)}
     * <p>
     * NOTE: not recommended to call directly
     * <p>
     * Send request and return response, throw Exception if any exception occurred
     * <p>
     * Better choices:
     * <p>
     * 1) {@link HttpClient#send(HttpRequest, BodyHandler)}
     * <p>
     * 2) {@link HttpClient#send(HttpRequest, Type)}
     *
     * @param request     the request
     * @param bodyHandler the body handler to handle body content
     * @param <T>         the type of body, subtypes of {@link Body}
     * @param <V>         the type of body internal ok value
     * @param <E>         the type of body internal error value
     * @return the response
     * @throws Exception the exception
     */
    default <T extends Body<V, E>, V, E> HttpResponse<T, V, E> doSend(HttpRequest request,
                                                                      BodyHandler<T> bodyHandler) throws Exception {
        try (var res = doSend(request)) {
            var responseInfo = res.responseInfo();
            var resBodySubscriber = bodyHandler.apply(responseInfo);
            var resBodyPublisher = res.bodyPublisher();

            resBodyPublisher.subscribe(resBodySubscriber);
            T resBody = resBodySubscriber.getBody().toCompletableFuture().get();
            return new HttpResponse<>(
                request,
                responseInfo.statusCode(),
                responseInfo.headers().map(),
                resBody
            );
        }
    }

    /**
     * Send request and return ok response,
     * throw {@link HttpError} if we get error response or any exception occurred
     *
     * @param request     the request
     * @param bodyHandler the body handler to handle body content
     * @param <T>         the type of body, subtypes of {@link Body}
     * @param <V>         the type of body internal ok value
     * @param <E>         the type of body internal error value
     * @return the ok response ( {@link HttpResponse} with {@link OkBody} )
     * @throws HttpError the error if we get error response ( {@link HttpResponse} with {@link ErrBody} )
     */
    default <T extends Body<V, E>, V, E> HttpResponse<T, V, E> send(HttpRequest request,
                                                                    BodyHandler<T> bodyHandler) throws HttpError {

        // TODO replace these into interceptors ?

        var uri = request.uri();
        var requestId = Util.genRequestId();

        logRequest(requestId, request);
        try {
            var startTime = System.nanoTime();

            // send request and get response
            var res = this.doSend(request, bodyHandler);

            logResponse(requestId, res, startTime);

            // convert error body response to error and throw it
            if (res.body().isErr()) {
                var e = new HttpError(res);
                if (e.isUnAuthError()) {
                    var username = context().userConfig().username();
                    throw new UnAuthError(username, e);
                } else if (e.isCaptchaTokenError()) {
                    throw new InvalidCaptchaTokenError(e);
                }
                throw e;
            }

            // only return ok body response
            return res;
        } catch (Exception e) {
            throw HttpError.wrap(uri, e);
        }
    }

    /**
     * Send request and return response body,
     * throw {@link HttpError} if we get error response or any exception occurred
     *
     * @param request  the request
     * @param bodyType the type of ok response body
     * @param <T>      the type of ok response body
     * @return ok response body
     * @throws HttpError the error if we get error response ( {@link HttpResponse} with {@link ErrBody} )
     */
    @SuppressWarnings("unchecked")
    default <T> T send(HttpRequest request,
                       Type bodyType) throws HttpError {

        // body handler:
        // 200<= status <300 => read body as OkBody<bodyType>
        // others => read body as ErrBody<String>
        BodyHandler<Body<T, String>> bodyHandler = responseInfo -> {
            int status = responseInfo.statusCode();
            if (200 <= status && status < 300) {
                var ups = (BodySubscriber<T>) bodyAdapters()
                    .json()
                    .reader()
                    .read(bodyType);
                return Body.okBodySubscriber(ups);
            } else {
                var ups = bodyAdapters()
                    .text()
                    .reader()
                    .read(null);
                return Body.errBodySubscriber(ups);
            }
        };

        return (T) send(
            request,
            bodyHandler
        ).body().value();
    }

    default Map<String, String> commonHeaders() {
        var userConfig = context().userConfig();
        var headers = new HashMap<String, String>();
        var d = userConfig.data();
        headers.put(HttpHeader.USER_AGENT.getValue(), d.extract(Config.Data::httpUserAgent).orElse(null));
        headers.put(HttpHeader.REFERER.getValue(), d.extract(Config.Data::httpReferer).map(URI::toString).orElse(null));
        headers.put(HttpHeader.DEVICE_ID.getValue(), d.extract(Config.Data::deviceId).orElse(null));
        headers.put(HttpHeader.DEVICE_NAME.getValue(), d.extract(Config.Data::deviceName).orElse(null));
        headers.put(HttpHeader.DEVICE_MODEL.getValue(), d.extract(Config.Data::deviceModel).orElse(null));
        headers.put(HttpHeader.DEVICE_SIGN.getValue(), d.extract(Config.Data::deviceSign).orElse(null));
        headers.put(HttpHeader.CLIENT_ID.getValue(), d.extract(Config.Data::clientId).orElse(null));
        headers.put(HttpHeader.CLIENT_VERSION.getValue(), d.extract(Config.Data::clientVersion).orElse(null));
        headers.put(HttpHeader.PLATFORM_VERSION.getValue(), d.extract(Config.Data::platformVersion).orElse(null));
        headers.put(HttpHeader.OS_VERSION.getValue(), d.extract(Config.Data::osVersion).orElse(null));
        headers.put(HttpHeader.PROTOCOL_VERSION.getValue(), d.extract(Config.Data::protocolVersion).orElse(null));
        headers.put(HttpHeader.SDK_VERSION.getValue(), d.extract(Config.Data::sdkVersion).orElse(null));
        headers.put(HttpHeader.NETWORK_TYPE.getValue(), d.extract(Config.Data::networkType).orElse(null));
        headers.put(HttpHeader.PROVIDER_NAME.getValue(), d.extract(Config.Data::providerName).orElse(null));

        headers.put(HttpHeader.CONTENT_TYPE.getValue(), "application/json");
        return headers;
    }

    private void logRequest(String requestId,
                            HttpRequest request) {
        if (LOG.isLoggable(DEBUG)) {
            var sb = new StringBuilder();
            sb.append("--> APP HTTP[%s]".formatted(requestId)).append("\n");
            sb.append("--> RequestLine: ").append("\n")
                .append(request.method())
                .append(" ")
                .append(request.uri().toString())
                .append(" ")
                .append(request.version())
                .append("\n");
            sb.append("--> Headers: ").append("\n")
                .append(request.headers().toString())
                .append("\n");
            request.bodyPublisher().ifPresent(p -> {
                var len = p.contentLength();
                sb.append("--> BodyPublisher contentLength: ").append(len).append("\n");
                if (len < 0) {
                    sb.append("""
                        --------------------------------------------------------
                        *NOTE*
                        we got request body `content length < 0`,
                        check your HttpRequest.BodyPublisher.contentLength()
                        if you dont want `Transfer-Encoding: chunked`
                        --------------------------------------------------------
                        """).append("\n");
                }
            });
            sb.append("--> Body: ").append("\n")
                .append(request.bodyPublisher()
                    .map(Util::collectIntoString)
                    .orElse("")
                )
                .append("\n");
            LOG.log(DEBUG, sb.toString());
        }
    }

    private <T extends Body<V, E>, V, E> void logResponse(String requestId,
                                                          HttpResponse<T, V, E> response,
                                                          long startTime) {
        if (LOG.isLoggable(DEBUG)) {
            var elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
            var sb = new StringBuilder();
            sb.append("<-- APP HTTP[%s] (elapsed time: %dms)".formatted(requestId, elapsedMs))
                .append("\n");
            sb.append("<-- StatusLine: ").append("\n")
                .append(response.request().method())
                .append(" ")
                .append(response.request().uri())
                .append(" ")
                .append(response.status())
                .append(" ")
                .append(response.request().version())
                .append("\n");
            sb.append("<-- Headers: ").append("\n")
                .append(response.headers())
                .append("\n");
            sb.append("<-- Body: ").append("\n")
                .append(response.body()) // TODO
                .append("\n");
            LOG.log(DEBUG, sb.toString());
        }
    }

    interface Response extends AutoCloseable {
        InputStream bodyInputStream();

        ResponseInfo responseInfo();

        /**
         * convert body inputStream into {@code Flow.Publisher<List<ByteBuffer>>}
         *
         * @return the publisher
         */
        default Flow.Publisher<List<ByteBuffer>> bodyPublisher() {
            return Util.intoPublisher(bodyInputStream());
        }
    }
}
