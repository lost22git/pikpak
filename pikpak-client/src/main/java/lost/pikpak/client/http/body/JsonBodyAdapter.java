package lost.pikpak.client.http.body;

import lost.pikpak.client.util.Util;

import java.lang.reflect.Type;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class JsonBodyAdapter<T> implements BodyAdapter<T> {
    public static <T> JsonBodyAdapter<T> create() {
        return new JsonBodyAdapter<>();
    }

    @Override
    public BodyReader<T> reader() {
        return new Reader<>();
    }

    @Override
    public BodyWriter<T> writer() {
        return new Writer<>();
    }

    private static class Reader<T> implements BodyReader<T> {
        @Override
        public HttpResponse.BodySubscriber<T> read(Type type) {
            return Util.jsonBodySubscriber(type);
        }
    }

    private static class Writer<T> implements BodyWriter<T> {

        @Override
        public HttpRequest.BodyPublisher write(T data) {
            return Util.jsonBodyPublisher(data);
        }
    }
}
