package lost.pikpak.client.bindata;

import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.Flow;
import lost.pikpak.client.bindata.util.AggSubscriber;
import lost.pikpak.client.bindata.util.SubscriberInputStream;

public class PublisherBinData extends BinDataBase<Flow.Publisher<ByteBuffer>> {
    private final Flow.Publisher<ByteBuffer> data;

    public PublisherBinData(Flow.Publisher<ByteBuffer> data, long len) {
        super(len);
        this.data = Objects.requireNonNull(data);
    }

    public PublisherBinData(HttpRequest.BodyPublisher data) {
        this(data, data.contentLength());
    }

    @Override
    public Flow.Publisher<ByteBuffer> unwrap() {
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
            return new BytesBinData(AggSubscriber.collectBytes(this.data)); // copy!
        } finally {
            makeConsumed();
        }
    }

    @Override
    public ByteBufferBinData intoByteBuffer() {
        ensureUnconsumed();
        try {
            return new ByteBufferBinData(ByteBuffer.wrap(AggSubscriber.collectBytes(
                    this.data))); // copy! TODO optimize, but we can not impl CompositeByteBuffer extends ByteBuffer
        } finally {
            makeConsumed();
        }
    }

    @Override
    public InputStreamBinData intoInputStream() {
        ensureUnconsumed();
        try {
            var is = new SubscriberInputStream();
            this.data.subscribe(is); // copy!
            return new InputStreamBinData(is, len());
        } finally {
            makeConsumed();
        }
    }

    @Override
    public PublisherBinData intoPublisher() {
        return this;
    }
}
