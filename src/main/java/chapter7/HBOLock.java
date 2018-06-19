package chapter7;

import chapter7.backoff.Backoff;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基于后退锁的层次锁.
 *
 * @author skywalker
 */
public class HBOLock {

    private static final int LOCAL_MIN_DELAY = 10;
    private static final int LOCAL_MAX_DELAY = 20;
    private static final int REMOTE_MIN_DELAY = 100;
    private static final int REMOTE_MAX_DELAY = 200;
    private static final int FREE = -1;

    private final AtomicInteger state = new AtomicInteger(FREE);

    public void lock() throws InterruptedException {
        int clusterID = getThreadClusterID();
        Backoff localBackOff = new Backoff(LOCAL_MIN_DELAY, LOCAL_MAX_DELAY);
        Backoff remoteBackOff = new Backoff(REMOTE_MIN_DELAY, REMOTE_MAX_DELAY);

        while (true) {
            if (state.compareAndSet(FREE, clusterID)) {
                return;
            }

            int lockState = state.get();
            if (lockState == clusterID) {
                localBackOff.backoff();
            } else {
                remoteBackOff.backoff();
            }
        }
    }

    public void unlock() {
        state.set(FREE);
    }

    // 集群ID不知道如何获取，这里使用伪代码
    private int getThreadClusterID() {
        return 1;
    }

}
