package lost.pikpak.client.error;

public final class SignInError extends ApiError {
    public final String username;

    public SignInError(String username) {
        super("username=" + username);
        this.username = username;
    }

    public SignInError(String username,
                       Throwable cause) {
        super("username=" + username, cause);
        this.username = username;
    }

}
