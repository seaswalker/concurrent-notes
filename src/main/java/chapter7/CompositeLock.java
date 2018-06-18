package chapter7;

import chapter7.backoff.Backoff;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * 复合锁.相对于{@link TOLock}，有以下几个优点:
 * <ol>
 * <li>节点可以重复利用，且机制较为简单.</li>
 * <li>有一个最大等待线程数的上限，故内存占用也有上限.</li>
 * <li>支持backoff机制.</li>
 * </ol>
 *
 * @author skywalker
 */
public class CompositeLock {

    private final int size;
    private final QNode[] waiting;

    private final AtomicStampedReference<QNode> tail = new AtomicStampedReference<>(null, 0);
    private final ThreadLocal<QNode> myNode = ThreadLocal.withInitial(() -> null);
    private final Random random = new Random();

    private static final int MIN_BACKOFF = 10;
    private static final int MAX_BACKOFF = 100;

    public CompositeLock(int size) {
        this.size = size;
        this.waiting = new QNode[size];
        for (int i = 0; i < size; ++i) {
            waiting[i] = new QNode();
        }
    }

    public boolean tryLock(long time, TimeUnit timeUnit) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        long patience = TimeUnit.MILLISECONDS.convert(time, timeUnit);
        Backoff backoff = new Backoff(MIN_BACKOFF, MAX_BACKOFF);

        try {
            QNode node = acquireQNode(backoff, startTime, patience);
            QNode prev = spliceQNode(node, startTime, patience);
            waitForPredecessor(prev, node, startTime, patience);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public void unlock() {
        QNode node = myNode.get();
        node.state.set(QNodeState.RELEASED);
        myNode.set(null);
    }

    /**
     * 在{@link #waiting}中申请一个空闲节点.
     *
     * @throws TimeoutException 如果等待超时
     */
    private QNode acquireQNode(Backoff backoff, long startTime, long patience)
            throws InterruptedException, TimeoutException {
        QNode node = waiting[random.nextInt(size)];

        while (true) {
            if (node.state.compareAndSet(QNodeState.FREE, QNodeState.WAITING)) {
                return node;
            }

            QNodeState state = node.state.get();
            int[] stamp = new int[1];
            QNode currentTail = tail.get(stamp);

            // 不太明白这的逻辑的意义：
            // 当前获取的是尾节点并且状态是已释放或ABORTED时，直接删除尾节点即可
            if (node == currentTail && (state == QNodeState.ABORTED || state == QNodeState.RELEASED)) {
                QNode prev = null;
                if (state == QNodeState.ABORTED) {
                    prev = currentTail.prev;
                }

                if (tail.compareAndSet(currentTail, prev, stamp[0], stamp[0] + 1)) {
                    node.state.set(QNodeState.WAITING);
                    return node;
                }
            }

            backoff.backoff();
            if (timeout(startTime, patience)) {
                throw new TimeoutException();
            }
        }
    }

    /**
     * 将给定的空闲节点插入到等待队列.
     *
     * @return 上一个节点
     * @throws TimeoutException 如果超时
     */
    private QNode spliceQNode(QNode node, long startTime, long patience) throws TimeoutException {
        QNode currentTail;
        int[] stamp = new int[1];

        do {
            if (timeout(startTime, patience)) {
                throw new TimeoutException();
            }

            currentTail = tail.get(stamp);
        } while (!tail.compareAndSet(currentTail, node, stamp[0], stamp[0] + 1));

        return currentTail;
    }

    /**
     * 等待给定节点获得锁.
     */
    private void waitForPredecessor(QNode prev, QNode node, long startTime, long patience) throws TimeoutException {
        // 快速路径
        if (prev == null) {
            myNode.set(node);
            return;
        }

        QNodeState prevState = prev.state.get();
        while (prevState != QNodeState.RELEASED) {
            if (prevState == QNodeState.ABORTED) {
                prev.state.set(QNodeState.FREE);
                prev = prev.prev;
            }

            if (timeout(startTime, patience)) {
                throw new TimeoutException();
            }

            prevState = prev.state.get();
        }

        prev.state.set(QNodeState.FREE);
        myNode.set(node);
    }

    private boolean timeout(long start, long patience) {
        return (System.currentTimeMillis() - start >= patience);
    }

    private enum QNodeState {
        /**
         * 表示可被线程申请.
         */
        FREE,
        /**
         * 已被线程占用.
         */
        WAITING,
        /**
         * 线程已释放锁.
         */
        RELEASED,
        /**
         * 等待超时.
         */
        ABORTED
    }

    private class QNode {
        volatile QNode prev;
        AtomicReference<QNodeState> state = new AtomicReference<>(QNodeState.FREE);
    }

}
