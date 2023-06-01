import lost.pikpak.client.http.body.multipart.Multipart;
import lost.pikpak.client.http.body.multipart.MultipartBodyAdapter;
import lost.pikpak.client.http.body.multipart.Part;
import lost.pikpak.client.util.Util;
import org.junit.jupiter.api.Test;

import java.net.http.HttpRequest;

import static org.assertj.core.api.Assertions.assertThat;

public class MultipartTest {
    @Test
    void write() {
        var multipart = Multipart.builder()
            .part(
                Part.builder("first")
                    .contentType("text/plain")
                    .body(HttpRequest.BodyPublishers.ofString("first part is a plain text"))
                    .build()
            )
            .part(
                Part.builder("second")
                    .contentType("text/json")
                    .header("x-header", "x-value")
                    .body(HttpRequest.BodyPublishers.ofString("""
                        {
                            "name": "second part",
                            "message": "second part is a json part"
                        }
                        """))
                    .build()
            )
            .build();

        assertThat(multipart.boundary()).isEqualTo(Multipart.DEFAULT_BOUNDARY);

        assertThat(multipart.parts()).hasSize(2);

        var bodyPublisher = MultipartBodyAdapter.create()
            .writer()
            .write(multipart);

        assertThat(bodyPublisher.contentLength()).isPositive();

        var bodyString = Util.collectIntoString(bodyPublisher);
        System.out.println("multipart body:  \n" + bodyString);

    }
}
