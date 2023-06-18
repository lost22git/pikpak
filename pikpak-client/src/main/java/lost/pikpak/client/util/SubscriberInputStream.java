package lost.pikpak.client.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow;


public class SubscriberInputStream extends InputStream
    implements Flow.Subscriber<ByteBuffer> {
    private Node pendingData;
    private Flow.Subscription subscription;
    private CompletableFuture<ByteBuffer> dataLoadFut;
    private volatile boolean end = false;

    @Override
    public int read() throws IOException {
        int res = -1;
        Node cur = this.pendingData;
        for (; cur != null; cur = cur.next()) {
            var buffer = cur.value();
            if (buffer.hasRemaining()) {
                res = buffer.get() & 0xFF;
                break;
            }
        }
        this.pendingData = cur;
        if (res != -1) {
            if (!cur.value().hasRemaining()) {
                this.pendingData = cur.next();
            }
            return res;
        }
        // pendingData is null, need load more data
        for (; ; ) {
            if (this.end) {
                return -1;
            }
            this.dataLoadFut = new CompletableFuture<>();
            this.subscription.request(1);
            try {
                var data = this.dataLoadFut.get();
                if (data == null) {
                    continue;
                }
                if (!data.hasRemaining()) {
                    continue;
                }
                // got more data
                this.pendingData = new Node(null, data);
                return data.get() & 0xFF;
            } catch (ExecutionException e) {
                throw new IOException("load more data error, got an error", e.getCause());
            } catch (InterruptedException e) {
                throw new IOException("load more data was interrupted", e);
            }
        }
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
    }

    @Override
    public void onNext(ByteBuffer item) {
        this.dataLoadFut.complete(item);
    }

    @Override
    public void onError(Throwable throwable) {
        this.dataLoadFut.completeExceptionally(throwable);
    }

    @Override
    public void onComplete() {
        this.end = true;
        this.dataLoadFut.complete(null);
    }

    record Node(Node next, ByteBuffer value) {
    }

}
