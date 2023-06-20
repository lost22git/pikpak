import static org.assertj.core.api.Assertions.assertThat;

import lost.pikpak.client.Config;
import lost.pikpak.client.PikPakClient;
import lost.pikpak.client.util.Util;
import org.junit.jupiter.api.Test;

public class InitCmdTest {

    static {
        Util.initJUL();
    }

    @Test
    void exec() throws Exception {
        var pikpak = PikPakClient.create();
        var username = "tt@uuf.me";
        var passwd = "ringbuffer111";
        var context = pikpak.addContext(Config.User.create(username).setPasswd(passwd))
                .context(username)
                .get();
        var action = "POST:/v1/auth/signin";
        var initInfoResult = context.initCmd(action).exec();

        assertThat(initInfoResult).isNotNull();
        assertThat(initInfoResult.captchaToken()).isNotNull();
    }
}
