import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import lost.pikpak.client.http.Params;
import lost.pikpak.client.util.Util;
import org.junit.jupiter.api.Test;

public class ParamsTest {

    @Test
    void format_and_parse() {
        var meta = Util.compactJson(
                """
            {
                "bitrate":  8700,
                "fps": 60
            }
            """);
        var param = Params.builder()
                .add("name", "video.mp4")
                .add("width", "1080")
                .add("tag", "3d")
                .add("tag", "animate")
                .add("meta", meta)
                .build();

        var format = param.format();

        System.out.println("format = " + format);

        var parse = Params.parse(format);
        assertThat(parse).isNotNull().extracting(p -> p.value("name")).isEqualTo(List.of("video.mp4"));

        assertThat(parse).extracting(p -> p.value("width")).isEqualTo(List.of("1080"));

        assertThat(parse).extracting(p -> p.value("tag")).matches(v -> v.containsAll(List.of("animate", "3d")));

        assertThat(parse).extracting(p -> p.value("meta")).isEqualTo(List.of(meta));
    }
}
