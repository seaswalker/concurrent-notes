package chapter7;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@link TASLock}的改进，test test and set lock.
 *
 * @author skywalker
 */
public class TTASLock {

    private AtomicBoolean state = new AtomicBoolean(false);

    public void lock() {
        while (true) {
            while (state.get());

            if (!state.getAndSet(true)) {
                // locked
                break;
            }
        }
    }

    public void unlock() {
        state.set(false);
    }

}
