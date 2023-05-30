package lost.pikpak.client.cmd;

import lost.pikpak.client.Config;
import lost.pikpak.client.context.Context;
import lost.pikpak.client.context.WithContext;
import lost.pikpak.client.enums.HttpHeader;
import lost.pikpak.client.enums.TokenType;
import lost.pikpak.client.error.ApiError;
import lost.pikpak.client.error.SignInError;
import lost.pikpak.client.model.SignInParamBuilder;
import lost.pikpak.client.model.SignInResult;
import lost.pikpak.client.token.RequireCaptchaToken;
import lost.pikpak.client.token.Token;
import lost.pikpak.client.token.TokenAccessTokenBuilder;
import lost.pikpak.client.util.Util;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.OffsetDateTime;
import java.util.Objects;

public interface SignInCmd extends Cmd<Void>, WithContext, RequireCaptchaToken {

    static SignInCmd create(Context context) {
        return new Impl(context);
    }

    @Override
    default String action() {
        return "POST:/v1/auth/signin";
    }

    interface Exec extends CmdExec<SignInCmd, SignInResult> {
        static Exec create() {
            return new ExecImpl();
        }
    }

    final class Impl implements SignInCmd {
        private final Context context;
        private final Exec exec;

        private Impl(Context context) {
            Objects.requireNonNull(context);
            this.context = context;
            this.exec = Exec.create();
        }

        @Override
        public Void exec() throws SignInError {
            try {
                var startTime = OffsetDateTime.now();
                var res = exec.exec(this);
                updateAccessToken(startTime, res);
                return null;
            } catch (Exception e) {
                throw new SignInError(this.context.userConfig().username(), e);
            }
        }

        private void updateAccessToken(
            OffsetDateTime startTime,
            SignInResult res) {
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

    final class ExecImpl implements SignInCmd.Exec {
        private ExecImpl() {
        }

        @Override
        public SignInResult exec(SignInCmd cmd) throws ApiError {
            var userConfig = cmd.context().userConfig();
            var httpClient = cmd.context().httpClient();

            // Headers
            var headers = httpClient.commonHeaders();
            headers.put(HttpHeader.CAPTCHA_TOKEN.getValue(), cmd.requireCaptchaToken().tokenValue());
            // Body
            var username = userConfig.username();
            var passwd = userConfig.passwd();
            var clientId = userConfig.data().extract(Config.Data::clientId).orElse("");
            var param = SignInParamBuilder.builder()
                .username(username)
                .password(passwd)
                .clientId(clientId)
                .build();

            // Request
            var uri = URI.create("https://user.mypikpak.com/v1/auth/signin");
            var request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(Util.jsonBodyPublisher(param));
            headers.forEach(request::setHeader);
            return httpClient.send(request.build(), SignInResult.class);
        }

    }
}
