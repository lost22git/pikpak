package lost.pikpak.client.model;

import io.avaje.jsonb.Json;
import io.soabase.recordbuilder.core.RecordBuilderFull;
import lost.pikpak.client.enums.FolderType;
import lost.pikpak.client.enums.Kind;
import lost.pikpak.client.enums.UploadType;

@RecordBuilderFull
@Json(naming = Json.Naming.LowerUnderscore)
public record FileAddParam(
        FolderType folderType, Kind kind, String parentId, UploadType uploadType, Url url, Params params) {

    @RecordBuilderFull
    @Json(naming = Json.Naming.LowerUnderscore)
    public record Url(String url) {}

    @RecordBuilderFull
    @Json(naming = Json.Naming.LowerUnderscore)
    public record Params(String withThumbnail) {}
}
