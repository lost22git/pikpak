package lost.pikpak.client.bindata;

import java.io.ByteArrayInputStream;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.ByteBuffer;
import java.util.Objects;

public final class BytesBinData extends BinDataBase<byte[]> {
    private final byte[] data;
    private final int offset;

    public BytesBinData(byte[] data) {
        this(Objects.requireNonNull(data), 0, data.length);
    }

    public BytesBinData(byte[] data, int offset, int len) {
        super(len);
        if (offset < 0) {
            throw new ArrayIndexOutOfBoundsException("'offset' must be >= 0");
        }
        if (len < 0) {
            throw new ArrayIndexOutOfBoundsException("'len' must be >=0");
        }
        this.data = Objects.requireNonNull(data);
        if (offset + len > data.length) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format("offset(%d) + len(%d) > data.length(%d)", offset, len, data.length));
        }
        this.offset = offset;
    }

    @Override
    public byte[] unwrap() {
        ensureUnconsumed();
        return this.data;
    }

    public int offset() {
        return this.offset;
    }

    @Override
    public BytesBinData intoBytes() {
        ensureUnconsumed();
        return this;
    }

    @Override
    public ByteBufferBinData intoByteBuffer() {
        ensureUnconsumed();
        return new ByteBufferBinData(ByteBuffer.wrap(this.data, this.offset, (int) len()));
    }

    @Override
    public InputStreamBinData intoInputStream() {
        ensureUnconsumed();
        return new InputStreamBinData(new ByteArrayInputStream(this.data, this.offset, (int) len()));
    }

    @Override
    public PublisherBinData intoPublisher() {
        ensureUnconsumed();
        return new PublisherBinData(BodyPublishers.ofByteArray(this.data, this.offset, (int) len()), (int) len());
    }
}
