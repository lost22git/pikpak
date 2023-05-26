package lost.pikpak.client.token;

import io.avaje.jsonb.Json;
import io.soabase.recordbuilder.core.RecordBuilderFull;
import lost.pikpak.client.enums.TokenType;

import java.time.OffsetDateTime;

public interface Token {
    String tokenValue();

    TokenType tokenType();

    boolean isExpired(OffsetDateTime time);

    default boolean isExpiredNow() {
        return isExpired(OffsetDateTime.now());
    }

    default String tokenString() {
        var value = tokenValue();
        if (value != null && !value.trim().isBlank()) {
            var type = tokenType();
            var tt = type == null ? "" : type.name() + " ";
            return tt + value;
        } else {
            return "";
        }
    }


    @RecordBuilderFull
    @Json(naming = Json.Naming.LowerUnderscore)
    record AccessToken(
        RefreshToken refreshToken,
        String tokenValue,
        TokenType tokenType,
        String sub,
        OffsetDateTime expiresAt
    ) implements Token {

        @Override
        public boolean isExpired(OffsetDateTime time) {
            // return true if time >= this.expiresAt
            return !this.expiresAt.isAfter(time);
        }
    }

    @RecordBuilderFull

    @Json(naming = Json.Naming.LowerUnderscore)
    record RefreshToken(
        String tokenValue,
        TokenType tokenType) implements Token {

        @Override
        public boolean isExpired(OffsetDateTime time) {
            return false;
        }
    }

    @RecordBuilderFull
    record CaptchaToken(
        String tokenValue,
        OffsetDateTime expiresAt

    ) implements Token {

        public static CaptchaToken createExpired() {
            return new CaptchaToken("", OffsetDateTime.MIN);
        }

        @Override
        public TokenType tokenType() {
            // TODO return null really ?
            return null;
        }

        @Override
        public boolean isExpired(OffsetDateTime time) {
            // return true if time >= this.expiresAt
            return !this.expiresAt.isAfter(time);
        }
    }

}
