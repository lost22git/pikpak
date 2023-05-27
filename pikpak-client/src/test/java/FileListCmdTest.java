import lost.pikpak.client.Config;
import lost.pikpak.client.PikPakClient;
import lost.pikpak.client.enums.SortOrder;
import lost.pikpak.client.enums.ThumbnailSize;
import lost.pikpak.client.util.Util;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FileListCmdTest {

    static {
        Util.initJUL();
    }

    @Test
    void exec() throws Exception {
        var pikpak = PikPakClient.create();
        var username = "tt@uuf.me";
        var passwd = "ringbuffer111";
        var context = pikpak.addContext(Config.User.create(username).setPasswd(passwd))
            .context(username).get();
        context.signInCmd().exec();

        assertThat(context.userConfig().accessToken()).isNotNull();

        // list files
        var list = context.fileListCmd()
            .setLimit(100)
            .setPageToken("")
            .setSortOrder(SortOrder.MODIFY_TIME_DESC)
            .setParentId("")
            .setThumbnailSize(ThumbnailSize.SIZE_MEDIUM)
            .setWithAudit(true)
            .exec();
        assertThat(list).isNotNull();

        System.out.println(list);
    }


    @Test
    void add() throws Exception {

    }

}
