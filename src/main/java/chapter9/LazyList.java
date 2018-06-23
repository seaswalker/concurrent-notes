package chapter9;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 惰性同步实现的链表.
 *
 * @author skywalker
 */
public class LazyList<T> {

    private final Node<T> head = new Node<>();
    private final Node<T> tail = new Node<>();

    public boolean add(T item) {
        int key = item.hashCode();
        Node<T> prev = head, cur = prev.next;

        while (cur.key < key) {
            prev = cur;
            cur = cur.next;
        }

        prev.lock.lock();
        cur.lock.lock();
        try {
            if (!validate(prev, cur)) {
                return false;
            }

            if (cur.key == key) {
                return false;
            }

            Node<T> node = new Node<T>();
            node.item = item;
            prev.next = node;
            node.next = cur;

            return true;
        } finally {
            prev.lock.unlock();
            cur.lock.unlock();
        }
    }

    public boolean remove(T item) {
        int key = item.hashCode();
        Node<T> prev = head, cur = prev.next;

        while (cur.key < key) {
            prev = cur;
            cur = cur.next;
        }

        prev.lock.lock();
        cur.lock.lock();
        try {
            if (!validate(prev, cur)) {
                return false;
            }

            if (cur.key != key) {
                return false;
            }

            // 首先标记为逻辑删除
            cur.marked = true;
            // 个人认为这里需要smp_mb()
            prev.next = cur.next;

            return true;
        } finally {
            prev.lock.unlock();
            cur.lock.unlock();
        }
    }

    public boolean contains(T item) {
        int key = item.hashCode();
        Node<T> cur = head;
        while (cur.key < key) {
            cur = cur.next;
        }

        return (cur.key == key && !cur.marked);
    }

    /**
     * 检查给定的节点是否仍然可达并且prev是否依然指向cur.
     *
     * @return {@code true}, 如果是
     */
    private boolean validate(Node<T> prev, Node<T> cur) {
       return (!prev.marked && cur.marked && prev.next == cur);
    }

    protected class Node<V> {
        V item;
        int key;
        Node<V> next;

        boolean marked = false;

        final Lock lock = new ReentrantLock();
    }

}
