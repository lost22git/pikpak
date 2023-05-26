package lost.pikpak.client.http;

import lost.pikpak.client.Config;
import lost.pikpak.client.context.Context;
import lost.pikpak.client.context.WithContext;
import lost.pikpak.client.enums.HttpHeader;
import lost.pikpak.client.error.ApiError;
import lost.pikpak.client.error.HttpError;
import lost.pikpak.client.error.InvalidCaptchaTokenError;
import lost.pikpak.client.error.UnAuthError;
import lost.pikpak.client.util.Util;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.Logger.Level.DEBUG;

public interface HttpClient extends WithContext {
    System.Logger LOG = System.getLogger(HttpClient.class.getName());

    static HttpClient create(Context context) {
        return new HttpClientImpl(context);
    }

    HttpResponse doSend(HttpRequest request) throws Exception;

    default String send(HttpRequest request) throws HttpError {

        var uri = request.uri();
        var requestId = Util.genRequestId();
        logRequest(requestId, request);
        try {
            var res = doSend(request);
            logResponse(requestId, res);
            return bodyOrThrow(res);
        } catch (Exception e) {
            throw HttpError.wrap(uri, e);
        }
    }

    default <T> T send(HttpRequest request,
                       Type bodyType) throws HttpError {
        var bodyJson = send(request);
        // replace "" to null
        bodyJson = bodyJson.replace("\"\"", "null");
        return Util.fromJson(bodyJson, bodyType);
    }

    private String bodyOrThrow(HttpResponse response) throws ApiError {
        var status = response.status();
        if (200 <= status && status < 300) {
            return response.body();
        } else {
            var e = new HttpError(response);
            if (e.isUnAuthError()) {
                throw new UnAuthError(context().userConfig().username(), e);
            } else if (e.isCaptchaTokenError()) {
                throw new InvalidCaptchaTokenError(e);
            }
            throw e;
        }
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

    private void logResponse(String requestId,
                             HttpResponse response) {
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
