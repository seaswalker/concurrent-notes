package chapter7;

import org.junit.Test;

/**
 * {@link CLHLock}测试.
 *
 * @author skywalker
 */
public class CLHLockTest {

    @Test
    public void test() {
        CLHLock lock = new CLHLock();
        lock.lock();
        System.out.println("进入临界区");
        lock.unlock();
        System.out.println("退出临界区");
        lock.lock();
    }

}
