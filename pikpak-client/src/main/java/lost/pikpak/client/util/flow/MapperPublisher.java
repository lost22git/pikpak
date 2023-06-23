package lost.pikpak.client.util.flow;

import java.util.Objects;
import java.util.concurrent.Flow;
import java.util.function.Function;

public final class MapperPublisher<S, T> implements Flow.Publisher<T> {
    private final Flow.Publisher<S> source;
    private final Function<S, T> mapper;

    public MapperPublisher(Flow.Publisher<S> source, Function<S, T> mapper) {
        this.source = Objects.requireNonNull(source);
        this.mapper = Objects.requireNonNull(mapper);
    }

    @Override
    public void subscribe(Flow.Subscriber<? super T> subscriber) {
        var p = new Processor(mapper);
        this.source.subscribe(p);
        p.subscribe(subscriber);
    }

    final class Processor implements Flow.Processor<S, T> {
        private final Function<S, T> mapper;
        private volatile Flow.Subscription up;
        private volatile Flow.Subscriber<? super T> down;
        private volatile boolean complete;
        private volatile Throwable error;

        public Processor(Function<S, T> mapper) {
            this.mapper = mapper;
        }

        private boolean end() {
            return complete || error != null;
        }

        @Override
        public void subscribe(Flow.Subscriber<? super T> subscriber) {
            this.down = subscriber;
            subscriber.onSubscribe(new Flow.Subscription() {
                @Override
                public void request(long n) {
                    if (end()) return;
                    Processor.this.up.request(n);
                }

                @Override
                public void cancel() {
                    if (end()) return;
                    Processor.this.up.cancel();
                }
            });
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.up = subscription;
        }

        @Override
        public void onNext(S item) {
            if (end()) return;
            var downNext = this.mapper.apply(item);
            this.down.onNext(downNext);
        }

        @Override
        public void onError(Throwable throwable) {
            if (end()) return;
            this.error = throwable;
            this.down.onError(throwable);
        }

        @Override
        public void onComplete() {
            if (end()) return;
            this.complete = true;
            this.down.onComplete();
        }
    }
}
