package lost.pikpak.client.bindata.util;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AggSubscriber<T> implements Flow.Subscriber<T> {
    private final CompletableFuture<List<T>> result = new CompletableFuture<>();
    private final List<T> buffers = new ArrayList<>();
    private final Lock lock = new ReentrantLock();
    private Flow.Subscription subscription;
    private volatile boolean complete = false;
    private volatile Throwable error;

    public static String collectString(Flow.Publisher<ByteBuffer> publisher) {
        return new String(collectBytes(publisher), UTF_8);
    }

    public static byte[] collectBytes(Flow.Publisher<ByteBuffer> publisher) {
        Objects.requireNonNull(publisher);
        var agg = new AggSubscriber<ByteBuffer>();
        publisher.subscribe(agg);
        var buffers = agg.get();
        return ByteUtil.collectBytes(buffers);
    }

    public List<T> get() {
        return this.result.join();
    }

    public CompletionStage<List<T>> getAsync() {
        CompletableFuture<List<T>> res = new CompletableFuture<>();
        this.result.whenComplete((v, e) -> {
            if (e != null) {
                res.completeExceptionally(e);
            } else {
                res.complete(v);
            }
        });
        return res;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = Objects.requireNonNull(subscription);
        this.subscription.request(1);
    }

    @Override
    public void onNext(T item) {
        if (end()) return;
        var l = this.lock;
        l.lock();
        try {
            if (end()) return;
            this.buffers.add(item);
            this.subscription.request(1);
        } finally {
            l.unlock();
        }
    }

    @Override
    public void onError(Throwable throwable) {
        if (end()) return;
        var l = this.lock;
        l.lock();
        try {
            if (end()) return;
            this.error = throwable;
            this.buffers.clear();
            this.result.completeExceptionally(throwable);
        } finally {
            l.unlock();
        }
    }

    @Override
    public void onComplete() {
        if (end()) return;
        var l = this.lock;
        l.lock();
        try {
            if (end()) return;
            this.complete = true;
            this.result.complete(this.buffers);
        } finally {
            l.unlock();
        }
    }

    private boolean end() {
        return complete || error != null;
    }
}
