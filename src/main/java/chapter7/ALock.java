package chapter7;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基于数组实现的队列锁.
 *
 * @author skywalker
 */
public class ALock {

    private final ThreadLocal<Integer> slotIndex = ThreadLocal.withInitial(() -> 0);

    private final AtomicInteger tail = new AtomicInteger(0);

    private volatile boolean[] queue;

    private final int size;

    public ALock(int capacity) {
        this.size = capacity;
        this.queue = new boolean[capacity];
        this.queue[0] = true;
    }

    public void lock() {
        int slot = tail.getAndIncrement() % size;
        slotIndex.set(slot);

        while (!queue[slot]);
    }

    public void unlock() {
        int slot = slotIndex.get();
        queue[slot] = false;
        // notify next thread
        queue[(slot + 1) % size] = true;
    }

}
