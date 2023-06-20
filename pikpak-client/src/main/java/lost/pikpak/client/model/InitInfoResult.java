package lost.pikpak.client.model;

import io.avaje.jsonb.Json;

@Json(naming = Json.Naming.LowerUnderscore)
public record InitInfoResult(String captchaToken, long expiresIn) {}
