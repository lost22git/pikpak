import lost.pikpak.client.util.SubscriberInputStream;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class SubscriberInputStreamTest {

    @Test
    void async_request() throws IOException {
        for (int i = 0; i < 1000; i++) {
            try (
                var pub = new SubmissionPublisher<ByteBuffer>();
                var sub = new SubscriberInputStream()) {
                pub.subscribe(sub);
                pub.submit(ByteBuffer.wrap("hello".getBytes()));
                pub.submit(ByteBuffer.wrap(" ".getBytes()));
                pub.submit(ByteBuffer.wrap("world".getBytes()));
                pub.submit(ByteBuffer.wrap("!".getBytes()));
                pub.close(); // complete
                var s = new String(sub.readAllBytes());
                assertThat(s).isEqualTo("hello world!");
            }
        }
    }

    @Test
    void async_request_and_error() {
        for (int i = 0; i < 1000; i++) {
            try (
                var pub = new SubmissionPublisher<ByteBuffer>();
                var sub = new SubscriberInputStream()) {
                pub.subscribe(sub);
                pub.submit(ByteBuffer.wrap("hello".getBytes()));
                pub.submit(ByteBuffer.wrap(" ".getBytes()));
                pub.submit(ByteBuffer.wrap("world".getBytes()));
                pub.submit(ByteBuffer.wrap("!".getBytes()));
                pub.closeExceptionally(new IOException("test error")); // error
                var s = new String(sub.readAllBytes());
                fail("should not reach here");
            } catch (Exception e) {
                assertThat(e).cause().hasMessage("test error");
            }
        }
    }

    @Test
    void sync_request() throws Exception {
        for (int i = 0; i < 1000; i++) {
            var pub = HttpRequest.BodyPublishers.ofByteArrays(
                List.of(
                    "hello".getBytes(),
                    " ".getBytes(),
                    "world".getBytes(),
                    "!".getBytes()
                ));
            var sub = new SubscriberInputStream();
            pub.subscribe(sub);
            var s = new String(sub.readAllBytes());
            assertThat(s).isEqualTo("hello world!");
        }
    }

    @Test
    void sync_request_and_error() {
        for (int i = 0; i < 1000; i++) {
            try {
                var pub = new ErrorPublisher();
                var sub = new SubscriberInputStream();
                pub.subscribe(sub);
                var bytes = sub.readAllBytes();
                var s = new String(bytes);
                fail("should not reach here");
            } catch (Exception e) {
                assertThat(e).cause().hasMessage("test error");
            }
        }
    }

    static class ErrorPublisher implements Flow.Publisher<ByteBuffer> {
        @Override
        public void subscribe(Flow.Subscriber<? super ByteBuffer> subscriber) {
            subscriber.onSubscribe(new Flow.Subscription() {
                final ArrayDeque<Object> queue = new ArrayDeque<>(
                    List.of(
                        ByteBuffer.wrap("hello".getBytes()),
                        ByteBuffer.wrap(" ".getBytes()),
                        ByteBuffer.wrap("world".getBytes()),
                        new IOException("test error"),
                        ByteBuffer.wrap("!".getBytes())
                    )
                );

                @Override
                public void request(long n) {
                    var o = this.queue.pollFirst();
                    switch (o) {
                        case null -> subscriber.onComplete();
                        case Exception e -> subscriber.onError(e);
                        default -> subscriber.onNext((ByteBuffer) o);
                    }
                }

                @Override
                public void cancel() {

                }
            });
        }
    }


}

