package lost.pikpak.client.cmd;

import lost.pikpak.client.context.Context;
import lost.pikpak.client.context.WithContext;
import lost.pikpak.client.enums.FolderType;
import lost.pikpak.client.enums.HttpHeader;
import lost.pikpak.client.enums.Kind;
import lost.pikpak.client.enums.UploadType;
import lost.pikpak.client.error.ApiError;
import lost.pikpak.client.model.FileAddParamBuilder;
import lost.pikpak.client.model.FileAddParamParamsBuilder;
import lost.pikpak.client.model.FileAddParamUrlBuilder;
import lost.pikpak.client.model.FileAddResult;
import lost.pikpak.client.util.Util;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Objects;

public interface FileAddCmd extends Cmd<FileAddResult>, WithContext, ObtainCaptchaToken {

    static FileAddCmd create(Context context) {
        Objects.requireNonNull(context);
        return new Impl(context);
    }

    @Override
    default String action() {
        return "POST:/drive/v1/files";
    }

    FolderType folderType();

    FileAddCmd setFolderType(FolderType folderType);

    Kind kind();

    FileAddCmd setKind(Kind kind);

    String parentId();

    FileAddCmd setParentId(String parentId);

    UploadType uploadType();

    FileAddCmd setUploadType(UploadType uploadType);

    String url();

    FileAddCmd setUrl(String url);


    interface Exec extends CmdExec<FileAddCmd, FileAddResult> {
        static Exec create() {
            return new ExecImpl();
        }
    }

    class Impl implements FileAddCmd {
        private final Context context;
        private final Exec exec;
        private FolderType folderType;
        private Kind kind;
        private String parentId;
        private UploadType uploadType;
        private String url;


        public Impl(Context context) {
            Objects.requireNonNull(context);
            this.context = context;
            this.exec = Exec.create();
        }

        @Override
        public FileAddResult exec() throws ApiError {
            return this.exec.exec(this);
        }

        @Override
        public Context context() {
            return this.context;
        }

        @Override
        public FolderType folderType() {
            return this.folderType;
        }

        @Override
        public FileAddCmd setFolderType(FolderType folderType) {
            this.folderType = folderType;
            return this;
        }

        @Override
        public Kind kind() {
            return this.kind;
        }

        @Override
        public FileAddCmd setKind(Kind kind) {
            this.kind = kind;
            return this;
        }

        @Override
        public String parentId() {
            return this.parentId;
        }

        @Override
        public FileAddCmd setParentId(String parentId) {
            this.parentId = parentId;
            return this;
        }

        @Override
        public UploadType uploadType() {
            return this.uploadType;
        }

        @Override
        public FileAddCmd setUploadType(UploadType uploadType) {
            this.uploadType = uploadType;
            return this;
        }

        @Override
        public String url() {
            return this.url;
        }

        @Override
        public FileAddCmd setUrl(String url) {
            this.url = url;
            return this;
        }

    }

    class ExecImpl implements Exec {

        @Override
        public FileAddResult exec(FileAddCmd cmd) throws ApiError {
            var httpClient = cmd.context().httpClient();

            // Headers
            var headers = httpClient.commonHeaders();
            headers.put(HttpHeader.AUTHORIZATION.getValue(), cmd.context().obtainAccessToken().tokenString());
            headers.put(HttpHeader.CAPTCHA_TOKEN.getValue(), cmd.obtainCaptchaToken().tokenValue());
            // Body
            var param = FileAddParamBuilder.builder()
                .url(FileAddParamUrlBuilder.builder().url(cmd.url()).build())
                .params(FileAddParamParamsBuilder.builder().withThumbnail("true").build())
                .uploadType(cmd.uploadType())
                .parentId(cmd.parentId())
                .kind(cmd.kind())
                .folderType(cmd.folderType())
                .build();

            // Request
            var uri = URI.create("https://api-drive.mypikpak.com/drive/v1/files");
            var request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(Util.jsonBodyPublisher(param));
            headers.forEach(request::setHeader);
            return httpClient.send(request.build(), FileAddResult.class);
        }
    }
}
