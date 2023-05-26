package lost.pikpak.client.model;

import io.avaje.jsonb.Json;
import lost.pikpak.client.enums.Kind;
import lost.pikpak.client.enums.Phase;
import lost.pikpak.client.enums.UploadType;

import java.time.OffsetDateTime;
import java.util.List;

@Json(naming = Json.Naming.LowerUnderscore)
public record FileAddResult(
    UploadType uploadType,
    Url url,
//    Object file  // TODO type ?
    Task task
) {

    @Json(naming = Json.Naming.LowerUnderscore)
    public record Url(
        Kind kind) {
    }

    @Json(naming = Json.Naming.LowerUnderscore)
    public record Task(
        Kind kind,
        String id,
        String name,
        String type, // TODO enum
        String userId,
        List<String> statuses, // component enum
        long statusSize,
        Params params,
        String fileId,
        String fileName,
        String fileSize,
        String message,
        OffsetDateTime createdTime,
        OffsetDateTime updatedTime,
        String thirdTaskId,
        Phase phase,
        int progress,
        String iconLink,
        String callback,
//        Object referenceResource, // TODO type ?
        String space
    ) {
    }

    @Json(naming = Json.Naming.LowerUnderscore)
    public record Params(
        String predictSpeed,
        String predictType, //TODO enum
        String thumbnailLink
    ) {

    }
}
