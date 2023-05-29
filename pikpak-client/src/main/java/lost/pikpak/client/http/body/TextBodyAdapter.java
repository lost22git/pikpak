package lost.pikpak.client.http.body;

import java.lang.reflect.Type;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class TextBodyAdapter implements BodyAdapter<String> {
    public static TextBodyAdapter create() {
        return new TextBodyAdapter();
    }

    @Override
    public BodyReader<String> reader() {
        return new Reader();
    }

    @Override
    public BodyWriter<String> writer() {
        return new Writer();
    }

    private static class Reader implements BodyReader<String> {
        @Override
        public HttpResponse.BodySubscriber<String> read(Type type) {
            return HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8);
        }
    }

    private static class Writer implements BodyWriter<String> {
        @Override
        public HttpRequest.BodyPublisher write(String data) {
            return HttpRequest.BodyPublishers.ofString(data);
        }
    }
}
