package lost.pikpak.client.model;

import io.avaje.jsonb.Json;
import lost.pikpak.client.enums.TokenType;

@Json(naming = Json.Naming.LowerUnderscore)
public record RefreshTokenResult(TokenType tokenType,
                                 String accessToken,
                                 String refreshToken,
                                 String sub,
                                 long expiresIn) {

}
