package lost.pikpak.client.bindata;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayInputStream;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Objects;

public final class StringBinData extends BinDataBase<String> {
    private final String data;
    private final Charset charset;
    private final byte[] dataBytes;
    private final int offset;

    public StringBinData(String data) {
        this(data, UTF_8);
    }

    public StringBinData(byte[] data) {
        this(Objects.requireNonNull(data), 0, data.length, UTF_8);
    }

    public StringBinData(byte[] data, int offset, int len, Charset charset) {
        this(
                new String(Objects.requireNonNull(data), offset, len, Objects.requireNonNull(charset)),
                charset,
                data,
                offset,
                len);
    }

    public StringBinData(String data, Charset charset) {
        this(Objects.requireNonNull(data), Objects.requireNonNull(charset), data.getBytes(charset)); // copy!
    }

    private StringBinData(String data, Charset charset, byte[] dataBytes) {
        super(dataBytes.length);
        this.data = data;
        this.charset = charset;
        this.dataBytes = dataBytes;
        this.offset = 0;
    }

    private StringBinData(String data, Charset charset, byte[] dataBytes, int offset, int len) {
        super(len);
        this.data = data;
        this.charset = charset;
        this.dataBytes = dataBytes;
        this.offset = offset;
    }

    public Charset charset() {
        return this.charset;
    }

    @Override
    public String unwrap() {
        ensureUnconsumed();
        return this.data;
    }

    @Override
    public StringBinData intoString(Charset charset) {
        if (this.charset == charset) {
            return this;
        }
        ensureUnconsumed();
        return new StringBinData(this.data, charset); // copy!
    }

    @Override
    public BytesBinData intoBytes() {
        ensureUnconsumed();
        return new BytesBinData(this.dataBytes, this.offset, (int) len());
    }

    @Override
    public ByteBufferBinData intoByteBuffer() {
        ensureUnconsumed();
        return new ByteBufferBinData(ByteBuffer.wrap(this.dataBytes, this.offset, (int) len()));
    }

    @Override
    public InputStreamBinData intoInputStream() {
        ensureUnconsumed();
        return new InputStreamBinData(new ByteArrayInputStream(this.dataBytes, this.offset, (int) len()));
    }

    @Override
    public PublisherBinData intoPublisher() {
        ensureUnconsumed();
        return new PublisherBinData(
                HttpRequest.BodyPublishers.ofByteArray(this.dataBytes, this.offset, (int) len()), (int) len());
    }
}
