package lost.pikpak.client.http;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lost.pikpak.client.util.Util;

public interface Params {
    Pattern P = Pattern.compile("([^=]+)=([^&]+)&?");

    static Builder builder() {
        return new Builder();
    }

    static Params parse(String str) {
        var decodedStr = URLDecoder.decode(str, UTF_8);
        var matcher = P.matcher(decodedStr);
        var builder = Params.builder();
        for (; matcher.find(); ) {
            var name = matcher.group(1).trim();
            var value = matcher.group(2).trim();
            builder.add(name, value);
        }
        return builder.build();
    }

    Set<String> names();

    List<String> value(String name);

    default String contentType() {
        return "application/x-www-form-urlencoded";
    }

    default String format() {
        var sb = new StringBuilder();
        for (var name : names()) {
            for (var value : value(name)) {
                sb.append(URLEncoder.encode(name, UTF_8))
                        .append("=")
                        .append(URLEncoder.encode(value, UTF_8))
                        .append("&");
            }
        }
        return Util.deleteLastChar(sb).toString();
    }

    final class Impl implements Params {
        private final Map<String, List<String>> map;

        private Impl(Map<String, List<String>> map) {
            if (map == null) {
                this.map = Map.of();
            } else {
                // copy immutables
                this.map = map.entrySet().stream()
                        .collect(Collectors.toUnmodifiableMap(
                                Map.Entry::getKey, e -> List.copyOf(e.getValue()), (v1, v2) -> v1));
            }
        }

        @Override
        public Set<String> names() {
            return this.map.keySet();
        }

        @Override
        public List<String> value(String name) {
            return this.map.getOrDefault(name, List.of());
        }
    }

    final class Builder implements lost.pikpak.client.util.Builder<Params> {
        private final Map<String, List<String>> map = new HashMap<>();

        private Builder() {}

        public Set<String> names() {
            return this.map.keySet();
        }

        public List<String> value(String name) {
            Objects.requireNonNull(name);
            return this.map.computeIfAbsent(name, k -> new ArrayList<>());
        }

        public Builder add(String name, String value) {
            Objects.requireNonNull(name);
            Objects.requireNonNull(value);
            value(name).add(value);
            return this;
        }

        @Override
        public Params build() {
            return new Impl(this.map);
        }
    }
}
