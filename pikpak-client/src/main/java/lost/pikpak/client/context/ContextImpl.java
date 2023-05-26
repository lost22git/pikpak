package lost.pikpak.client.context;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import lost.pikpak.client.Config;
import lost.pikpak.client.PikPakClient;
import lost.pikpak.client.Token;
import lost.pikpak.client.cmd.*;
import lost.pikpak.client.http.HttpClient;
import org.checkerframework.checker.index.qual.NonNegative;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ContextImpl implements Context {
    private final PikPakClient pikpak;
    private final Config.User userConfig;

    // TODO lazy
    // TODO close resource
    private final HttpClient httpClient;
    // TODO lazy
    // TODO close resource
    private final Cache<String, Token.CaptchaToken> captchaTokenCache;

    public ContextImpl(PikPakClient pikpak,
                       Config.User userConfig) {
        Objects.requireNonNull(pikpak);
        Objects.requireNonNull(userConfig);
        this.pikpak = pikpak;
        this.userConfig = userConfig;
        this.httpClient = HttpClient.create(this);
        this.captchaTokenCache = Caffeine.newBuilder()
            .expireAfter(new Expiry<String, Token.CaptchaToken>() {
                @Override
                public long expireAfterCreate(String key,
                                              Token.CaptchaToken value,
                                              long currentTime) {
                    return TimeUnit.SECONDS.toNanos(value.expiresAt().toEpochSecond()) - currentTime;
                }

                @Override
                public long expireAfterUpdate(String key,
                                              Token.CaptchaToken value,
                                              long currentTime,
                                              @NonNegative long currentDuration) {
                    return TimeUnit.SECONDS.toNanos(value.expiresAt().toEpochSecond()) - currentTime;
                }

                @Override
                public long expireAfterRead(String key,
                                            Token.CaptchaToken value,
                                            long currentTime,
                                            @NonNegative long currentDuration) {
                    return currentDuration;
                }
            })
            .build();
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
    public Token.CaptchaToken captchaToken(String action) {
        return this.captchaTokenCache.getIfPresent(action);
    }

    @Override
    public Context setCaptchaToken(String action,
                                   Token.CaptchaToken token) {
        this.captchaTokenCache.put(action, token);
        return this;
    }


    @Override
    public InitCmd initCmd(String action) {
        return InitCmd.create(this, action);
    }

    @Override
    public AuthCmd authCmd() {
        return new AuthCmd.Impl(this);
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
