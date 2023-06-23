package lost.pikpak.client.util.flow;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class SubscriberInputStream extends InputStream implements Flow.Subscriber<ByteBuffer> {
    private static final ByteBuffer LOADING = ByteBuffer.wrap(new byte[0]);
    private final CountDownLatch subLatch = new CountDownLatch(1);
    private final Lock dataLoadLock = new ReentrantLock();
    private final Condition dataLoadCond = dataLoadLock.newCondition();
    private ByteBuffer buffer;
    private volatile Flow.Subscription subscription;
    private volatile boolean complete = false;
    private volatile Throwable error;
    private volatile ByteBuffer dataLoaded = LOADING;

    @Override
    public void close() {
        this.subscription.cancel();
        this.subscription = null;
        this.dataLoaded = null;
        this.buffer = null;
    }

    @Override
    public int read() throws IOException {
        var b = this.buffer;
        if (b != null) {
            assert b.hasRemaining();
            var res = b.get() & 0xff;
            if (!b.hasRemaining()) {
                this.buffer = null;
            }
            return res;
        }
        //  load more data
        if (this.subscription == null) {
            try {
                this.subLatch.await();
            } catch (Exception e) {
                throw new IOException("get subscription error", e);
            }
        }
        for (; ; ) {
            var lock = this.dataLoadLock;
            lock.lock();
            try {
                if (this.error != null) {
                    throw new IOException("load more data error, got an error", this.error);
                }
                if (this.complete) {
                    return -1;
                }

                this.subscription.request(1);

                var data = this.dataLoaded; // when sync request

                if (!end() && data == LOADING) {
                    this.dataLoadCond.await(); // when async request
                    data = this.dataLoaded;
                }
                this.dataLoaded = LOADING;

                if (data == null || !data.hasRemaining()) {
                    continue;
                }

                // got data
                var res = data.get() & 0xff;
                if (data.hasRemaining()) {
                    this.buffer = data;
                }
                return res;
            } catch (InterruptedException e) {
                throw new IOException("load more data is interrupted", e);
            } finally {
                lock.unlock();
            }
        }
    }

    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = Objects.requireNonNull(subscription);
        this.subLatch.countDown();
    }

    private boolean end() {
        return this.error != null || this.complete;
    }

    @Override
    public void onNext(ByteBuffer item) {
        if (end()) return;
        var lock = this.dataLoadLock;
        lock.lock();
        try {
            if (end()) return;
            this.dataLoaded = item;
            this.dataLoadCond.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void onError(Throwable throwable) {
        var lock = this.dataLoadLock;
        lock.lock();
        try {
            this.error = throwable;
            this.dataLoadCond.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void onComplete() {
        var lock = this.dataLoadLock;
        lock.lock();
        try {
            this.complete = true;
            this.dataLoadCond.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
