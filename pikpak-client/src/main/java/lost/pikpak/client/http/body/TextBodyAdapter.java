package lost.pikpak.client.http.body;

import java.lang.reflect.Type;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodySubscriber;
import java.net.http.HttpResponse.BodySubscribers;
import java.nio.charset.StandardCharsets;

import static java.nio.charset.StandardCharsets.*;

public final class TextBodyAdapter implements BodyAdapter<String> {
    private final Reader reader = new Reader();
    private final Writer writer = new Writer();

    private TextBodyAdapter() {
    }

    public static TextBodyAdapter create() {
        return new TextBodyAdapter();
    }

    @Override
    public BodyReader<String> reader() {
        return this.reader;
    }

    @Override
    public BodyWriter<String> writer() {
        return this.writer;
    }

    private static final class Reader implements BodyReader<String> {
        @Override
        public BodySubscriber<String> read(Type type) {
            return BodySubscribers.ofString(UTF_8);
        }
    }

    private static final class Writer implements BodyWriter<String> {
        @Override
        public BodyPublisher write(String data) {
            return BodyPublishers.ofString(data);
        }
    }
}
