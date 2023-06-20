package lost.pikpak.client.model;

import io.avaje.jsonb.Json;
import lost.pikpak.client.enums.TokenType;

@Json(naming = Json.Naming.LowerUnderscore)
public record SignInResult(TokenType tokenType, String accessToken, String refreshToken, long expiresIn, String sub) {}
