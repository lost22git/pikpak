package lost.pikpak.client.cmd;

import lost.pikpak.client.error.ApiError;

@FunctionalInterface
public interface Cmd<RES> {

    RES exec() throws ApiError;

    @FunctionalInterface
    interface CmdExec<CMD extends Cmd<?>, RES> {

        RES exec(CMD cmd) throws ApiError;
    }
}
