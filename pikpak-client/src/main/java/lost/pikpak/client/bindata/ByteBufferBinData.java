package lost.pikpak.client.bindata;

import java.nio.ByteBuffer;
import java.util.Objects;
import lost.pikpak.client.bindata.util.ByteBufferInputStream;
import lost.pikpak.client.bindata.util.ByteBufferPublisher;

public final class ByteBufferBinData extends BinDataBase<ByteBuffer> {
    private final ByteBuffer data;

    public ByteBufferBinData(ByteBuffer data) {
        super(Objects.requireNonNull(data).remaining());
        this.data = data.asReadOnlyBuffer();
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
        try {
            var data = this.data;
            if (data.hasArray()) {
                return new BytesBinData(data.array(), data.position(), (int) len());
            } else {
                var dest = new byte[(int) len()];
                data.get(dest); // copy!
                return new BytesBinData(dest);
            }
        } finally {
            makeConsumed();
        }
    }

    @Override
    public ByteBufferBinData intoByteBuffer() {
        return this;
    }

    @Override
    public InputStreamBinData intoInputStream() {
        ensureUnconsumed();
        try {
            return new InputStreamBinData(new ByteBufferInputStream(this.data));
        } finally {
            makeConsumed();
        }
    }

    @Override
    public PublisherBinData intoPublisher() {
        return new PublisherBinData(new ByteBufferPublisher(this.data), len());
    }
}
