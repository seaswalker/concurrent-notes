package chapter9;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 细粒度同步链表.
 *
 * @author skywalker
 */
public class FineList<T> {

    protected final Node<T> head = new Node<>();
    protected final Node<T> tail = new Node<>();

    public FineList() {
        head.next = tail;
        head.key = Integer.MIN_VALUE;
        tail.key = Integer.MAX_VALUE;
    }

    /**
     * 添加新的节点.
     *
     * @return {@code true}, 如果添加成功
     */
    public boolean add(T item) {
        int key = item.hashCode();
        Node<T> prev = head;
        prev.lock.lock();
        try {
            Node<T> cur = prev.next;
            cur.lock.lock();

            try {
                // 由于此链表的tail节点的key为int最大值，所以一定会找到一个可以添加的位置
                while (cur.key < key) {
                    prev.lock.unlock();
                    prev = cur;
                    cur = cur.next;
                    cur.lock.lock();
                }

                if (cur.key == key) {
                    // 节点已存在
                    return false;
                }

                Node<T> node = new Node<>();
                node.item = item;
                prev.next = node;
                node.next = cur;

                return true;
            } finally {
                cur.lock.unlock();
            }
        } finally {
            prev.lock.unlock();
        }
    }

    /**
     * 节点删除.
     *
     * @return {@code true}, 如果删除成功
     */
    public boolean remove(T item) {
        int key = item.hashCode();
        Node<T> prev = head;
        prev.lock.lock();
        try {
            Node<T> cur = prev.next;
            cur.lock.lock();

            try {
                while (cur.key < key) {
                    prev.lock.unlock();
                    prev = cur;
                    cur = cur.next;
                    cur.lock.lock();
                }

                if (cur.key > key) {
                    // 节点不存在
                    return false;
                }

                prev.next = cur.next;
                return true;
            } finally {
                cur.lock.unlock();
            }
        } finally {
            prev.lock.unlock();
        }
    }

    protected class Node<V> {
        V item;
        int key;
        Node<V> next;

        final Lock lock = new ReentrantLock();
    }

}
