import lost.pikpak.client.Config;
import lost.pikpak.client.PikPakClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PikPakClientTest {

    @Test
    void createDefault() {
        var pikPak = PikPakClient.create();
        assertThat(pikPak.context("")).isEmpty();
    }

    @Test
    void addUser() {
        var pikPak = PikPakClient.create();

        var context = pikPak.addContext(Config.User.create("xxx"))
            .context("xxx");

        assertThat(context).isNotNull();

    }

}
