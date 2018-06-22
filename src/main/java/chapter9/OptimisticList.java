package chapter9;

/**
 * 乐观同步实现的链表：每次遍历是不需要加锁.
 *
 * @author skywalker
 */
public class OptimisticList<T> extends FineList<T> {

    @Override
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

    @Override
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

           prev.next = cur.next;

            return true;
        } finally {
            prev.lock.unlock();
            cur.lock.unlock();
        }
    }

    /**
     * 检查给定的节点是否仍然可达并且prev是否依然指向cur.
     *
     * @return {@code true}, 如果是
     */
    private boolean validate(Node<T> prev, Node<T> cur) {
        Node<T> node = head;
        while (node.key < prev.key) {
            node = node.next;
        }

        if (node.key > prev.key) {
            return false;
        }

        return (prev.next == cur);
    }

}
