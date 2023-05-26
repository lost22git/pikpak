package lost.pikpak.client.http;

import java.net.http.HttpRequest;
import java.util.List;
import java.util.Map;

public record HttpResponse(
    HttpRequest request,
    int status,
    Map<String, List<String>> headers,
    String body
) {
    public HttpResponse {
        // make headers immutable
        headers = headers == null ? Map.of() : Map.copyOf(headers);
        // body default value is ""
        body = body == null ? "" : body;
    }

}
