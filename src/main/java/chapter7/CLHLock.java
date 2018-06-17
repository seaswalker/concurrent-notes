package chapter7;

import java.util.concurrent.atomic.AtomicReference;

/**
 * CLH锁实现，CLH为三个发明者的人名的第一个字母.
 *
 * @author skywalker
 */
public class CLHLock {

    private ThreadLocal<QNode> myNode = ThreadLocal.withInitial(QNode::new);
    private ThreadLocal<QNode> myPrev = ThreadLocal.withInitial(() -> null);

    // 书中初始时为null，是一个bug
    private final AtomicReference<QNode> tail = new AtomicReference<>(new QNode());

    public void lock() {
        QNode node = myNode.get();
        node.isLocked = true;
        QNode prev = tail.getAndSet(node);
        myPrev.set(prev);

        while (prev.isLocked);
    }

    public void unlock() {
        QNode node = myNode.get();
        node.isLocked = false;
        myNode.set(myPrev.get());
    }

    private class QNode {
        volatile boolean isLocked = false;
    }

}
