package lost.pikpak.client.http.body.multipart;

import lost.pikpak.client.util.Util;

import java.net.http.HttpRequest;
import java.util.*;

public interface Part {

    String EOL = "\r\n";

    static Builder builder(String name) {
        return new Builder(name);
    }

    String name();

    Optional<String> filename();

    String contentType();

    Map<String, String> headers();

    HttpRequest.BodyPublisher body();

    default String contentDisposition() {
        var sb = new StringBuilder();
        sb.append("form-data;");
        sb.append(" name=").append(Util.quote(name())).append(";");
        filename().ifPresent(fname -> sb.append(" filename=").append(Util.quote(fname)).append(";"));
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    default HttpRequest.BodyPublisher bodyPublisher(String boundaryStart) {
        var list = new ArrayList<HttpRequest.BodyPublisher>(4);
        // boundary
        var boundary = HttpRequest.BodyPublishers.ofString(boundaryStart + EOL);
        list.add(boundary);

        // headers
        var sb = new StringBuilder();
        headers().forEach((k, v) -> {
            sb.append(k).append(": ").append(v);
            sb.append(EOL);
        });
        sb.append(EOL);
        var headers = HttpRequest.BodyPublishers.ofString(sb.toString());
        list.add(headers);

        // body
        list.add(body());
        //
        list.add(HttpRequest.BodyPublishers.ofString(EOL));

        return HttpRequest.BodyPublishers.concat(list.toArray(HttpRequest.BodyPublisher[]::new));
    }

    final class Impl implements Part {
        private final String name;
        private final String filename;
        private final String contentType;
        private final Map<String, String> headers;
        private final HttpRequest.BodyPublisher body;

        private Impl(String name,
                     String filename,
                     String contentType,
                     Map<String, String> headers,
                     HttpRequest.BodyPublisher body) {
            Objects.requireNonNull(name);
            Objects.requireNonNull(contentType);
            Objects.requireNonNull(body);
            this.name = name;
            this.filename = filename;
            this.contentType = contentType;
            this.body = body;
            {
                Map<String, String> tmpHeaders = new HashMap<>(headers);
                // not be overridden by headers
                tmpHeaders.put("Content-Disposition", contentDisposition());
                tmpHeaders.put("Content-Type", contentType);
                tmpHeaders.put("Content-Length", String.valueOf(body.contentLength()));
                this.headers = Map.copyOf(tmpHeaders);
            }
        }

        @Override
        public String name() {
            return this.name;
        }

        @Override
        public Optional<String> filename() {
            return Optional.ofNullable(this.filename);
        }

        @Override
        public String contentType() {
            return this.contentType;
        }

        @Override
        public Map<String, String> headers() {
            return this.headers;
        }

        public HttpRequest.BodyPublisher body() {
            return this.body;
        }
    }

    final class Builder implements lost.pikpak.client.util.Builder<Part> {
        private final Map<String, String> headers;
        private String name;
        private String filename;
        private String contentType;
        private HttpRequest.BodyPublisher body;

        private Builder(String name) {
            Objects.requireNonNull(name);
            this.name = name;
            this.headers = new HashMap<>();
        }

        public Builder name(String name) {
            Objects.requireNonNull(name);
            this.name = name;
            return this;
        }

        public String name() {
            return this.name;
        }

        public Builder filename(String filename) {
            Objects.requireNonNull(filename);
            this.filename = filename;
            return this;
        }

        public String filename() {
            return this.filename;
        }

        public Builder contentType(String contentType) {
            Objects.requireNonNull(contentType);
            this.contentType = contentType;
            return this;
        }

        public String contentType() {
            return this.contentType;
        }

        public Builder header(String name,
                              String value) {
            Objects.requireNonNull(name);
            Objects.requireNonNull(value);
            this.headers.put(name, value);
            return this;
        }

        public Map<String, String> headers() {
            return this.headers;
        }

        public Builder body(HttpRequest.BodyPublisher body) {
            Objects.requireNonNull(body);
            this.body = body;
            return this;
        }

        public HttpRequest.BodyPublisher body() {
            return this.body;
        }

        @Override
        public Part build() {
            return new Impl(name, filename, contentType, headers, body);
        }
    }
}
