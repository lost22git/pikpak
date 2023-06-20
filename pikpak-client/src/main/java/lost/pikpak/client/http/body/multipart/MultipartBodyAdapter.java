package lost.pikpak.client.http.body.multipart;

import java.lang.reflect.Type;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import lost.pikpak.client.http.body.BodyAdapter;

/**
 * <pre>
 * # Header:
 * Content-Type: multipart/form-data; boundary=^_^
 * Content-Length: 333173
 *
 * # Body:
 * --^_^                                // first part start
 * Content-Disposition: form-data; name="file"; filename="test.png"
 * Content-Type: image/png
 * Content-Length: 333038
 *
 * ... file content bytes
 * --^_^                                // second part start
 * ... second part headers
 *
 * ... second part body
 * --^_^--                              // body end
 * </pre>
 */
public final class MultipartBodyAdapter implements BodyAdapter<Multipart> {

    private final Reader reader = new Reader();
    private final Writer writer = new Writer();

    private MultipartBodyAdapter() {}

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
        public BodyPublisher write(Multipart data) {
            var boundaryStart = data.boundaryStart();
            var boundaryEnd = data.boundaryEnd();
            var partCount = data.parts().size();

            List<BodyPublisher> list = new ArrayList<>(partCount + 1);
            // parts
            for (Part part : data.parts()) {
                list.add(part.bodyPublisher(boundaryStart));
            }
            // end
            list.add(BodyPublishers.ofString(boundaryEnd + Part.EOL));

            return BodyPublishers.concat(list.toArray(BodyPublisher[]::new));
        }
    }
}
