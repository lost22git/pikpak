package lost.pikpak.client.http.body;

import lost.pikpak.client.util.Util;

import java.lang.reflect.Type;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodySubscriber;

public final class JsonBodyAdapter<T> implements BodyAdapter<T> {
    private final Reader<T> reader = new Reader<>();
    private final Writer<T> writer = new Writer<>();

    private JsonBodyAdapter() {
    }

    public static <T> JsonBodyAdapter<T> create() {
        return new JsonBodyAdapter<>();
    }

    @Override
    public BodyReader<T> reader() {
        return this.reader;
    }

    @Override
    public BodyWriter<T> writer() {
        return this.writer;
    }

    private static final class Reader<T> implements BodyReader<T> {
        @Override
        public BodySubscriber<T> read(Type type) {
            return Util.jsonBodySubscriber(type);
        }
    }

    private static final class Writer<T> implements BodyWriter<T> {

        @Override
        public HttpRequest.BodyPublisher write(T data) {
            return Util.jsonBodyPublisher(data);
        }
    }
}
