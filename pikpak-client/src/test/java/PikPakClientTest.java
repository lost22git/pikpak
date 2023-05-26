import lost.pikpak.client.Config;
import lost.pikpak.client.PikPakClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PikPakClientTest {

    @Test
    void createDefault() {
        var pikPak = new PikPakClient();
        assertThat(pikPak.context("")).isEmpty();
    }

    @Test
    void addUser() {
        var pikPak = new PikPakClient();

        var context = pikPak.addContext(
            "xxx",
            new Config.User("xxx", null, null, null)
        ).context("xxx");

        assertThat(context).isNotNull();

    }

}
