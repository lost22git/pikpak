package lost.pikpak.client.error;

import lost.pikpak.client.Token;

import java.util.Optional;

public final class RefreshTokenError extends ApiError {
    public final String username;
    public final Token.RefreshToken refreshToken;

    public RefreshTokenError(String username,
                             Token.RefreshToken refreshToken) {
        super("username=%s, refreshToken=%s".formatted(
            username,
            Optional.ofNullable(refreshToken).map(Token.RefreshToken::toString).orElse("")));
        this.username = username;
        this.refreshToken = refreshToken;
    }

    public RefreshTokenError(String username,
                             Token.RefreshToken refreshToken,
                             Throwable cause) {
        super("username=%s, refreshToken=%s".formatted(
                username,
                Optional.ofNullable(refreshToken).map(Token.RefreshToken::toString).orElse("")),
            cause);
        this.username = username;
        this.refreshToken = refreshToken;
    }

    public static RefreshTokenError wrap(String username,
                                         Token.RefreshToken refreshToken,
                                         Throwable cause) {
        return (cause instanceof RefreshTokenError e) ? e
            : new RefreshTokenError(username, refreshToken, cause);
    }
}
