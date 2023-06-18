package lost.pikpak.client.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

public class AggSubscriber<T> implements Flow.Subscriber<T> {
    private final CompletableFuture<List<T>> result =
        new CompletableFuture<>();
    private final List<T> buffers = new ArrayList<>();
    private Flow.Subscription subscription;

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
        buffers.add(item);
        this.subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {
        buffers.clear();
        result.completeExceptionally(throwable);
    }

    @Override
    public void onComplete() {
        result.complete(buffers);
    }
}
