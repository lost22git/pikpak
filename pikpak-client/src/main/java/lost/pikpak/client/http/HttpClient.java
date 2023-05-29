package lost.pikpak.client.http;

import lost.pikpak.client.Config;
import lost.pikpak.client.context.Context;
import lost.pikpak.client.context.WithContext;
import lost.pikpak.client.enums.HttpHeader;
import lost.pikpak.client.error.HttpError;
import lost.pikpak.client.error.InvalidCaptchaTokenError;
import lost.pikpak.client.error.UnAuthError;
import lost.pikpak.client.util.Util;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.Logger.Level.DEBUG;

public interface HttpClient extends WithContext {
    System.Logger LOG = System.getLogger(HttpClient.class.getName());

    static HttpClient create(Context context) {
        return new HttpClientImpl(context);
    }

    /**
     * NOTE: not recommended to call directly
     * <p>
     * Send request and return response, throw Exception if any exception occurred
     * <p>
     * Better choices:
     * <p>
     * 1) {@link HttpClient#send(HttpRequest, java.net.http.HttpResponse.BodyHandler)}
     * <p>
     * 2) {@link HttpClient#send(HttpRequest, Type)}
     *
     * @param request     the request
     * @param bodyHandler the body handler to handle body content
     * @param <T>         the type of body, subtypes of {@link lost.pikpak.client.http.HttpResponse.Body}
     * @param <V>         the type of body internal ok value
     * @param <E>         the type of body internal error value
     * @return the response
     * @throws Exception the exception
     */
    <T extends HttpResponse.Body<V, E>, V, E> HttpResponse<T, V, E> doSend(HttpRequest request,
                                                                           java.net.http.HttpResponse.BodyHandler<T> bodyHandler) throws Exception;

    /**
     * Send request and return ok response, throw {@link HttpError} if we get error response or any exception occurred
     *
     * @param request     the request
     * @param bodyHandler the body handler to handle body content
     * @param <T>         the type of body, subtypes of {@link lost.pikpak.client.http.HttpResponse.Body}
     * @param <V>         the type of body internal ok value
     * @param <E>         the type of body internal error value
     * @return the ok response ( {@link HttpResponse} with {@link lost.pikpak.client.http.HttpResponse.OkBody} )
     * @throws HttpError the error if we get error response ( {@link HttpResponse} with {@link lost.pikpak.client.http.HttpResponse.ErrBody} )
     */
    default <T extends HttpResponse.Body<V, E>, V, E> HttpResponse<T, V, E> send(HttpRequest request,
                                                                                 java.net.http.HttpResponse.BodyHandler<T> bodyHandler) throws HttpError {

        // TODO replace these into interceptors ?

        var uri = request.uri();
        var requestId = Util.genRequestId();
        logRequest(requestId, request);
        try {
            var res = this.doSend(request, bodyHandler);
            logResponse(requestId, res);
            // throw http error if body is ErrBody
            if (res.body().isErr()) {
                var e = new HttpError(res);
                if (e.isUnAuthError()) {
                    throw new UnAuthError(context().userConfig().username(), e);
                } else if (e.isCaptchaTokenError()) {
                    throw new InvalidCaptchaTokenError(e);
                }
                throw e;
            }
            return res;
        } catch (Exception e) {
            throw HttpError.wrap(uri, e);
        }
    }

    /**
     * Send request and return response body, throw {@link HttpError} if we get error response or any exception occurred
     *
     * @param request  the request
     * @param bodyType the type of ok response body
     * @param <T>      the type of ok response body
     * @return ok response body
     * @throws HttpError the error if we get error response ( {@link HttpResponse} with {@link lost.pikpak.client.http.HttpResponse.ErrBody} )
     */
    @SuppressWarnings("unchecked")
    default <T> T send(HttpRequest request,
                       Type bodyType) throws HttpError {
        var res = send(request, responseInfo ->
        {
            int status = responseInfo.statusCode();
            if (200 <= status && status < 300) {
                var ups = (java.net.http.HttpResponse.BodySubscriber<T>) Util.jsonBodyHandle(bodyType).apply(responseInfo);
                return HttpResponse.Body.okBodySubscriber(ups);
            } else {
                var ups = java.net.http.HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8);
                return HttpResponse.Body.errBodySubscriber(ups);
            }
        });
        return res.body().value();
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
            sb.append("HTTP[%s]".formatted(requestId));
            sb.append("\n");
            sb.append(">> ");
            sb.append(request.method());
            sb.append(" ");
            sb.append(request.uri().toString());
            sb.append("\n");
            sb.append("Headers: ");
            sb.append(request.headers().toString());
            sb.append("\n");
            sb.append("Body: ");
            sb.append(request.bodyPublisher().map(Util::collectIntoString).orElse(""));
            LOG.log(DEBUG, sb.toString());
        }
    }

    private <T extends HttpResponse.Body<V, E>, V, E> void logResponse(String requestId,
                                                                       HttpResponse<T, V, E> response) {
        if (LOG.isLoggable(DEBUG)) {
            var sb = new StringBuilder();
            sb.append("HTTP[%s]".formatted(requestId));
            sb.append("\n");
            sb.append("<< ");
            sb.append(response.request().method());
            sb.append(" ");
            sb.append(response.request().uri());
            sb.append(" ");
            sb.append(response.status());
            sb.append("\n");
            sb.append("Headers: ");
            sb.append(response.headers().toString());
            sb.append("\n");
            sb.append("Body: ");
            sb.append(response.body());
            LOG.log(DEBUG, sb.toString());
        }
    }


}
