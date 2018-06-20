package chapter8;

/**
 * 习题1.
 *
 * @author skywalker
 */
public class One {

    private final Object monitor = new Object();

    private boolean hasWriter = false;
    private int readers = 0;

    public class ReadLock {
        public void lock() {
            synchronized (monitor) {
                while (hasWriter) {
                    try {
                        monitor.wait();
                        // 不可中断
                    } catch (InterruptedException ignore) {
                    }

                    ++readers;
                }
            }
        }

        public void unlock() {
            synchronized (monitor) {
                --readers;
                if (readers == 0) {
                    monitor.notifyAll();
                }
            }
        }
    }

    public class WriteLock {
        public void lock() {
            synchronized (monitor) {
                while (hasWriter || readers > 0) {
                    try {
                        monitor.wait();
                    } catch (InterruptedException ignore) {
                    }
                }

                hasWriter = true;
            }
        }

        public void unlock() {
            synchronized (monitor) {
                hasWriter = false;
                monitor.notifyAll();
            }
        }
    }

}
