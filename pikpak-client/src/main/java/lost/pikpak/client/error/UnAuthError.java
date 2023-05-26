package lost.pikpak.client.error;

public final class UnAuthError extends ApiError {
    public final String username;

    public UnAuthError(String username) {
        super("username=" + username);
        this.username = username;
    }

    public UnAuthError(String username,
                       Throwable cause) {
        super("username=" + username, cause);
        this.username = username;
    }
}
