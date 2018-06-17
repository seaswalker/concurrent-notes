package chapter7;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 时限队列锁.
 *
 * @author skywalker
 */
public class TOLock {

    private final AtomicReference<QNode> tail = new AtomicReference<>(null);

    private final ThreadLocal<QNode> myNode = ThreadLocal.withInitial(() -> null);

    public boolean tryLock(long time, TimeUnit timeUnit) {
        long start = System.currentTimeMillis();
        long patience = TimeUnit.MILLISECONDS.convert(time, timeUnit);

        // 每次都使用一个新节点，因为老节点可能仍被后续节点引用
        QNode node = new QNode();
        myNode.set(node);
        QNode prev = tail.getAndSet(node);

        if (prev == null || prev == QNode.AVAILABLE) {
            return true;
        }

        while (System.currentTimeMillis() - start < patience) {
            QNode prevPrev = prev.prev;
            if (prevPrev == QNode.AVAILABLE) {
                return true;
            }
            if (prevPrev != null) {
                // 前驱节点已超时
                prev = prevPrev;
            }
        }

        // 当前线程已超时
        if (!tail.compareAndSet(node, prev)) {
            // 通知后续节点跳过自己
            node.prev = prev;
        }
        return false;
    }

    public void unlock() {
        QNode node = myNode.get();
        if (!tail.compareAndSet(node, null)) {
            node.prev = QNode.AVAILABLE;
        }
    }

    private static class QNode {
        /**
         * 锁已释放.
         */
        static QNode AVAILABLE = new QNode();

        volatile QNode prev;
    }

}
