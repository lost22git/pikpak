package lost.pikpak.client.cmd;

import lost.pikpak.client.context.Context;
import lost.pikpak.client.context.WithContext;
import lost.pikpak.client.enums.HttpHeader;
import lost.pikpak.client.error.ApiError;
import lost.pikpak.client.model.FileDetailsResult;
import lost.pikpak.client.token.RequireAccessToken;
import lost.pikpak.client.token.RequireCaptchaToken;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Objects;

public interface FileDetailsCmd extends Cmd<FileDetailsResult>, WithContext, RequireCaptchaToken, RequireAccessToken {

    static FileDetailsCmd create(Context context,
                                 String fileId) {
        return new Impl(context, fileId);
    }

    String fileId();

    FileDetailsCmd setFileId(String fileId);

    default String action() {
        return "GET:/drive/v1/files/%s".formatted(fileId());
    }

    interface Exec extends CmdExec<FileDetailsCmd, FileDetailsResult> {
        static Exec create() {
            return new ExecImpl();
        }
    }

    class Impl implements FileDetailsCmd {
        private final Context context;
        private final Exec exec;

        private String fileId;

        public Impl(Context context,
                    String fileId) {
            Objects.requireNonNull(context);
            Objects.requireNonNull(fileId);
            this.context = context;
            this.fileId = fileId;
            this.exec = Exec.create();
        }

        @Override
        public FileDetailsResult exec() throws ApiError {
            return this.exec.exec(this);
        }

        @Override
        public String fileId() {
            return this.fileId;
        }

        @Override
        public FileDetailsCmd setFileId(String fileId) {
            this.fileId = fileId;
            return this;
        }

        @Override
        public Context context() {
            return this.context;
        }
    }

    class ExecImpl implements Exec {

        @Override
        public FileDetailsResult exec(FileDetailsCmd cmd) throws ApiError {
            var httpClient = cmd.context().httpClient();

            // Headers
            var headers = httpClient.commonHeaders();
            headers.put(HttpHeader.AUTHORIZATION.getValue(), cmd.requireAccessToken().tokenString());
            headers.put(HttpHeader.CAPTCHA_TOKEN.getValue(), cmd.requireCaptchaToken().tokenValue());

            // Request
            var uri = URI.create("https://api-drive.mypikpak.com/drive/v1/files/%s".formatted(cmd.fileId()));
            var request = HttpRequest.newBuilder()
                .uri(uri)
                .GET();
            headers.forEach(request::setHeader);
            return httpClient.send(request.build(), FileDetailsResult.class);
        }
    }
}
