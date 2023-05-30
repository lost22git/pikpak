package lost.pikpak.client.token;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import lost.pikpak.client.context.Context;
import lost.pikpak.client.context.WithContext;
import lost.pikpak.client.error.ObtainCaptchaTokenError;
import org.checkerframework.checker.index.qual.NonNegative;

import java.lang.System.Logger.Level;
import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

public interface CaptchaTokenProvider extends WithContext, AutoCloseable {

    System.Logger LOG = System.getLogger(CaptchaTokenProvider.class.getName());

    static CaptchaTokenProvider create(Context context) {
        return new Impl(context);
    }

    Token.CaptchaToken obtainToken(String action) throws ObtainCaptchaTokenError;

    final class Impl implements CaptchaTokenProvider {
        private final Context context;
        private final Cache<String, Token.CaptchaToken> cache;

        private Impl(Context context) {
            this.context = context;
            this.cache = Caffeine.newBuilder()
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
        public Context context() {
            return this.context;
        }

        @Override
        public Token.CaptchaToken obtainToken(String action) throws ObtainCaptchaTokenError {
            try {
                var token = this.cache.getIfPresent(action);
                if (token == null || token.isExpiredNow()) {
                    LOG.log(Level.INFO,
                        "obtain token from cache was null or expired, try to obtain it from remote now, action={0}",
                        action);
                    var startTime = OffsetDateTime.now();
                    var initInfoResult = this.context.initCmd(action).exec();
                    // TODO 更新 captchaToken 操作是否放在 InitInfo.get(...) 内部 ?
                    // 提前 60s 失效
                    var expiresAt = startTime.plusSeconds(initInfoResult.expiresIn()).minusSeconds(60);
                    var newToken = new Token.CaptchaToken(initInfoResult.captchaToken(), expiresAt);
                    this.cache.put(action, newToken);
                    return newToken;
                } else {
                    LOG.log(Level.DEBUG, "obtain token from cache, action={0}, token={1}", action, token);
                    return token;
                }
            } catch (Exception e) {
                throw ObtainCaptchaTokenError.wrap(action, e);
            }
        }

        @Override
        public void close() throws Exception {
            this.cache.cleanUp();
        }
    }
}
