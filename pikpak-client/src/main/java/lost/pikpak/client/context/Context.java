package lost.pikpak.client.context;

import lost.pikpak.client.Config;
import lost.pikpak.client.PikPakClient;
import lost.pikpak.client.Token;
import lost.pikpak.client.cmd.*;
import lost.pikpak.client.error.ObtainCaptchaTokenError;
import lost.pikpak.client.error.RefreshTokenError;
import lost.pikpak.client.http.HttpClient;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface Context {
    static ContextImpl create(PikPakClient pikpak,
                              Config.User userConfig) {
        return new ContextImpl(pikpak, userConfig);
    }

    PikPakClient pikpak();

    Config.User userConfig();

    HttpClient httpClient();

    Token.CaptchaToken captchaToken(String action);

    @SuppressWarnings("UnusedReturnValue")
    Context setCaptchaToken(String action,
                            Token.CaptchaToken token);

    default Token.CaptchaToken obtainCaptchaToken(String action) throws ObtainCaptchaTokenError {
        try {
            var token = captchaToken(action);
            if (token == null || token.isExpiredNow()) {
                var startTime = OffsetDateTime.now();
                var initInfoResult = initCmd(action).exec();
                // TODO 更新 captchaToken 操作是否放在 InitInfo.get(...) 内部 ?
                // 提前 60s 失效
                var expiresAt = startTime.plusSeconds(initInfoResult.expiresIn()).minusSeconds(60);
                var newToken = new Token.CaptchaToken(initInfoResult.captchaToken(), expiresAt);
                setCaptchaToken(action, newToken);
                return newToken;
            } else {
                return token;
            }
        } catch (Exception e) {
            throw ObtainCaptchaTokenError.wrap(action, e);
        }
    }

    default Token.AccessToken obtainAccessToken() throws RefreshTokenError {
        try {
            var token = userConfig().accessToken();
            if (token == null) {
                throw new RefreshTokenError(userConfig().username(), null);
            }
            if (token.isExpiredNow()) {
                authCmd().exec();
                return userConfig().accessToken();
            } else {
                return token;
            }
        } catch (Exception e) {
            throw RefreshTokenError.wrap(userConfig().username(),
                Optional.ofNullable(userConfig().accessToken())
                    .map(Token.AccessToken::refreshToken).orElse(null)
                , e);
        }
    }

    // -------------------------------- Cmd

    InitCmd initCmd(String action);

    AuthCmd authCmd();

    SignInCmd signInCmd();

    FileAddCmd fileAddCmd();

    FileListCmd fileListCmd();

    FileDetailsCmd fileDetailsCmd(String fileId);

}
