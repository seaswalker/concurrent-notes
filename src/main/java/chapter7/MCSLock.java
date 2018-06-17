package chapter7;

import java.util.concurrent.atomic.AtomicReference;

/**
 * RT.
 *
 * @author skywalker
 */
public class MCSLock {

    private final AtomicReference<QNode> tail = new AtomicReference<>(null);
    private final ThreadLocal<QNode> myNode = ThreadLocal.withInitial(QNode::new);

    public void lock() {
        QNode node = myNode.get();
        QNode prev = tail.getAndSet(node);
        if (prev != null) {
            node.isLocked = true;
            prev.next = node;
            while (node.isLocked);
        }
    }

    public void unlock() {
        QNode node = myNode.get();
        if (node.next == null) {
            if (tail.compareAndSet(node, null)) {
                return;
            }
            // 走到这里的原因：lock方法在更新tail和next之间有一个时间空隙
            while (node.next == null);
        }

        node.next.isLocked = false;
        node.next = null;
    }

    private class QNode {
        // 个人认为这里两个字段需要用volatile修饰，或者使用Unsafe.getBoolean进行轮询(避免hardware barrier)
        volatile boolean isLocked = false;
        volatile QNode next;
    }

}
