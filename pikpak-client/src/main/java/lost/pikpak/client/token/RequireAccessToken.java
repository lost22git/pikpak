package lost.pikpak.client.token;

import lost.pikpak.client.context.WithContext;
import lost.pikpak.client.error.RefreshTokenError;

public interface RequireAccessToken extends WithContext {
    default Token.AccessToken requireAccessToken() throws RefreshTokenError {
        return context().accessTokenProvider().obtainToken();
    }
}
