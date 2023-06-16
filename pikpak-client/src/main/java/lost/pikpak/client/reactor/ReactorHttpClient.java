package lost.pikpak.client.reactor;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.logging.LogLevel;
import lost.pikpak.client.Config;
import lost.pikpak.client.context.Context;
import lost.pikpak.client.http.HttpClient;
import lost.pikpak.client.http.body.BodyAdapters;
import reactor.adapter.JdkFlowAdapter;
import reactor.netty.http.client.HttpClient.ResponseReceiver;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.ProtocolException;
import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.ResponseInfo;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Flow;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;
import static reactor.netty.transport.ProxyProvider.Proxy.HTTP;

public final class ReactorHttpClient implements HttpClient {
    private static final System.Logger LOG =
        System.getLogger(ReactorHttpClient.class.getName());
    private final Context context;
    private final BodyAdapters bodyAdapters;

    private final reactor.netty.http.client.HttpClient httpClient;

    public ReactorHttpClient(Context context) {
        this.context = context;
        this.bodyAdapters = BodyAdapters.create();

        var httpClient = reactor.netty.http.client.HttpClient.create();
        // logging
        httpClient = httpClient.wiretap(
            ReactorHttpClient.class.getName(),
            LogLevel.DEBUG,
            AdvancedByteBufFormat.TEXTUAL);
        // proxy
        var proxy = context.userConfig().data().extract(Config.Data::proxy);
        if (proxy.isPresent()) {
            var p = proxy.get();
            if (LOG.isLoggable(DEBUG)) {
                LOG.log(DEBUG, "configuring proxy=" + p);
            }
            httpClient = httpClient.proxy(opt ->
                opt.type(HTTP).host(p.getHost()).port(p.getPort()));
        }
        this.httpClient = httpClient;

        if (LOG.isLoggable(INFO)) {
            LOG.log(INFO, "http client init ok");
        }
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
    public <T extends Response> T doSend(HttpRequest request) throws Exception {
        ResponseReceiver<?> resReceiver = this.httpClient
            .headers(h -> request.headers().map().forEach(h::add))
            .request(HttpMethod.valueOf(request.method()))
            .uri(request.uri())
            .send(JdkFlowAdapter.flowPublisherToFlux(
                    request.bodyPublisher().orElse(BodyPublishers.noBody()))
                .map(Unpooled::wrappedBuffer));
        var res = resReceiver.response().block();
        var resContent = resReceiver.responseContent();

        //noinspection unchecked
        return (T) new Response() {

            @Override
            public void close() throws Exception {
            }

            @Override
            public InputStream bodyInputStream() {
                return null;
            }

            @Override
            public Flow.Publisher<List<ByteBuffer>> bodyPublisher() {
                var flux = resContent.asByteBuffer().map(List::of);
                return JdkFlowAdapter.publisherToFlowPublisher(flux);
            }

            @Override
            public ResponseInfo responseInfo() {
                return new ResponseInfo() {
                    @Override
                    public int statusCode() {
                        return res.status().code();
                    }

                    @Override
                    public HttpHeaders headers() {
                        var map = new HashMap<String, List<String>>();
                        var headers = res.responseHeaders();
                        for (String name : headers.names()) {
                            map.put(name, headers.getAll(name));
                        }
                        return HttpHeaders.of(map, (v1, v2) -> true);
                    }

                    @Override
                    public Version version() {
                        var majorVersion = res.version().majorVersion();
                        return switch (majorVersion) {
                            case 1 -> Version.HTTP_1_1;
                            case 2 -> Version.HTTP_2;
                            default -> throw new UncheckedIOException(
                                new ProtocolException(
                                    "unsupported http protocol version: "
                                    + majorVersion)
                            );
                        };
                    }
                };
            }
        };
    }
}
