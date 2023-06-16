package lost.pikpak.client.http;

import lost.pikpak.client.Config;
import lost.pikpak.client.context.Context;
import lost.pikpak.client.http.HttpResponse.Body;
import lost.pikpak.client.http.body.BodyAdapters;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandler;
import java.util.Objects;
import java.util.concurrent.Executors;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;

final class HttpClientImpl implements HttpClient {
    private static final System.Logger LOG =
        System.getLogger(HttpClientImpl.class.getName());
    private final Context context;
    private final java.net.http.HttpClient jdkHttpClient;
    private final BodyAdapters bodyAdapters;

    public HttpClientImpl(Context context) {
        Objects.requireNonNull(context);
        this.context = context;
        this.jdkHttpClient = createJdkHttpClient(context);
        this.bodyAdapters = BodyAdapters.create();
        if (LOG.isLoggable(INFO)) {
            LOG.log(INFO, "http client init ok");
        }
    }

    private static void configureProxy(java.net.http.HttpClient.Builder builder,
                                       Config.User userConfig) {
        var proxy = userConfig.data().extract(Config.Data::proxy);
        if (proxy.isPresent()) {
            var p = proxy.get();
            if (LOG.isLoggable(DEBUG)) {
                LOG.log(DEBUG, "configuring proxy=" + p);
            }
            var host = p.getHost();
            var port = p.getPort();
            var addr = new InetSocketAddress(host, port);
            builder.proxy(ProxySelector.of(addr));
        }
    }

    private java.net.http.HttpClient createJdkHttpClient(Context context) {
        var builder = java.net.http.HttpClient.newBuilder();
        builder.executor(Executors.newVirtualThreadPerTaskExecutor());
        var userConfig = context.userConfig();
        configureProxy(builder, userConfig);
        return builder.build();
    }

    @Override
    public Context context() {
        return this.context;
    }

    @Override
    public BodyAdapters bodyAdapters() {
        return this.bodyAdapters;
    }

    @Override
    public <T extends Body<V, E>, V, E> HttpResponse<T, V, E> doSend(HttpRequest request,
                                                                     BodyHandler<T> bodyHandler) throws Exception {
        var res = this.jdkHttpClient.send(request, bodyHandler);
        return new HttpResponse<>(
            request,
            res.statusCode(),
            res.headers().map(),
            res.body()
        );
    }
}
