package lost.pikpak.client.helidon;

import io.helidon.common.http.Http.Header;
import io.helidon.common.http.Http.Method;
import io.helidon.nima.webclient.http1.Http1Client;
import io.helidon.nima.webclient.http1.Http1ClientResponse;
import lost.pikpak.client.context.Context;
import lost.pikpak.client.http.HttpClient;
import lost.pikpak.client.http.body.BodyAdapters;
import lost.pikpak.client.util.Util;

import java.io.InputStream;
import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.ResponseInfo;
import java.util.Objects;

import static java.lang.System.Logger.Level.DEBUG;
import static java.net.http.HttpClient.Version.HTTP_1_1;

public final class HelidonNimaWebClient implements HttpClient {
    private static final System.Logger LOG =
        System.getLogger(HelidonNimaWebClient.class.getName());
    private final Context context;
    private final BodyAdapters bodyAdapters;
    private final Http1Client http1Client;

    public HelidonNimaWebClient(Context context) {
        Objects.requireNonNull(context);
        this.context = context;
        this.bodyAdapters = BodyAdapters.create();
        this.http1Client = Http1Client.builder()
            .build();
        if (LOG.isLoggable(DEBUG)) {
            LOG.log(DEBUG, "Helidon nima webclient init ok");
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

        var uri = request.uri().toString();
        var method = request.method();
        var headers = request.headers().map();
        var body = request.bodyPublisher();
        var req = http1Client
            .method(Method.create(method))
            .uri(uri);
        headers.forEach((k, v) -> req.header(Header.create(Header.create(k), v)));

        Http1ClientResponse res;
        if (body.isPresent()) {
            res = req.outputStream(out -> Util.collectIntoStream(body.get(), out));
        } else {
            res = req.request();
        }

        //noinspection unchecked
        return (T) new Response() {
            @Override
            public void close() throws Exception {
                res.close();
            }

            @Override
            public InputStream bodyInputStream() {
                return res.inputStream();
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
                        //noinspection removal
                        return HttpHeaders.of(res.headers().toMap(), (x, y) -> true);
                    }

                    @Override
                    public Version version() {
                        return HTTP_1_1;
                    }
                };
            }
        };
    }
}
