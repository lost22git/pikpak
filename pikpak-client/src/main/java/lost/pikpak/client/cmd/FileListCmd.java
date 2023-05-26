package lost.pikpak.client.cmd;

import lost.pikpak.client.context.Context;
import lost.pikpak.client.context.WithContext;
import lost.pikpak.client.enums.HttpHeader;
import lost.pikpak.client.enums.SortOrder;
import lost.pikpak.client.enums.ThumbnailSize;
import lost.pikpak.client.error.ApiError;
import lost.pikpak.client.model.FileListParamBuilder;
import lost.pikpak.client.model.FileListResult;
import lost.pikpak.client.util.Util;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public interface FileListCmd extends Cmd<FileListResult>, WithContext, ObtainCaptchaToken {
    static FileListCmd create(Context context) {
        return new Impl(context);
    }

    @Override
    default String action() {
        return "GET:/drive/v1/files";
    }

    ThumbnailSize thumbnailSize();

    FileListCmd setThumbnailSize(ThumbnailSize thumbnailSize);

    int limit();

    FileListCmd setLimit(int limit);

    String pageToken();

    FileListCmd setPageToken(String pageToken);

    String parentId();

    FileListCmd setParentId(String parentId);

    boolean withAudit();

    FileListCmd setWithAudit(boolean withAudit);

    SortOrder sortOrder();

    FileListCmd setSortOrder(SortOrder sortOrder);

    interface Exec extends CmdExec<FileListCmd, FileListResult> {
        static Exec create() {
            return new ExecImpl();
        }
    }

    class Impl implements FileListCmd {
        private final Context context;
        private final Exec exec;

        private ThumbnailSize thumbnailSize;
        private int limit;
        private String pageToken;
        private String parentId;
        private boolean withAudit;
        private SortOrder sortOrder;

        public Impl(Context context) {
            Objects.requireNonNull(context);
            this.context = context;
            this.exec = Exec.create();
        }

        @Override
        public FileListResult exec() throws ApiError {
            return this.exec.exec(this);
        }

        @Override
        public Context context() {
            return this.context;
        }

        @Override
        public ThumbnailSize thumbnailSize() {
            return this.thumbnailSize;
        }

        @Override
        public FileListCmd setThumbnailSize(ThumbnailSize thumbnailSize) {
            this.thumbnailSize = thumbnailSize;
            return this;
        }

        @Override
        public int limit() {
            return this.limit;
        }

        @Override
        public FileListCmd setLimit(int limit) {
            this.limit = limit;
            return this;
        }

        @Override
        public String pageToken() {
            return this.pageToken;
        }

        @Override
        public FileListCmd setPageToken(String pageToken) {
            this.pageToken = pageToken;
            return this;
        }

        @Override
        public String parentId() {
            return this.pageToken;
        }

        @Override
        public FileListCmd setParentId(String parentId) {
            this.parentId = parentId;
            return this;
        }

        @Override
        public boolean withAudit() {
            return this.withAudit;
        }

        @Override
        public FileListCmd setWithAudit(boolean withAudit) {
            this.withAudit = withAudit;
            return this;
        }

        @Override
        public SortOrder sortOrder() {
            return this.sortOrder;
        }

        @Override
        public FileListCmd setSortOrder(SortOrder sortOrder) {
            this.sortOrder = sortOrder;
            return this;
        }
    }

    class ExecImpl implements Exec {

        @Override
        public FileListResult exec(FileListCmd cmd) throws ApiError {
            var httpClient = cmd.context().httpClient();

            // Headers
            var headers = httpClient.commonHeaders();
            headers.put(HttpHeader.AUTHORIZATION.getValue(), cmd.context().obtainAccessToken().tokenString());
            headers.put(HttpHeader.CAPTCHA_TOKEN.getValue(), cmd.obtainCaptchaToken().tokenValue());
            // QueryParams
            var param = FileListParamBuilder.builder()
                .limit(cmd.limit())
                .pageToken(cmd.pageToken())
                .parentId(cmd.parentId())
                .sortOrder(cmd.sortOrder())
                .thumbnailSize(cmd.thumbnailSize())
                .withAudit(cmd.withAudit())
                .build();

            var filterParam = """
                {
                   // "kind": {
                   //     "eq": "drive#file"
                   // }
                    "trashed": {
                        "eq": false
                    },
                    "phase": {
                        "eq": "PHASE_TYPE_COMPLETE"
                    }
                }
                """;
            var filterParamCompact = Util.compactJson(filterParam);
            var filterParamEncoded = URLEncoder.encode(filterParamCompact, StandardCharsets.UTF_8);

            // Request
            var uri = URI.create("https://api-drive.mypikpak.com/drive/v1/files?%s&filters=%s"
                .formatted(param.asQueryString(), filterParamEncoded));
            var requestBuilder = HttpRequest.newBuilder()
                .uri(uri)
                .GET();
            headers.forEach(requestBuilder::setHeader);
            var request = requestBuilder.build();
            return httpClient.send(request, FileListResult.class);
        }
    }
}
