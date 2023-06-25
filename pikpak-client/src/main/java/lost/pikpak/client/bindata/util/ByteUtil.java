package lost.pikpak.client.bindata.util;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;

public class ByteUtil {

    private static final VarHandle VH_FILTER_INPUT_STREAM_IN;
    private static final VarHandle VH_BYTEARRAY_INPUT_STREAM_BUF;
    private static final VarHandle VH_BYTEARRAY_INPUT_STREAM_POS;
    private static final VarHandle VH_BYTEBUFFER_INPUT_STREAM_DATA;

    static {
        var lookup = MethodHandles.lookup();
        try {
            VH_FILTER_INPUT_STREAM_IN = MethodHandles.privateLookupIn(FilterInputStream.class, lookup)
                    .findVarHandle(FilterInputStream.class, "in", InputStream.class);
            VH_BYTEARRAY_INPUT_STREAM_BUF = MethodHandles.privateLookupIn(ByteArrayInputStream.class, lookup)
                    .findVarHandle(ByteArrayInputStream.class, "buf", byte[].class);
            VH_BYTEARRAY_INPUT_STREAM_POS = MethodHandles.privateLookupIn(ByteArrayInputStream.class, lookup)
                    .findVarHandle(ByteArrayInputStream.class, "pos", int.class);
            VH_BYTEBUFFER_INPUT_STREAM_DATA = MethodHandles.privateLookupIn(ByteBufferInputStream.class, lookup)
                    .findVarHandle(ByteBufferInputStream.class, "data", ByteBuffer.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * copy bytes from inputStream to outputStream,
     * and auto close them when error or complete
     *
     * @param inputStream  the inputStream
     * @param outputStream the outputStream
     * @param bufferSize   buffer size, fallback to 8k if value < 8k
     * @throws IOException the error
     */
    public static void ioCopy(InputStream inputStream, OutputStream outputStream, int bufferSize) throws IOException {
        Objects.requireNonNull(inputStream);
        Objects.requireNonNull(outputStream);
        try (var in = inputStream;
                var out = outputStream) {
            var size = Math.max(bufferSize, 8 * 1024);
            var buffer = new byte[size];
            for (; ; ) {
                var read = in.read(buffer); // copy!
                if (read == -1) break;
                out.write(buffer, 0, read); // copy!
            }
        }
    }

    /**
     * copy bytes into byte array from {@code List<ByteBuffer>}
     *
     * @param buffers the buffers
     * @return byte array
     */
    public static byte[] collectBytes(List<ByteBuffer> buffers) {
        Objects.requireNonNull(buffers);
        int size = 0;
        for (ByteBuffer bb : buffers) {
            size += bb.remaining();
        }
        byte[] bytes = new byte[size];
        int start = 0;
        for (ByteBuffer bb : buffers) {
            var remaining = bb.remaining();
            bb.get(bytes, start, remaining); // Copy!
            start += remaining;
        }
        return bytes;
    }

    public static ByteBuffer getByteBufferForReadOnly(InputStream inputStream) {
        try (var is = inputStream) {
            // ByteArrayInputStream resolved
            var bis = reflectResolveByteArrayInputStream(is);
            if (bis != null) {
                var len = bis.available();
                int pos = reflectGetPos(bis);
                var buf = reflectGetBuf(bis);
                return ByteBuffer.wrap(buf, pos, len);
            }
            // ByteBufferInputStream resolved
            var bbis = reflectResolveByteBufferInputStream(is);
            if (bbis != null) {
                return reflectGetData(bbis);
            }
            return ByteBuffer.wrap(is.readAllBytes()); // copy!
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static ByteArrayInputStream reflectResolveByteArrayInputStream(InputStream inputStream) {
        if (inputStream instanceof ByteArrayInputStream bis) {
            return bis;
        } else if (inputStream instanceof FilterInputStream fis) {
            var inner_in = reflectGetIn(fis);
            return reflectResolveByteArrayInputStream(inner_in);
        } else {
            return null;
        }
    }

    public static InputStream reflectGetIn(FilterInputStream fis) {
        return (InputStream) VH_FILTER_INPUT_STREAM_IN.get(fis);
    }

    public static int reflectGetPos(ByteArrayInputStream bis) {
        return (int) VH_BYTEARRAY_INPUT_STREAM_POS.get(bis);
    }

    public static byte[] reflectGetBuf(ByteArrayInputStream bis) {
        return (byte[]) VH_BYTEARRAY_INPUT_STREAM_BUF.get(bis);
    }

    public static ByteBufferInputStream reflectResolveByteBufferInputStream(InputStream inputStream) {
        if (inputStream instanceof ByteBufferInputStream bis) {
            return bis;
        } else if (inputStream instanceof FilterInputStream fis) {
            var inner_in = reflectGetIn(fis);
            return reflectResolveByteBufferInputStream(inner_in);
        } else {
            return null;
        }
    }

    public static ByteBuffer reflectGetData(ByteBufferInputStream bis) {
        return (ByteBuffer) VH_BYTEBUFFER_INPUT_STREAM_DATA.get(bis);
    }
}
