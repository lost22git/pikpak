package lost.pikpak.client.error;

import lost.pikpak.client.http.HttpResponse;

import java.net.URI;

public final class HttpError extends ApiError {

    public final HttpResponse<?, ?, ?> response;
    public final URI uri;

    public HttpError(HttpResponse<?, ?, ?> response) {
        super("http error: uri=%s status=%d\ncontent=%s".formatted(response.request().uri().toString(), response.status(), response.body()));
        this.response = response;
        this.uri = response.request().uri();
    }

    public HttpError(URI uri,
                     Throwable cause) {
        super("http error: uri=%s".formatted(uri.toString()), cause);
        this.uri = uri;
        this.response = null;
    }

    public static HttpError wrap(URI uri,
                                 Throwable cause) {
        return cause instanceof HttpError e ? e : new HttpError(uri, cause);
    }

    public boolean isCaptchaTokenError() {
        return this.response.body().toString().contains("captcha_invalid");
    }

    public boolean isUnAuthError() {
        return this.response.body().toString().contains("unauthenticated");
    }

}
