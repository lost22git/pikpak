package lost.pikpak.client.error;

public sealed class ApiError extends Exception
        permits HttpError,
                InvalidCaptchaTokenError,
                ObtainCaptchaTokenError,
                RefreshTokenError,
                SignInError,
                UnAuthError {
    public ApiError() {
        super();
    }

    public ApiError(String message) {
        super(message);
    }

    public ApiError(Throwable cause) {
        super(cause);
    }

    public ApiError(String message, Throwable cause) {
        super(message, cause);
    }
}
