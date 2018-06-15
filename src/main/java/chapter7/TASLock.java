package chapter7;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Test and set lock.
 *
 * @author skywalker
 */
public class TASLock {

    private AtomicBoolean state = new AtomicBoolean(false);

    public void lock() {
        while (state.getAndSet(true));
    }

    public void unlock() {
        state.set(false);
    }

}
