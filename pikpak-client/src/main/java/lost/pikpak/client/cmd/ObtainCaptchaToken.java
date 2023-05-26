package lost.pikpak.client.cmd;

import lost.pikpak.client.Token;
import lost.pikpak.client.context.WithContext;
import lost.pikpak.client.error.ObtainCaptchaTokenError;

public interface ObtainCaptchaToken extends WithContext {
    String action();

    default Token.CaptchaToken obtainCaptchaToken() throws ObtainCaptchaTokenError {
        return context().obtainCaptchaToken(action());
    }
}
