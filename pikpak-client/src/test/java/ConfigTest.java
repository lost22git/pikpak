import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.Map;
import lost.pikpak.client.Config;
import lost.pikpak.client.util.Util;
import org.junit.jupiter.api.Test;

public class ConfigTest {
    @Test
    void dataParent() {
        var config = new Config(
                null,
                Map.of(
                        "xxx", new Config.User("xxx", null, null, null),
                        "yyy", new Config.User("yyy", null, null, null)));

        var defaultData = Config.Data.createDefault();

        assertThat(defaultData)
                .isEqualTo(config.data().parent().get())
                .isEqualTo(
                        config.users().get("xxx").data().parent().get().parent().get());
    }

    @Test
    void dataExtract() {
        var json =
                """
            {
                "http_referer":"http://localhost:8888"
            }
            """;
        Config.Data yyyUserData = Util.fromJson(json, Config.Data.class);
        var config = new Config(
                null,
                Map.of(
                        "xxx", new Config.User("xxx", null, null, null),
                        "yyy", new Config.User("yyy", null, null, yyyUserData)));

        var defaultData = Config.Data.createDefault();

        assertThat(defaultData.httpReferer())
                .isEqualTo(URI.create("https://mypikpak.com/"))
                .isEqualTo(config.data().extract(Config.Data::httpReferer).get())
                .isEqualTo(config.users()
                        .get("xxx")
                        .data()
                        .extract(Config.Data::httpReferer)
                        .get())
                .isNotEqualTo(config.users()
                        .get("yyy")
                        .data()
                        .extract(Config.Data::httpReferer)
                        .get());
    }

    @Test
    void addUser() {
        var config = Config.createDefault();
        assertThat(config.users()).hasSize(0);

        var xxxUser = config.addUser(new Config.User("xxx", null, null, null)).user("xxx");
        assertThat(config.users()).hasSize(1);

        assertThat(config.data().extract(Config.Data::httpReferer).get())
                .isEqualTo(URI.create("https://mypikpak.com/"))
                .isEqualTo(config.users()
                        .get("xxx")
                        .data()
                        .extract(Config.Data::httpReferer)
                        .get());
    }
}
