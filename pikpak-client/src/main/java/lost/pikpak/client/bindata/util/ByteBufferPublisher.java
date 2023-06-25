package lost.pikpak.client.bindata.util;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class ByteBufferPublisher implements Flow.Publisher<ByteBuffer> {
    private final ByteBuffer data;
    private final AtomicBoolean subscribed = new AtomicBoolean(false);

    public ByteBufferPublisher(ByteBuffer data) {
        this.data = Objects.requireNonNull(data).asReadOnlyBuffer();
    }

    @Override
    public void subscribe(Flow.Subscriber<? super ByteBuffer> subscriber) {
        Objects.requireNonNull(subscriber);
        if (this.subscribed.compareAndSet(false, true)) {
            var sub = new Sub(subscriber, data);
            subscriber.onSubscribe(sub);
        } else {
            throw new IllegalStateException(this.getClass().getSimpleName() + " can only be subscribed once");
        }
    }

    private static final class Sub implements Flow.Subscription {
        private final Flow.Subscriber<? super ByteBuffer> subscriber;
        private ByteBuffer data;
        private volatile boolean complete;
        private volatile Throwable error;
        private final Lock lock = new ReentrantLock();

        private Sub(Flow.Subscriber<? super ByteBuffer> subscriber, ByteBuffer data) {
            this.subscriber = subscriber;
            this.data = Objects.requireNonNull(data);
        }

        private boolean end() {
            return this.complete || this.error != null;
        }

        private void close() {
            this.data = null;
        }

        @Override
        public void request(long n) {
            if (end()) return;
            var l = this.lock;
            l.lock();
            try {
                if (end()) return;
                this.subscriber.onNext(this.data);
                this.close();
                this.subscriber.onComplete();
                this.complete = true;
            } catch (Exception e) {
                close();
                this.error = e;
                this.subscriber.onError(e);
            } finally {
                l.unlock();
            }
        }

        @Override
        public void cancel() {
            if (end()) return;
            var l = this.lock;
            l.lock();
            try {
                if (end()) return;
                close();
                var err = this.error = new CancellationException();
                this.subscriber.onError(err);
            } finally {
                l.unlock();
            }
        }
    }
}
