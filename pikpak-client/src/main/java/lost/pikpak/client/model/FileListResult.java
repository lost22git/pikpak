package lost.pikpak.client.model;

import io.avaje.jsonb.Json;
import java.time.OffsetDateTime;
import java.util.List;
import lost.pikpak.client.enums.*;

@Json(naming = Json.Naming.LowerUnderscore)
public record FileListResult(
        Kind kind,
        String nextPageToken,
        List<File> files,
        OffsetDateTime syncTime,
        String version,
        boolean versionOutdated) {

    @Json(naming = Json.Naming.LowerUnderscore)
    public record File(
            Kind kind,
            String id,
            String parentId,
            String name,
            String userId,
            String size,
            String revision,
            String fileExtension,
            String mimeType, // TODO enum
            boolean starred,
            String webContentLink,
            OffsetDateTime createdTime,
            OffsetDateTime modifiedTime,
            String iconLink,
            String thumbnailLink,
            String md5Checksum,
            String hash,
            //        Object links, // TODO type?
            Phase phase, // TODO enum
            Audit audit,
            String media,
            boolean trashed,
            OffsetDateTime deleteTime,
            String originalUrl,
            Params params,
            int originalFileIndex,
            String space,
            List apps, // TODO type?
            boolean writable,
            FolderType folderType,
            String collection,
            String sortName,
            OffsetDateTime userModifiedTime,
            List<String> spellName,
            FileCategory fileCategory,
            List<String> tags) {}

    @Json(naming = Json.Naming.LowerUnderscore)
    public record Audit(AuditStatus status, String message, String title) {}

    @Json(naming = Json.Naming.LowerUnderscore)
    public record Params(String duration, String height, String width, String platformIcon, String url) {}
}
