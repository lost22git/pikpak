package lost.pikpak.client.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AggSubscriber<T> implements Flow.Subscriber<T> {
    private final CompletableFuture<List<T>> result =
        new CompletableFuture<>();
    private final List<T> buffers = new ArrayList<>();
    private final Lock lock = new ReentrantLock();
    private Flow.Subscription subscription;
    private volatile boolean end = false;

    public List<T> blockingGet() {
        return this.result.join();
    }

    public CompletionStage<List<T>> asyncGet() {
        CompletableFuture<List<T>> res = new CompletableFuture<>();
        this.result
            .whenComplete((v, e) -> {
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
        this.subscription = subscription;
        this.subscription.request(1);
    }

    @Override
    public void onNext(T item) {
        var l = this.lock;
        l.lock();
        try {
            if (!this.end) {
                this.buffers.add(item);
                this.subscription.request(1);
            }
        } finally {
            l.unlock();
        }
    }

    @Override
    public void onError(Throwable throwable) {
        var l = this.lock;
        l.lock();
        try {
            if (!this.end) {
                this.end = true;
                this.buffers.clear();
                this.result.completeExceptionally(throwable);
            }
        } finally {
            l.unlock();
        }
    }

    @Override
    public void onComplete() {
        var l = this.lock;
        l.lock();
        try {
            if (!this.end) {
                this.end = true;
                this.result.complete(this.buffers);
            }
        } finally {
            l.unlock();
        }
    }
}
