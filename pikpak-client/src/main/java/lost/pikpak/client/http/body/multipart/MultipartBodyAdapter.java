package lost.pikpak.client.http.body.multipart;

import lost.pikpak.client.http.body.BodyAdapter;

import java.lang.reflect.Type;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

public final class MultipartBodyAdapter implements BodyAdapter<Multipart> {

    private final Reader reader = new Reader();
    private final Writer writer = new Writer();

    private MultipartBodyAdapter() {
    }

    public static MultipartBodyAdapter create() {
        return new MultipartBodyAdapter();
    }

    @Override
    public BodyReader<Multipart> reader() {
        return this.reader;
    }

    @Override
    public BodyWriter<Multipart> writer() {
        return this.writer;
    }

    private static final class Reader implements BodyReader<Multipart> {
        @Override
        public HttpResponse.BodySubscriber<Multipart> read(Type type) {
            throw new UnsupportedOperationException("TODO");
        }
    }

    private static final class Writer implements BodyWriter<Multipart> {
        @Override
        public HttpRequest.BodyPublisher write(Multipart data) {

            var list = new ArrayList<HttpRequest.BodyPublisher>();

            for (Part part : data.parts()) {
                // boundary
                var boundary = HttpRequest.BodyPublishers.ofString(data.boundary() + "\r\n");
                list.add(boundary);

                // headers
                var sb = new StringBuilder();
                part.headers().forEach((k, v) -> {
                    sb.append(k).append(": ").append(v);
                    sb.append("\r\n");
                });
                sb.append("\r\n");
                var headers = HttpRequest.BodyPublishers.ofString(sb.toString());
                list.add(headers);

                // body
                list.add(part.body());
                list.add(HttpRequest.BodyPublishers.ofString("\r\n"));
            }
            var boundaryEnd = HttpRequest.BodyPublishers.ofString(data.boundaryEnd());
            list.add(boundaryEnd);

            return HttpRequest.BodyPublishers.concat(list.toArray(HttpRequest.BodyPublisher[]::new));
        }
    }
}
