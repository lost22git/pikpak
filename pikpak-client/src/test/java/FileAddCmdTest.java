import static org.assertj.core.api.Assertions.assertThat;

import lost.pikpak.client.Config;
import lost.pikpak.client.PikPakClient;
import lost.pikpak.client.enums.FolderType;
import lost.pikpak.client.enums.Kind;
import lost.pikpak.client.enums.UploadType;
import lost.pikpak.client.util.Util;

public class FileAddCmdTest {

    static {
        Util.initJUL();
    }

    //    @Test
    void exec() throws Exception {
        var pikpak = PikPakClient.create();
        var username = "tt@uuf.me";
        var passwd = "ringbuffer111";
        var context = pikpak.addContext(Config.User.create(username).setPasswd(passwd))
                .context(username)
                .get();

        context.signInCmd().exec();

        assertThat(context.userConfig().accessToken()).isNotNull();

        // Add file
        var result = context.fileAddCmd()
                .setKind(Kind.FILE)
                .setFolderType(FolderType.DOWNLOAD)
                .setUploadType(UploadType.UPLOAD_TYPE_URL)
                .setParentId("")
                .setUrl(
                        "magnet:?xt=urn:btih:9E0173D31000DB8BBA93621F6E885EDF92ADD3C9&dn=%E8%91%AB%E8%8A%A6%E5%85%84%E5%BC%9F%E5%85%A8%E9%9B%86%2B%E7%BB%AD%E9%9B%86%E8%91%AB%E8%8A%A6%E5%B0%8F%E9%87%91%E5%88%9A%2B%E7%94%B5%E5%BD%B1%E7%89%88")
                .exec();
        assertThat(result).isNotNull();
    }
}
