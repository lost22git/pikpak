package lost.pikpak.client.cmd;

import lost.pikpak.client.Config;
import lost.pikpak.client.Token;
import lost.pikpak.client.TokenAccessTokenBuilder;
import lost.pikpak.client.context.Context;
import lost.pikpak.client.context.WithContext;
import lost.pikpak.client.enums.TokenType;
import lost.pikpak.client.error.ApiError;
import lost.pikpak.client.error.RefreshTokenError;
import lost.pikpak.client.model.RefreshTokenParamBuilder;
import lost.pikpak.client.model.RefreshTokenResult;
import lost.pikpak.client.util.Util;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;

public interface AuthCmd extends Cmd<Void>, WithContext {
    static AuthCmd create(Context context) {
        return new Impl(context);
    }

    interface Exec extends CmdExec<AuthCmd, RefreshTokenResult> {
        static Exec create() {
            return new ExecImpl();
        }
    }

    class Impl implements AuthCmd {
        private final Context context;
        private final Exec exec;

        public Impl(Context context) {
            Objects.requireNonNull(context);
            this.context = context;
            this.exec = Exec.create();
        }

        @Override
        public Void exec() throws RefreshTokenError {
            try {
                var startTime = OffsetDateTime.now();
                var res = this.exec.exec(this);
                updateAccessToken(startTime, res);
                return null;
            } catch (Exception e) {
                throw new RefreshTokenError(this.context.userConfig().username(),
                    Optional.ofNullable(this.context.userConfig().accessToken())
                        .map(Token.AccessToken::refreshToken).orElse(null),
                    e);
            }
        }

        private void updateAccessToken(OffsetDateTime startTime,
                                       RefreshTokenResult res) {
            var refreshToken = new Token.RefreshToken(res.refreshToken(), TokenType.Bearer);
            // 提前 60s 失效
            var expiresAt = startTime.plusSeconds(res.expiresIn()).minusSeconds(60);
            var accessToken = TokenAccessTokenBuilder.builder()
                .tokenValue(res.accessToken())
                .tokenType(TokenType.Bearer)
                .sub(res.sub())
                .expiresAt(expiresAt)
                .refreshToken(refreshToken)
                .build();
            this.context.userConfig().setAccessToken(accessToken);
        }

        @Override
        public Context context() {
            return this.context;
        }
    }

    class ExecImpl implements Exec {

        @Override
        public RefreshTokenResult exec(AuthCmd cmd) throws ApiError {
            var userConfig = cmd.context().userConfig();
            var httpClient = cmd.context().httpClient();

            // Headers
            var headers = httpClient.commonHeaders();
            // Request Body
            var refreshTokenValue = Optional.ofNullable(userConfig.accessToken())
                .map(Token.AccessToken::refreshToken)
                .map(Token.RefreshToken::tokenValue).orElse("");
            var clientId = userConfig.data().extract(Config.Data::clientId).orElse("");
            var param = RefreshTokenParamBuilder.builder()
                .clientId(clientId)
                .grantType("refresh_token")
                .refreshToken(refreshTokenValue)
                .build();

            // Request
            var uri = URI.create("https://user.mypikpak.com/v1/auth/token");
            var request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(Util.jsonBodyPublisher(param));
            headers.forEach(request::setHeader);
            return httpClient.send(request.build(), RefreshTokenResult.class);
        }
    }
}
