package lost.pikpak.client.bindata;

import java.nio.ByteBuffer;
import java.util.Objects;
import lost.pikpak.client.bindata.util.ByteBufferInputStream;
import lost.pikpak.client.bindata.util.ByteBufferPublisher;

public final class ByteBufferBinData extends BinDataBase<ByteBuffer> {
    private final ByteBuffer data;

    public ByteBufferBinData(ByteBuffer data) {
        super(Objects.requireNonNull(data).remaining());
        this.data = data;
    }

    @Override
    public ByteBuffer unwrap() {
        ensureUnconsumed();
        try {
            return this.data;
        } finally {
            makeConsumed();
        }
    }

    @Override
    public BytesBinData intoBytes() {
        ensureUnconsumed();
        var data = this.data;
        if (data.hasArray()) {
            return new BytesBinData(data.array(), data.position(), (int) len());
        } else {
            data = data.asReadOnlyBuffer();
            var dest = new byte[(int) len()];
            data.get(dest); // copy!
            return new BytesBinData(dest);
        }
    }

    @Override
    public ByteBufferBinData intoByteBuffer() {
        return this;
    }

    @Override
    public InputStreamBinData intoInputStream() {
        ensureUnconsumed();
        return new InputStreamBinData(new ByteBufferInputStream(this.data.asReadOnlyBuffer()));
    }

    @Override
    public PublisherBinData intoPublisher() {
        ensureUnconsumed();
        return new PublisherBinData(new ByteBufferPublisher(this.data.asReadOnlyBuffer()), len());
    }
}
