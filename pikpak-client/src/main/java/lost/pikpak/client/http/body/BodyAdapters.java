package lost.pikpak.client.http.body;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lost.pikpak.client.http.Params;
import lost.pikpak.client.http.body.multipart.Multipart;
import lost.pikpak.client.http.body.multipart.MultipartBodyAdapter;

public class BodyAdapters {
    private final List<BodyAdapter<?>> bodyAdapters = new ArrayList<>();

    public static BodyAdapters create() {
        return new BodyAdapters()
                .register(TextBodyAdapter.create())
                .register(JsonBodyAdapter.create())
                .register(FormParamBodyAdapter.create())
                .register(MultipartBodyAdapter.create());
    }

    public <T> BodyAdapters register(BodyAdapter<T> adapter) {
        Objects.requireNonNull(adapter);
        bodyAdapters.add(adapter);
        return this;
    }

    public <T> BodyAdapters unregister(BodyAdapter<T> adapter) {
        Objects.requireNonNull(adapter);
        bodyAdapters.remove(adapter);
        return this;
    }

    public <T> BodyAdapters unregisterByClass(Class<? extends BodyAdapter<T>> adapterClass) {
        Objects.requireNonNull(adapterClass);
        bodyAdapters.removeIf(
                item -> adapterClass.getName().equals(item.getClass().getName()));
        return this;
    }

    public Stream<BodyAdapter<?>> bodyAdapterStream() {
        return bodyAdapters.stream();
    }

    @SuppressWarnings({"OptionalGetWithoutIsPresent", "unchecked"})
    public BodyAdapter<String> text() {
        return (BodyAdapter<String>) bodyAdapterStream()
                .filter(TextBodyAdapter.class::isInstance)
                .findFirst()
                .get();
    }

    @SuppressWarnings({"OptionalGetWithoutIsPresent", "unchecked"})
    public <T> BodyAdapter<T> json() {
        return (BodyAdapter<T>) bodyAdapterStream()
                .filter(JsonBodyAdapter.class::isInstance)
                .findFirst()
                .get();
    }

    @SuppressWarnings({"OptionalGetWithoutIsPresent", "unchecked"})
    public BodyAdapter<Params> formParam() {
        return (BodyAdapter<Params>) bodyAdapterStream()
                .filter(FormParamBodyAdapter.class::isInstance)
                .findFirst()
                .get();
    }

    @SuppressWarnings({"OptionalGetWithoutIsPresent", "unchecked"})
    public BodyAdapter<Multipart> multipart() {
        return (BodyAdapter<Multipart>) bodyAdapterStream()
                .filter(MultipartBodyAdapter.class::isInstance)
                .findFirst()
                .get();
    }
}
