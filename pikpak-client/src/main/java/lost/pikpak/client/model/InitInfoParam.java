package lost.pikpak.client.model;

import io.avaje.jsonb.Json;
import io.soabase.recordbuilder.core.RecordBuilderFull;

@RecordBuilderFull
@Json(naming = Json.Naming.LowerUnderscore)
public record InitInfoParam(String action, String clientId, String deviceId, String captchaToken, MetaParam meta) {
    @RecordBuilderFull
    @Json(naming = Json.Naming.LowerUnderscore)
    public record MetaParam(
            String email,
            String captchaSign,
            String clientVersion,
            String packageName,
            String userId,
            String timestamp) {}
}
