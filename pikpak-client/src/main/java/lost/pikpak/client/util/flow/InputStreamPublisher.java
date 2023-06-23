package lost.pikpak.client.util.flow;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Flow;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public final class InputStreamPublisher implements Flow.Publisher<List<ByteBuffer>> {
    private final Supplier<InputStream> is;
    private final int bufferSize;

    public InputStreamPublisher(Supplier<InputStream> inputStream) {
        this(inputStream, 0);
    }

    public InputStreamPublisher(Supplier<InputStream> inputStream, int bufferSize) {
        this.is = Objects.requireNonNull(inputStream);
        this.bufferSize = Math.max(bufferSize, 8 * 1024);
    }

    @Override
    public void subscribe(Flow.Subscriber<? super List<ByteBuffer>> subscriber) {
        Objects.requireNonNull(subscriber);
        var is = Objects.requireNonNull(this.is.get());
        subscriber.onSubscribe(new Sub(is, this.bufferSize, subscriber));
    }

    static final class Sub implements Flow.Subscription {
        private final InputStream is;
        private final Flow.Subscriber<? super List<ByteBuffer>> subscriber;
        private final int bufferSize;
        /**
         * control: ensure request(..) thread safe
         */
        private final Lock lock = new ReentrantLock();

        private volatile boolean complete = false;
        private volatile Throwable error;

        Sub(InputStream is, int bufferSize, Flow.Subscriber<? super List<ByteBuffer>> subscriber) {
            this.is = is;
            this.bufferSize = bufferSize;
            this.subscriber = subscriber;
        }

        private boolean end() {
            return complete | error != null;
        }

        @Override
        public void request(long n) {
            if (end()) return;
            var lock = this.lock;
            lock.lock();
            try {
                if (end()) return;
                for (; n > 0; n--) {
                    var bytes = new byte[bufferSize];
                    var read = this.is.read(bytes); // Blocking! TODO run as an async task
                    if (read == -1) {
                        close();
                        this.complete = true;
                        this.subscriber.onComplete();
                        break;
                    } else {
                        var item = List.of(ByteBuffer.wrap(bytes, 0, read));
                        this.subscriber.onNext(item);
                    }
                }
            } catch (Exception e) {
                close();
                this.error = e;
                this.subscriber.onError(e);
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void cancel() {
            if (end()) return;
            var l = this.lock;
            l.lock();
            try {
                if (end()) return;
                this.close();
                var err = new CancellationException();
                this.error = err;
                this.subscriber.onError(err);
            } finally {
                l.unlock();
            }
        }

        private void close() {
            try {
                this.is.close();
            } catch (IOException ignore) {
            }
        }
    }
}
