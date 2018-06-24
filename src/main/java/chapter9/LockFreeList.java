package chapter9;

import java.util.concurrent.atomic.AtomicMarkableReference;

/**
 * 无锁(非阻塞同步)的链表实现.
 *
 * @author skywalker
 */
public class LockFreeList<T> {

    private final Node<T> head = new Node<>();
    private final Node<T> tail = new Node<>();

    public LockFreeList() {
        head.next = new AtomicMarkableReference<>(tail, false);
        head.key = Integer.MIN_VALUE;
        tail.key = Integer.MAX_VALUE;
    }

    public boolean add(T item) {
        int key = item.hashCode();

        while (true) {
            Window<T> window = find(head, key);

            if (window.cur.key == key) {
                return false;
            }

            Node<T> node = new Node<>();
            node.key = key;
            node.item = item;
            node.next = new AtomicMarkableReference<>(window.cur, false);

            // 结构已发生变化
            if (window.prev.next.compareAndSet(window.cur, node, false, false)) {
                return true;
            }
        }
    }

    public boolean remove(T item) {
        int key = item.hashCode();

        while (true) {
            Window<T> window = find(head, key);

            if (window.cur.key > key) {
                return false;
            }

            // 逻辑删除自己
            if (!window.cur.next.compareAndSet(window.cur, window.cur, false, true)) {
                continue;
            }

            // 物理删除自己，失败也ok
            window.prev.next.compareAndSet(window.cur, window.cur.next.getReference(), false, false);
            return true;
        }
    }

    public boolean contains(T item) {
        int key = item.hashCode();

        var marked = new boolean[] {false};
        Node<T> node = head;
        while (node.key < key) {
            node = node.next.get(marked);
        }

        return (node.key == key && !marked[0]);
    }

    private Window<T> find(Node<T> head, int key) {
        Node<T> prev, cur, succ;
        boolean[] marked = {false};
        boolean snip;
        retry : while (true) {
            prev = head;
            cur = prev.next.getReference();
            while (true) {
                succ = cur.next.get(marked);

                // 删除(物理)所有已被逻辑删除的节点
                while (marked[0]) {
                    snip = prev.next.compareAndSet(cur, succ, false, false);
                    if (!snip) {
                        // 失败的原因:
                        // 1. cur之前有新节点插入
                        // 2. prev节点也被删除
                        continue retry;
                    }

                    cur = succ;
                    succ = succ.next.get(marked);
                }

                if (cur.key >= key) {
                    return new Window<T>(prev, cur);
                }

                prev = cur;
                cur = succ;
            }
        }
    }

    private class Node<V> {
        int key;
        V item;
        AtomicMarkableReference<Node<V>> next;
    }

    /**
     * 代表一次对链表的遍历.
     */
    private class Window<V> {

        final Node<V> prev;
        final Node<V> cur;

        private Window(Node<V> prev, Node<V> cur) {
            this.prev = prev;
            this.cur = cur;
        }

    }

}
