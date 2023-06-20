package lost.pikpak.client.http.body.multipart;

import java.net.http.HttpRequest.BodyPublisher;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.lang.System.Logger.Level.DEBUG;

public sealed interface Multipart {
    System.Logger LOG = System.getLogger(Multipart.class.getName());

    String DEFAULT_BOUNDARY = "^_^";

    static Builder builder() {
        return new Builder();
    }

    String boundary();

    default String boundaryStart() {
        return "--" + boundary();
    }

    default String boundaryEnd() {
        return boundaryStart() + "--";
    }

    default String contentType() {
        return "multipart/form-data; boundary=" + boundary();
    }

    List<Part> parts();

    final class Impl implements Multipart {
        private final String boundary;
        private final List<Part> parts;

        private Impl(String boundary,
                     List<Part> parts) {
            if (boundary == null || boundary.trim().isBlank()) {
                if (LOG.isLoggable(DEBUG)) {
                    LOG.log(DEBUG, "boundary is null, so we use default: " + DEFAULT_BOUNDARY);
                }
                this.boundary = DEFAULT_BOUNDARY;
            } else {
                this.boundary = boundary;
            }
            this.parts = parts == null ? List.of() : List.copyOf(parts);
        }

        @Override
        public String boundary() {
            return this.boundary;
        }

        @Override
        public List<Part> parts() {
            return this.parts;
        }

    }

    class Builder implements lost.pikpak.client.util.Builder<Multipart> {
        private final List<Part> parts = new ArrayList<>();
        private String boundary;

        private Builder() {
        }

        public Builder boundary(String boundary) {
            this.boundary = boundary;
            return this;
        }

        public String boundary() {
            return this.boundary;
        }

        public Builder part(Part part) {
            Objects.requireNonNull(part);
            this.parts.add(part);
            return this;
        }

        public Builder textPart(String name,
                                String contentType,
                                BodyPublisher body) {
            var part = Part.builder(name)
                .contentType(contentType)
                .body(body)
                .build();
            this.parts.add(part);
            return this;
        }

        public Builder filePart(String name,
                                String filename,
                                String contentType,
                                BodyPublisher body) {
            var part = Part.builder(name)
                .filename(filename)
                .contentType(contentType)
                .body(body)
                .build();
            this.parts.add(part);
            return this;
        }

        public List<Part> parts() {
            return this.parts;
        }

        @Override
        public Multipart build() {
            return new Impl(this.boundary, this.parts);
        }
    }
}
