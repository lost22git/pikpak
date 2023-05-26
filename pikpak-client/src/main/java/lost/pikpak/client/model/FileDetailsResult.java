package lost.pikpak.client.model;

import io.avaje.jsonb.Json;
import lost.pikpak.client.enums.*;

import java.time.OffsetDateTime;
import java.util.List;

@Json(naming = Json.Naming.LowerUnderscore)
public record FileDetailsResult(
    Kind kind,
    String id,
    String parentId,
    String name,
    String userId,
    String size,
    String revision,
    String fileExtension,
    String mimeType, //TODO enum
    boolean starred,
    String webContentLink,
    OffsetDateTime createdTime,
    OffsetDateTime modifiedTime,
    String iconLink,
    String thumbnailLink,
    String md5Checksum,
    String hash,
    Links links,
    Phase phase,
    Audit audit,
    List<Media> medias,
    boolean trashed,
    OffsetDateTime deleteTime,
    String originalUrl,
    Params params,
    int originalFileIndex,
    String space,
    List<?> apps, // TODO component type
    boolean writable,
    FolderType folderType,
    String collection,
    String sortName,
    OffsetDateTime userModifiedTime,
    List<String> spellName,
    FileCategory fileCategory,
    List<String> tags
) {

    @Json(naming = Json.Naming.LowerUnderscore)
    public record Links(
        @Json.Property("application/octet-stream")
        OctetStream octetStream
    ) {
    }

    @Json(naming = Json.Naming.LowerUnderscore)
    public record OctetStream(
        String url,
        String token,
        OffsetDateTime expire,
        String type
    ) {
    }

    @Json(naming = Json.Naming.LowerUnderscore)
    public record Audit(
        AuditStatus status, // TODO enum
        String message,
        String title
    ) {

    }

    @Json(naming = Json.Naming.LowerUnderscore)
    public record Media(
        String mediaId,
        String mediaName,
        Video video,
        Link link,
        boolean needMoreQuota,
        List<String> vipTypes,
        String redirectLink,
        String iconLink,
        boolean isDefault,
        int priority,
        boolean isOrigin,
        String resolutionName,
        boolean isVisible,
        String category //TODO enum
    ) {

    }

    @Json(naming = Json.Naming.LowerUnderscore)
    public record Video(
        int height,
        int width,
        long duration,
        long bitRate,
        int frameRate,
        String videoCodec,
        String audioCodec,
        String videoType,
        String hdrType
    ) {

    }

    @Json(naming = Json.Naming.LowerUnderscore)
    public record Link(
        String url,
        String token,
        OffsetDateTime expire,
        String type
    ) {

    }

    @Json(naming = Json.Naming.LowerUnderscore)
    public record Params(
        String duration,
        String height,
        String width,
        String platformIcon,
        String url
    ) {
    }
}
