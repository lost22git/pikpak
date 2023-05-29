package lost.pikpak.client.http.body;

import java.lang.reflect.Type;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public interface BodyAdapter<T> {

    BodyReader<T> reader();

    BodyWriter<T> writer();

    interface BodyReader<T> {
        HttpResponse.BodySubscriber<T> read(Type type);
    }

    interface BodyWriter<T> {
        HttpRequest.BodyPublisher write(T data);
    }
}
