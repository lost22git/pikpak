import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import lost.pikpak.client.bindata.util.AggSubscriber;
import lost.pikpak.client.http.body.BodyAdapters;
import lost.pikpak.client.http.body.multipart.Multipart;
import lost.pikpak.client.http.body.multipart.MultipartBodyAdapter;
import lost.pikpak.client.http.body.multipart.Part;
import lost.pikpak.client.util.Util;
import org.junit.jupiter.api.Test;

public class MultipartTest {
    static {
        Util.configureJdkHttpClientLog();
        Util.initJUL();
    }

    @Test
    void write() {
        var multipart = Multipart.builder()
                .part(Part.builder("first")
                        .contentType("text/plain")
                        .body(BodyPublishers.ofString("first part is a plain text"))
                        .build())
                .part(Part.builder("second")
                        .contentType("application/json")
                        .header("x-header", "x-value")
                        .body(
                                BodyPublishers.ofString(
                                        """
                            {
                                "name": "second part",
                                "message": "second part is a json part"
                            }
                            """))
                        .build())
                .build();

        assertThat(multipart.boundary()).isEqualTo(Multipart.DEFAULT_BOUNDARY);

        assertThat(multipart.parts()).hasSize(2);

        var bodyPublisher = MultipartBodyAdapter.create().writer().write(multipart);

        assertThat(bodyPublisher.contentLength()).isPositive();

        var bodyString = AggSubscriber.collectString(bodyPublisher);
        System.out.println("multipart body:  \n" + bodyString);
    }

    @Test
    void upload() throws Exception {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var proxy = ProxySelector.of(InetSocketAddress.createUnresolved("127.0.0.1", 55556));
            var client = HttpClient.newBuilder().executor(executor).proxy(proxy).build();

            Supplier<InputStream> inputStream =
                    () -> MultipartTest.class.getClassLoader().getResourceAsStream("test.png");
            var len = -1;
            try (var is = inputStream.get()) {
                System.out.println("is.available() = " + is.available());
                len = is.available();
            }
            var multipart = Multipart.builder()
                    .filePart(
                            "file",
                            "test.png",
                            "image/png",
                            BodyPublishers.fromPublisher(BodyPublishers.ofInputStream(inputStream), len))
                    .build();

            var reqBody = BodyAdapters.create().multipart().writer().write(multipart);

            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.anonfiles.com/upload"))
                    .header("Content-Type", multipart.contentType())
                    .POST(reqBody)
                    .build();

            var res = client.send(request, BodyHandlers.ofString());

            assertThat(res.statusCode()).isEqualTo(200);
            assertThat(res.body()).contains("url", "id", "name");
        }
    }
}
