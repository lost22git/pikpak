package lost.pikpak.client.context;

import lost.pikpak.client.Config;
import lost.pikpak.client.PikPakClient;
import lost.pikpak.client.cmd.*;
import lost.pikpak.client.http.HttpClient;
import lost.pikpak.client.token.AccessTokenProvider;
import lost.pikpak.client.token.CaptchaTokenProvider;

public interface Context {
    static ContextImpl create(PikPakClient pikpak,
                              Config.User userConfig) {
        return new ContextImpl(pikpak, userConfig);
    }

    PikPakClient pikpak();

    Config.User userConfig();

    HttpClient httpClient();

    AccessTokenProvider accessTokenProvider();

    CaptchaTokenProvider captchaTokenProvider();

    // -------------------------------- Cmd

    InitCmd initCmd(String action);

    AuthCmd authCmd();

    SignInCmd signInCmd();

    FileAddCmd fileAddCmd();

    FileListCmd fileListCmd();

    FileDetailsCmd fileDetailsCmd(String fileId);

}
