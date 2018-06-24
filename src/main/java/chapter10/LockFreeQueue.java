package chapter10;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 无锁实现的无界队列.
 *
 * @author skywalker
 */
public class LockFreeQueue<T> {

    private final Node<T> STUPID_NODE = new Node<>();
    private final AtomicReference<Node<T>> head = new AtomicReference<>(STUPID_NODE);
    private final AtomicReference<Node<T>> tail = new AtomicReference<>(STUPID_NODE);

    public void enq(T item) {
        Node<T> node = new Node<>();
        node.item = item;

        while (true) {
            Node<T> last = tail.get();
            Node<T> next = last.next.get();

            // 这个判断的目的是优化?
            if (last != tail.get()) {
                continue;
            }

            if (next == null) {
                if (last.next.compareAndSet(null, node)) {
                    tail.compareAndSet(last, node);
                    return;
                }
            } else {
                // help
                tail.compareAndSet(last, next);
            }
        }
    }

    public T deq() {
        while (true) {
            Node<T> first = head.get(), last = tail.get();
            Node<T> next = first.next.get();

            if (first != head.get()) {
                continue;
            }

            if (first == last) {
                if (next == null) {
                    // empty
                    throw new IllegalArgumentException();
                }
                // help
                tail.compareAndSet(last, next);
            } else if (head.compareAndSet(first, next)) {
                return next.item;
            }
        }
    }

    private class Node<V> {
        V item;
        AtomicReference<Node<V>> next = new AtomicReference<>(null);
    }

}
