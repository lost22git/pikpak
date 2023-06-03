package lost.pikpak.client.http.body;

import lost.pikpak.client.http.Params;

import java.lang.reflect.Type;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodySubscriber;
import java.net.http.HttpResponse.BodySubscribers;
import java.nio.charset.StandardCharsets;

public final class FormParamBodyAdapter implements BodyAdapter<Params> {

    private final Reader reader = new Reader();
    private final Writer writer = new Writer();

    private FormParamBodyAdapter() {
    }

    public static FormParamBodyAdapter create() {
        return new FormParamBodyAdapter();
    }

    @Override
    public BodyReader<Params> reader() {
        return this.reader;
    }

    @Override
    public BodyWriter<Params> writer() {
        return this.writer;
    }


    private static final class Reader implements BodyReader<Params> {

        @Override
        public BodySubscriber<Params> read(Type type) {
            return BodySubscribers.mapping(
                BodySubscribers.ofString(StandardCharsets.UTF_8),
                Params::parse
            );
        }
    }


    private static final class Writer implements BodyWriter<Params> {

        @Override
        public BodyPublisher write(Params data) {
            return BodyPublishers.ofString(
                data.format()
            );
        }
    }

}
