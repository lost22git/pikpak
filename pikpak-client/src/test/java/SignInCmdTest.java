import lost.pikpak.client.Config;
import lost.pikpak.client.PikPakClient;
import lost.pikpak.client.util.Util;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SignInCmdTest {
    static {
        Util.initJUL();
    }

    @Test
    void exec() throws Exception {
        var pikpak = new PikPakClient();
        var username = "tt@uuf.me";
        var passwd = "ringbuffer111";
        var context = pikpak.addContext(username, new Config.User(username, passwd, null, null))
            .context(username).get();

        context.signInCmd().exec();

        assertThat(context.userConfig().accessToken()).isNotNull();
    }
}
