package lost.pikpak.client.token;

import lost.pikpak.client.context.Context;
import lost.pikpak.client.context.WithContext;
import lost.pikpak.client.error.RefreshTokenError;

import java.util.Optional;

public interface AccessTokenProvider extends WithContext {
    static AccessTokenProvider create(Context context) {
        return new Impl(context);
    }

    Token.AccessToken obtainToken() throws RefreshTokenError;

    class Impl implements AccessTokenProvider {
        private final Context context;

        public Impl(Context context) {
            this.context = context;
        }

        @Override
        public Context context() {
            return this.context;
        }

        @Override
        public Token.AccessToken obtainToken() throws RefreshTokenError {
            var userConfig = this.context.userConfig();
            try {
                var token = userConfig.accessToken();
                if (token == null) {
                    throw new RefreshTokenError(userConfig.username(), null);
                }
                if (token.isExpiredNow()) {
                    this.context.authCmd().exec();
                    return userConfig.accessToken();
                } else {
                    return token;
                }
            } catch (Exception e) {
                throw RefreshTokenError.wrap(userConfig.username(),
                    Optional.ofNullable(userConfig.accessToken())
                        .map(Token.AccessToken::refreshToken).orElse(null)
                    , e);
            }
        }
    }
}
