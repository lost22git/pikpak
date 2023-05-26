package lost.pikpak.client.token;

import lost.pikpak.client.context.WithContext;
import lost.pikpak.client.error.ObtainCaptchaTokenError;

public interface RequireCaptchaToken extends WithContext {
    String action();

    default Token.CaptchaToken requireCaptchaToken() throws ObtainCaptchaTokenError {
        return context().captchaTokenProvider().obtainToken(action());
    }
}
