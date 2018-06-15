package chapter7.backoff;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 指数后退锁.
 *
 * @author skywalker
 */
public class BackoffLock {

    private final AtomicBoolean state = new AtomicBoolean(false);

    public void lock() {

        Backoff backoff = new Backoff(10, 1000);

        while (true) {
            while (state.get());

            if (!state.getAndSet(true)) {
                // locked
                break;
            }

            // 锁争用失败
            try {
                backoff.backoff();
            } catch (InterruptedException e) {
                // It's ok.
            }
        }
    }

    public void unlock() {
        state.set(false);
    }

}
