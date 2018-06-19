package chapter7;

import java.util.concurrent.TimeUnit;

/**
 * 在{@link CompositeLock}的基础上增加fast path.
 *
 * @author skywalker
 */
public class CompositeFastPathLock extends CompositeLock {

    /**
     * 使用第30位作为fast path lock标志.
     */
    private static final int FAST_PATH_MASK = 0x40000000;

    public CompositeFastPathLock(int size) {
        super(size);
    }

    @Override
    public boolean tryLock(long time, TimeUnit timeUnit) throws InterruptedException {
        if (fastPathLock()) {
            return true;
        }

        if (super.tryLock(time, timeUnit)) {
            // 可能在申请普通锁期间有其它线程获取了fast path lock
            while ((tail.getStamp() & FAST_PATH_MASK) != 0) ;
        }

        return false;
    }

    @Override
    public void unlock() {
        if (fastPathUnlock()) {
            return;
        }
        super.unlock();
    }

    private boolean fastPathLock() {
        int[] stamp = {0};
        QNode node = tail.get(stamp);
        if (node != null) {
            return false;
        }

        if ((stamp[0] & FAST_PATH_MASK) != 0) {
            return false;
        }

        int newStamp = ((stamp[0] + 1) | FAST_PATH_MASK);
        return tail.compareAndSet(null, null, stamp[0], newStamp);
    }

    /**
     * 适用于快速路径的解锁操作.
     *
     * @return true, 如果成功解锁fast path lock.
     */
    private boolean fastPathUnlock() {
        int newStamp = 0, oldStamp;
        oldStamp = tail.getStamp();

        if ((oldStamp & FAST_PATH_MASK) == 0) {
            return false;
        }

        QNode node;
        int[] stamp = {0};

        do {
            node = tail.get(stamp);
            newStamp = ((stamp[0] + 1) & (~FAST_PATH_MASK));
        } while (!tail.compareAndSet(node, node, stamp[0], newStamp));

        return true;
    }

}