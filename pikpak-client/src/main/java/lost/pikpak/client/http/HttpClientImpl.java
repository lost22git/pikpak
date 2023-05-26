package lost.pikpak.client.http;

import lost.pikpak.client.Config;
import lost.pikpak.client.context.Context;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpRequest;
import java.util.Objects;
import java.util.concurrent.Executors;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.WARNING;

public class HttpClientImpl implements HttpClient {
    private static final System.Logger LOG = System.getLogger(HttpClientImpl.class.getName());
    private final Context context;
    private final java.net.http.HttpClient jdkHttpClient;

    public HttpClientImpl(Context context) {
        Objects.requireNonNull(context);
        this.context = context;
        this.jdkHttpClient = createJdkHttpClient(context);
    }

    private static void configureProxy(java.net.http.HttpClient.Builder builder,
                                       Config.User userConfig) {
        var proxy = userConfig.data().extract(Config.Data::proxy);
        if (proxy.isPresent()) {
            LOG.log(DEBUG, "configuring proxy={0} for http client", proxy.get().toString());
            var host = proxy.get().getHost();
            var port = proxy.get().getPort();
            try {
                var proxyAddr = InetSocketAddress.createUnresolved(host, port);
                builder.proxy(ProxySelector.of(proxyAddr));
            } catch (Exception e) {
                LOG.log(WARNING,
                    "failed to configure `Proxy` for http client, username={0}, proxy={1}, error={2}",
                    userConfig.username(), proxy.get().toString(), e.toString());
            }
        }
    }

    private java.net.http.HttpClient createJdkHttpClient(Context context) {
        LOG.log(DEBUG, "createJdkHttpClient start...");
        var builder = java.net.http.HttpClient.newBuilder();
        builder.executor(Executors.newVirtualThreadPerTaskExecutor());
        var userConfig = context.userConfig();
        configureProxy(builder, userConfig);
        LOG.log(DEBUG, "createJdkHttpClient end...");
        return builder.build();
    }

    @Override
    public Context context() {
        return this.context;
    }

    @Override
    public HttpResponse doSend(HttpRequest request) throws Exception {
        var res = this.jdkHttpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
        return new HttpResponse(request, res.statusCode(), res.headers().map(), res.body());
    }
}
