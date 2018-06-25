package chapter11;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * 无锁交换机.
 *
 * @author skywalker
 */
public class LockFreeExchanger<T> {

    private static final int EMPTY = 0, WAITING = 1, BUSY = 2;

    private final AtomicStampedReference<T> slot = new AtomicStampedReference<>(null, EMPTY);

    /**
     * 交换.
     *
     * @return 从其它线程交换来的数据
     */
    public T exchange(T myItem, long timeout, TimeUnit timeUnit) throws TimeoutException {
        long timeBound = (System.currentTimeMillis() + timeUnit.toMillis(timeout));
        int[] stamp = {EMPTY};

        while (true) {
            if (System.currentTimeMillis() > timeBound) {
                throw new TimeoutException();
            }

            T item = slot.get(stamp);
            switch (stamp[0]) {
                case EMPTY: {
                    if (!slot.compareAndSet(item, myItem, EMPTY, WAITING)) {
                        break;
                    }

                    while (System.currentTimeMillis() < timeBound) {
                        item = slot.get(stamp);
                        if (stamp[0] == BUSY) {
                            // exchange finished
                            slot.set(null, EMPTY);
                            return item;
                        }
                    }

                    // 超时，将状态改为EMPTY
                    if (slot.compareAndSet(item, null, WAITING, EMPTY)) {
                        throw new TimeoutException();
                    }

                    // 交换成功
                    item = slot.get(stamp);
                    slot.set(null, EMPTY);
                    return item;
                }
                case WAITING: {
                    // 与处于WAITING的线程交换
                    if (slot.compareAndSet(item, myItem, WAITING, BUSY)) {
                        return item;
                    }
                    break;
                }
                case BUSY: {
                    // just retry
                    break;
                }
                default:
                    throw new IllegalStateException("Unknown stamp: " + stamp[0]);
            }
        }
    }

}
