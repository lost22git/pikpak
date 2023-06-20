package lost.pikpak.client.util;

import io.avaje.jsonb.Jsonb;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodySubscriber;
import java.net.http.HttpResponse.BodySubscribers;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.LogManager;

public final class Util {
    private static final Jsonb JSONB;

    static {
        JSONB = Jsonb.builder()
                .failOnUnknown(false)
                .serializeEmpty(true)
                .serializeNulls(true)
                .build();
    }

    private Util() {}

    public static boolean hasClass(String classname) {
        try {
            var classLoader = Util.class.getClassLoader();
            Class.forName(classname, false, classLoader);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static StringBuilder deleteLastChar(StringBuilder sb) {
        if (!sb.isEmpty()) sb.deleteCharAt(sb.length() - 1);
        return sb;
    }

    public static String quote(String data) {
        return "\"" + escapeQuote(data) + "\"";
    }

    public static String escapeQuote(String data) {
        Objects.requireNonNull(data);
        return data.replace("\"", "\\\"");
    }

    @SuppressWarnings("unchecked")
    public static <T> T fromJson(String json, Type type) {
        return (T) JSONB.type(type).fromJson(json);
    }

    public static String toJson(Object obj) {
        return JSONB.toJson(obj);
    }

    public static String toJsonPretty(Object obj) {
        return JSONB.toJsonPretty(obj);
    }

    public static byte[] toJsonBytes(Object obj) {
        return JSONB.toJsonBytes(obj);
    }

    public static void toJson(Object obj, OutputStream out) {
        JSONB.toJson(out);
    }

    public static String dropJsonSingleLineComment(String json) {
        var sb = new StringBuilder();
        // commented -> maybe_commented -> quoted -> escaped
        boolean quoted = false;
        boolean escaped = false;
        boolean maybe_commented = false;
        boolean commented = false;
        for (char c : json.toCharArray()) {
            // commented
            if (c == '\n') {
                maybe_commented = false;
                commented = false;
                sb.append('\n');
                continue;
            }
            if (commented) {
                continue;
            }
            // maybe_commented
            if (c == '/' && !quoted) {
                if (maybe_commented) {
                    commented = true;
                    // delete first '/'
                    sb.deleteCharAt(sb.length() - 1);
                    continue;
                } else {
                    maybe_commented = true;
                }
            } else {
                if (maybe_commented) {
                    maybe_commented = false;
                }
            }
            // quoted
            if (c == '"') {
                quoted = escaped ? quoted : !quoted;
            }
            // escaped
            escaped = (c == '\\');

            sb.append(c);
        }
        return sb.toString();
    }

    public static String dropJsonExtraWhitespace(String json) {
        var sb = new StringBuilder();
        boolean quoted = false;
        boolean escaped = false;
        for (char c : json.toCharArray()) {
            // drop -> quoted -> escaped
            // drop
            boolean drop = !quoted && Character.isWhitespace(c);
            if (drop) {
                continue;
            }
            // quoted
            if (c == '"') {
                quoted = escaped ? quoted : !quoted;
            }
            // escaped
            escaped = (c == '\\');

            sb.append(c);
        }
        return sb.toString();
    }

    public static String compactJson(String json) {
        return dropJsonExtraWhitespace(dropJsonSingleLineComment(json));
    }

    public static BodyPublisher jsonBodyPublisher(Object obj) {
        return BodyPublishers.ofByteArray(toJsonBytes(obj));
    }

    public static <T> BodyHandler<T> jsonBodyHandle(Type bodyType) {
        return responseInfo -> jsonBodySubscriber(bodyType);
    }

    public static <T> BodySubscriber<T> jsonBodySubscriber(Type bodyType) {
        return BodySubscribers.mapping(BodySubscribers.ofString(StandardCharsets.UTF_8), bodyJson -> {
            // replace value "" into null
            // e.g. { "name": "" } -> { "name": null }
            bodyJson = bodyJson.replace("\"\"", "null");
            return Util.fromJson(bodyJson, bodyType);
        });
    }

    /**
     * generate request id
     *
     * @return the request id
     */
    public static String genRequestId() {
        return UUID.randomUUID().toString();
    }

    /**
     * init {@link java.util.logging.LogManager} by loading config file
     * <p>
     * config file load order:
     * <p>
     * 1) system property: `java.util.logging.config.file`
     * <p>
     * 2) classpath://logging.properties
     * <p>
     * 3) not config file loaded, use JUL default config
     */
    public static void initJUL() {
        var configFile = System.getProperty("java.util.logging.config.file");
        if (configFile == null || configFile.trim().isBlank()) {
            System.err.println("initJUL: -Djava.util.logging.config.file not specified, "
                    + "try use JUL config file classpath://logging.properties");
            configFile = Optional.ofNullable(Util.class.getClassLoader().getResource("logging.properties"))
                    .map(URL::getFile)
                    .orElse(null);
            if (configFile == null) {
                System.err.println("initJUL: JUL config file classpath://logging.properties not found.");
                return;
            }
            System.setProperty("java.util.logging.config.file", configFile);
            try {
                LogManager.getLogManager().readConfiguration();
            } catch (Exception e) {
                throw new RuntimeException("initJUL error", e);
            }
        }
    }

    /**
     * see {@code jdk.internal.net.http.common.Log}
     */
    public static void configureJdkHttpClientLog() {

        System.setProperty("jdk.httpclient.HttpClient.log", "headers,content,requests,errors,frames:data");
        //        System.setProperty("jdk.httpclient.HttpClient.log", "all");
        //        System.setProperty("jdk.internal.httpclient.debug", "true");
    }
}
