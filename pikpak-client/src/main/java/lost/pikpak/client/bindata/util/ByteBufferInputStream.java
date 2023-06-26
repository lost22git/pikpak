package lost.pikpak.client.bindata.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.Objects;

public final class ByteBufferInputStream extends InputStream {
    private ByteBuffer data;

    public ByteBufferInputStream(ByteBuffer data) {
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public int read() throws IOException {
        try {
            return this.data.get(); // copy!
        } catch (BufferOverflowException | NullPointerException e) {
            return -1;
        }
    }

    @Override
    public int available() throws IOException {
        return this.data.remaining();
    }

    @Override
    public void close() throws IOException {
        this.data = null;
    }
}
