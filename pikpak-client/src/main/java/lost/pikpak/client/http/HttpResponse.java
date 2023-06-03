package lost.pikpak.client.http;

import lost.pikpak.client.http.body.BodyAdapters;

import java.lang.reflect.Type;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodySubscriber;
import java.net.http.HttpResponse.BodySubscribers;
import java.net.http.HttpResponse.ResponseInfo;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Simplified version of {@link java.net.http.HttpResponse}
 *
 * @param request the request
 * @param status  the statusCode
 * @param headers the headers
 * @param body    the body
 * @param <T>     the type of body
 * @param <V>     the inner type of ok body
 * @param <E>     the inner type of err body
 */
public record HttpResponse<T extends HttpResponse.Body<V, E>, V, E>(
    HttpRequest request,
    int status,
    Map<String, List<String>> headers,
    T body
) {
    public HttpResponse {
        Objects.requireNonNull(request);
        Objects.requireNonNull(body);
        // make headers immutable
        headers = headers == null ? Map.of() : Map.copyOf(headers);
    }


    public sealed interface Body<V, E> {
        static <V, E> BodySubscriber<Body<V, E>> okBodySubscriber(BodySubscriber<V> ups) {
            return BodySubscribers.mapping(ups, Body::createOk);
        }

        static <V, E> BodySubscriber<Body<V, E>> errBodySubscriber(BodySubscriber<E> ups) {
            return BodySubscribers.mapping(ups, Body::createErr);
        }

        static <V, E> OkBody<V, E> createOk(V value) {
            return new OkBody<>(value);
        }

        static <V, E> ErrBody<V, E> createErr(E error) {
            return new ErrBody<>(error);
        }

        V value();

        E error();

        default boolean isOk() {
            return this instanceof HttpResponse.OkBody;
        }

        default boolean isErr() {
            return !isOk();
        }

        class Handler<V> implements BodyHandler<Body<V, String>> {
            private final BodyAdapters bodyAdapters;
            private final Type type;

            private Handler(BodyAdapters bodyAdapters,
                            Type type) {
                this.bodyAdapters = bodyAdapters;
                this.type = type;
            }

            public static <V> Handler<V> create(BodyAdapters bodyAdapters,
                                                Type type) {
                return new Handler<>(bodyAdapters, type);
            }

            @SuppressWarnings("unchecked")
            @Override
            public BodySubscriber<Body<V, String>> apply(ResponseInfo responseInfo) {
                int status = responseInfo.statusCode();
                if (200 <= status && status < 300) {
                    var ups = (BodySubscriber<V>) bodyAdapters.json().reader().read(type);
                    return Body.okBodySubscriber(ups);
                } else {
                    var ups = bodyAdapters.text().reader().read(null);
                    return Body.errBodySubscriber(ups);
                }
            }
        }
    }

    public record OkBody<V, E>(
        V value
    ) implements Body<V, E> {

        @Override
        public E error() {
            return null;
        }
    }

    public record ErrBody<V, E>(
        E error
    ) implements Body<V, E> {

        @Override
        public V value() {
            return null;
        }
    }
}
