package lost.pikpak.client.context;

import java.util.Objects;
import lost.pikpak.client.Config;
import lost.pikpak.client.PikPakClient;
import lost.pikpak.client.cmd.*;
import lost.pikpak.client.http.HttpClient;
import lost.pikpak.client.token.AccessTokenProvider;
import lost.pikpak.client.token.CaptchaTokenProvider;

final class ContextImpl implements Context {
    private final PikPakClient pikpak;
    private final Config.User userConfig;

    // TODO lazy
    // TODO close resource
    private final HttpClient httpClient;
    private final AccessTokenProvider accessTokenProvider;
    // TODO close resource
    private final CaptchaTokenProvider captchaTokenProvider;

    public ContextImpl(PikPakClient pikpak, Config.User userConfig) {
        Objects.requireNonNull(pikpak);
        Objects.requireNonNull(userConfig);
        this.pikpak = pikpak;
        this.userConfig = userConfig;
        this.httpClient = HttpClient.create(this);
        this.accessTokenProvider = AccessTokenProvider.create(this);
        this.captchaTokenProvider = CaptchaTokenProvider.create(this);
    }

    @Override
    public PikPakClient pikpak() {
        return this.pikpak;
    }

    @Override
    public Config.User userConfig() {
        return this.userConfig;
    }

    @Override
    public HttpClient httpClient() {
        return this.httpClient;
    }

    @Override
    public AccessTokenProvider accessTokenProvider() {
        return this.accessTokenProvider;
    }

    @Override
    public CaptchaTokenProvider captchaTokenProvider() {
        return this.captchaTokenProvider;
    }

    @Override
    public InitCmd initCmd(String action) {
        return InitCmd.create(this, action);
    }

    @Override
    public AuthCmd authCmd() {
        return AuthCmd.create(this);
    }

    @Override
    public SignInCmd signInCmd() {
        return SignInCmd.create(this);
    }

    @Override
    public FileAddCmd fileAddCmd() {
        return FileAddCmd.create(this);
    }

    @Override
    public FileListCmd fileListCmd() {
        return FileListCmd.create(this);
    }

    @Override
    public FileDetailsCmd fileDetailsCmd(String fileId) {
        return FileDetailsCmd.create(this, fileId);
    }
}
