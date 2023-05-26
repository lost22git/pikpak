package lost.pikpak.client.model;

import io.avaje.jsonb.Json;
import io.soabase.recordbuilder.core.RecordBuilderFull;

@RecordBuilderFull
@Json(naming = Json.Naming.LowerUnderscore)
public record SignInParam(String username,
                          String password,
                          String clientId) {

}
