package lost.pikpak.client.cmd;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Objects;
import java.util.Optional;
import lost.pikpak.client.Config;
import lost.pikpak.client.context.Context;
import lost.pikpak.client.context.WithContext;
import lost.pikpak.client.error.ApiError;
import lost.pikpak.client.model.InitInfoParamBuilder;
import lost.pikpak.client.model.InitInfoParamMetaParamBuilder;
import lost.pikpak.client.model.InitInfoResult;
import lost.pikpak.client.token.Token;
import lost.pikpak.client.util.Util;

public interface InitCmd extends Cmd<InitInfoResult>, WithContext {
    static InitCmd create(Context context, String action) {
        return new Impl(context, action);
    }

    String action();

    InitCmd setAction(String action);

    interface Exec extends CmdExec<InitCmd, InitInfoResult> {
        static Exec create() {
            return new ExecImpl();
        }
    }

    final class Impl implements InitCmd {
        private final Context context;
        private final Exec exec;
        private String action;

        private Impl(Context context, String action) {
            Objects.requireNonNull(context);
            Objects.requireNonNull(action);
            this.context = context;
            this.action = action;
            this.exec = Exec.create();
        }

        @Override
        public InitInfoResult exec() throws ApiError {
            return this.exec.exec(this);
        }

        @Override
        public String action() {
            return this.action;
        }

        @Override
        public InitCmd setAction(String action) {
            this.action = action;
            return this;
        }

        @Override
        public Context context() {
            return this.context;
        }
    }

    final class ExecImpl implements Exec {
        private ExecImpl() {}

        @Override
        public InitInfoResult exec(InitCmd cmd) throws ApiError {
            var userConfig = cmd.context().userConfig();
            var httpClient = cmd.context().httpClient();

            // Headers
            var headers = httpClient.commonHeaders();
            // Body
            // timestamp 和 captchaSign 是挂钩的
            // TODO 如何动态获取
            var captchaSign = "1.1f3f0300501d60450a3371ca773f1138";
            var timestamp = "1680261267160";
            var packageName = "mypikpak.com";
            var param = InitInfoParamBuilder.builder()
                    .action(cmd.action())
                    .captchaToken("")
                    .clientId(userConfig.data().extract(Config.Data::clientId).orElse(""))
                    .deviceId(userConfig.data().extract(Config.Data::deviceId).orElse(""))
                    .meta(InitInfoParamMetaParamBuilder.builder()
                            .captchaSign(captchaSign)
                            .timestamp(timestamp)
                            .packageName(packageName)
                            .clientVersion(userConfig
                                    .data()
                                    .extract(Config.Data::clientVersion)
                                    .orElse(""))
                            .userId(Optional.ofNullable(userConfig.accessToken())
                                    .map(Token.AccessToken::sub)
                                    .orElse(""))
                            .email(userConfig.username())
                            .build())
                    .build();

            // Request
            var uri = URI.create("https://user.mypikpak.com/v1/shield/captcha/init");
            var request = HttpRequest.newBuilder().uri(uri).POST(Util.jsonBodyPublisher(param));
            headers.forEach(request::setHeader);
            return httpClient.send(request.build(), InitInfoResult.class);
        }
    }
}
