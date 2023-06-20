package lost.pikpak.client;

import lost.pikpak.client.util.Util;

public class Main {

    public static void main(String[] args) {
        Util.initJUL();
        var pikpak = PikPakClient.create();
    }
}
