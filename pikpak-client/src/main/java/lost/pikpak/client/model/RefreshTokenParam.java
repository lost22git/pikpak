package lost.pikpak.client.model;

import io.avaje.jsonb.Json;
import io.soabase.recordbuilder.core.RecordBuilderFull;

@RecordBuilderFull
@Json(naming = Json.Naming.LowerUnderscore)
public record RefreshTokenParam(String clientId, String grantType, String refreshToken) {}
