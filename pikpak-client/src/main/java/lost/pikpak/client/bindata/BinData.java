package lost.pikpak.client.bindata;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.charset.Charset;

public interface BinData<S> {
    long UNKNOWN_LEN = -1;

    boolean consumed();

    void makeConsumed();

    default void ensureUnconsumed() {
        if (consumed()) {
            throw new IllegalStateException("already consumed, can not do more operations");
        }
    }

    S unwrap();

    long len();

    default StringBinData intoString() {
        return intoString(UTF_8);
    }

    default StringBinData intoString(Charset charset) {
        var bd = intoBytes();
        var offset = bd.offset();
        var len = bd.len();
        return new StringBinData(bd.unwrap(), offset, (int) len, charset);
    }

    BytesBinData intoBytes();

    ByteBufferBinData intoByteBuffer();

    InputStreamBinData intoInputStream();

    PublisherBinData intoPublisher();
}
