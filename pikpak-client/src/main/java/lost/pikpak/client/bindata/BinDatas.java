package lost.pikpak.client.bindata;

import java.io.InputStream;
import java.net.http.HttpRequest.BodyPublisher;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.Flow;

public final class BinDatas {
    private BinDatas() {}

    public static StringBinData string(String data) {
        return new StringBinData(data);
    }

    public static StringBinData string(String data, Charset charset) {
        return new StringBinData(data, charset);
    }

    public static StringBinData string(byte[] data) {
        return new StringBinData(data);
    }

    public static StringBinData string(byte[] data, int offset, int len, Charset charset) {
        return new StringBinData(data, offset, len, charset);
    }

    public static BytesBinData bytes(byte[] data) {
        return new BytesBinData(data);
    }

    public static BytesBinData bytes(byte[] data, int offset, int len) {
        return new BytesBinData(data, offset, len);
    }

    public static ByteBufferBinData bytebuffer(ByteBuffer data) {
        return new ByteBufferBinData(data);
    }

    public static InputStreamBinData inputStream(InputStream data) {
        return new InputStreamBinData(data);
    }

    public static InputStreamBinData inputStream(InputStream data, long len) {
        return new InputStreamBinData(data, len);
    }

    public static PublisherBinData publisher(Flow.Publisher<ByteBuffer> data, long len) {
        return new PublisherBinData(data, len);
    }

    public static PublisherBinData publisher(BodyPublisher data) {
        return new PublisherBinData(data);
    }
}
