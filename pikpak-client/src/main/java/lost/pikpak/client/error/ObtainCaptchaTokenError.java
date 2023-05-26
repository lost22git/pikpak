package lost.pikpak.client.error;

public final class ObtainCaptchaTokenError extends ApiError {
    public final String action;

    public ObtainCaptchaTokenError(String action) {
        super("action=" + action);
        this.action = action;
    }


    public ObtainCaptchaTokenError(String action,
                                   Throwable cause) {
        super("action=" + action, cause);
        this.action = action;
    }


    public static ObtainCaptchaTokenError wrap(String action,
                                               Exception cause) {
        return (cause instanceof ObtainCaptchaTokenError e)
            ? e
            : new ObtainCaptchaTokenError(action, cause);
    }
}
