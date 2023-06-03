package lost.pikpak.client.http.body;

import java.lang.reflect.Type;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodySubscriber;

public interface BodyAdapter<T> {

    BodyReader<T> reader();

    BodyWriter<T> writer();

    interface BodyReader<T> {
        BodySubscriber<T> read(Type type);
    }

    interface BodyWriter<T> {
        BodyPublisher write(T data);
    }
}
