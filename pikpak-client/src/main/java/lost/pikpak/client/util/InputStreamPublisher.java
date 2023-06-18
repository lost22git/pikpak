package lost.pikpak.client.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Flow;

public final class InputStreamPublisher implements Flow.Publisher<List<ByteBuffer>> {
    private final InputStream is;
    private final int bufferSize;

    public InputStreamPublisher(InputStream inputStream) {
        this(inputStream, 0);
    }

    public InputStreamPublisher(InputStream inputStream,
                                int bufferSize) {
        this.is = Objects.requireNonNull(inputStream);
        this.bufferSize = Math.max(bufferSize, 8 * 1024);
    }

    @Override
    public void subscribe(Flow.Subscriber<? super List<ByteBuffer>> subscriber) {
        subscriber.onSubscribe(new Sub(this.is, this.bufferSize, subscriber));
    }

    final static class Sub implements Flow.Subscription {
        private final InputStream is;
        private final Flow.Subscriber<? super List<ByteBuffer>> subscriber;

        private byte[] buffer;

        Sub(InputStream is,
            int bufferSize,
            Flow.Subscriber<? super List<ByteBuffer>> subscriber) {
            this.is = is;
            this.buffer = new byte[bufferSize];
            this.subscriber = subscriber;
        }

        @Override
        public void request(long n) {
            try {
                for (; n > 0; n--) {
                    var read = this.is.read(this.buffer); // Blocking! TODO run as a async task
                    if (read == -1) {
                        close();
                        this.subscriber.onComplete();
                        break;
                    } else {
                        var target = new byte[read];
                        System.arraycopy(this.buffer, 0, target, 0, read);
                        var item = List.of(ByteBuffer.wrap(target));
                        this.subscriber.onNext(item);
                    }
                }
            } catch (IOException e) {
                close();
                this.subscriber.onError(e);
            }
        }

        @Override
        public void cancel() {
            this.close();
        }

        private void close() {
            this.buffer = null;
            try {
                this.is.close();
            } catch (IOException ignore) {
            }
        }
    }
}
