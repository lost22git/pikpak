package lost.pikpak.client.bindata;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.http.HttpRequest;
import java.util.Objects;
import lost.pikpak.client.bindata.util.ByteUtil;

public final class InputStreamBinData extends BinDataBase<InputStream> {
    private final InputStream data;

    public InputStreamBinData(InputStream data) {
        super(getLength(Objects.requireNonNull(data)));
        this.data = data;
    }

    public InputStreamBinData(InputStream data, long len) {
        super(len);
        this.data = data;
    }

    private static long getLength(InputStream inputStream) {
        try {
            return inputStream.available();
        } catch (IOException ignore) {
            return UNKNOWN_LEN;
        }
    }

    @Override
    public InputStream unwrap() {
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
        try (var is = this.data) {
            var buffer = ByteUtil.getByteBufferForReadOnly(is);
            if (buffer.hasArray()) {
                return new BytesBinData(buffer.array(), buffer.position(), buffer.remaining());
            } else { // is == ByteBufferInputStream with DirectByteBuffer
                var bytes = new byte[buffer.remaining()];
                buffer.get(bytes); // copy!
                return new BytesBinData(bytes);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            makeConsumed();
        }
    }

    @Override
    public ByteBufferBinData intoByteBuffer() {
        ensureUnconsumed();
        try (var is = this.data) {
            var buffer = ByteUtil.getByteBufferForReadOnly(is);
            return new ByteBufferBinData(buffer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            makeConsumed();
        }
    }

    @Override
    public InputStreamBinData intoInputStream() {
        return this;
    }

    @Override
    public PublisherBinData intoPublisher() {
        ensureUnconsumed();
        try {
            return new PublisherBinData(HttpRequest.BodyPublishers.ofInputStream(() -> this.data), (int) len());
        } finally {
            makeConsumed();
        }
    }
}
