package lost.pikpak.client.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Flow;

public class ByteUtil {

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
                var read = in.read(buffer);
                if (read == -1) break;
                out.write(buffer, 0, read);
            }
        }
    }

    /**
     * check the bytebuffer whether is full array slice
     *
     * @param buffer the bytebuffer
     * @return true if the bytebuffer is full array slice
     */
    public static boolean fullArraySlice(ByteBuffer buffer) {
        Objects.requireNonNull(buffer);
        return buffer.hasArray() && buffer.array().length == buffer.remaining();
    }

    /**
     * get byte array from the bytebuffer for read only usage
     *
     * @param buffer the bytebuffer
     * @return byte array for read only usage
     */
    public static byte[] getByteArrayForReadOnly(ByteBuffer buffer) {
        Objects.requireNonNull(buffer);
        byte[] byteArrayForReadOnly;
        if (fullArraySlice(buffer)) { // not copy, use internal array
            byteArrayForReadOnly = buffer.array();
        } else { // copy
            int len = buffer.remaining();
            if (len > 0) {
                byteArrayForReadOnly = new byte[len];
                buffer.get(byteArrayForReadOnly);
            } else {
                byteArrayForReadOnly = new byte[0];
            }
        }
        return byteArrayForReadOnly;
    }

    /**
     * subscribe `publisher` and collect items into {@link OutputStream}
     *
     * @param publisher    the publisher to be subscribed
     * @param outputStream the output stream, it will be close
     * @throws IOException
     */
    public static void collectIntoStream(Flow.Publisher<ByteBuffer> publisher, OutputStream outputStream)
            throws IOException {
        Objects.requireNonNull(publisher);
        Objects.requireNonNull(outputStream);
        try (var out = outputStream) {
            out.write(collectIntoBytes(publisher));
        }
    }

    /**
     * subscribe `publisher` and collect items into String
     *
     * @param publisher the publisher to be subscribed
     * @return the result collected by subscriber subscribes `publisher`
     */
    public static String collectIntoString(Flow.Publisher<ByteBuffer> publisher) {
        var bytes = collectIntoBytes(publisher);
        return bytes.length == 0 ? "" : new String(bytes);
    }

    /**
     * subscribe `publisher` and collect items into byte array
     *
     * @param publisher the publisher to be subscribed
     * @return the result collected by subscriber subscribes `publisher`
     */
    public static byte[] collectIntoBytes(Flow.Publisher<ByteBuffer> publisher) {
        Objects.requireNonNull(publisher);
        var agg = new AggSubscriber<ByteBuffer>();
        publisher.subscribe(agg);
        var buffers = agg.blockingGet();
        return collectIntoBytes(buffers);
    }

    /**
     * copy bytes into byte array from {@code List<ByteBuffer>}
     *
     * @param buffers the buffers
     * @return byte array
     */
    public static byte[] collectIntoBytes(List<ByteBuffer> buffers) {
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
}
