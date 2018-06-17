package chapter7;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link ALock}改进版本，避免伪共享问题.
 *
 * @author skywalker
 */
public class BetterALock {

    private final ThreadLocal<Integer> slotIndex = ThreadLocal.withInitial(() -> 0);

    private final AtomicInteger tail = new AtomicInteger(0);

    private volatile boolean[] queue;

    private final int size;

    private static final int CACHE_LINE_FACTOR = 16;

    public BetterALock(int capacity) {
        // 假设缓存行的大小为64B，Java里面boolean使用int实现，所以16个boolean值可以填满一个缓存行
        this.size = (capacity * CACHE_LINE_FACTOR);
        this.queue = new boolean[size];
        this.queue[0] = true;
    }

    public void lock() {
        int slot = tail.getAndAdd(CACHE_LINE_FACTOR) % size;
        slotIndex.set(slot);

        while (!queue[slot]);
    }

    public void unlock() {
        int slot = slotIndex.get();
        queue[slot] = false;
        // notify next thread
        queue[(slot + CACHE_LINE_FACTOR) % size] = true;
    }

}
