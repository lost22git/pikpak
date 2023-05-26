import lost.pikpak.client.util.Util;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UtilTest {

    @Test
    void dropJsonExtraWhitespace() {
        var json = """
                {
                    "name": "\"video \' xyz \' \".mp4",
                    "width": 1080,
                    "height": 760
                }
            """;
        var dropWhitespace = Util.dropJsonExtraWhitespace(json);
        var expected = "{\"name\":\"\"video'xyz'\".mp4\",\"width\":1080,\"height\":760}";
        assertThat(expected).isEqualTo(dropWhitespace);
    }

    @Test
    void dropJsonSingleLineComment() {
        var json = """
            {
                "name": "\"video \' xyz \' \".mp4", // sdfs
                "width": 1080,
                // height value
                "//height": 760
            }
            """;
        var dropComment = Util.dropJsonSingleLineComment(json);
        System.out.println("dropComment = " + dropComment);
    }

    @Test
    void compactJson() {
        var json = """
            {
                "name": "\"video \' xyz \' \".mp4", // sdfs
                "width": 1080,
                // height value
                "//height": 760
            }
            """;
        var compactJson = Util.compactJson(json);
        var expected = "{\"name\":\"\"video'xyz'\".mp4\",\"width\":1080,\"//height\":760}";
        assertThat(expected).isEqualTo(compactJson);
    }
}
