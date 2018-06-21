package chapter8.four;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 使用{@link java.util.concurrent.locks.ReentrantLock}实现洗澡澡问题.
 *
 * @author skywalker
 */
public class UseLock {

    private final Lock lock = new ReentrantLock();
    // 这里区分男人和女人也许是没有必要的
    private final Condition male = lock.newCondition();
    private final Condition female = lock.newCondition();

    /**
     * 人数(同性别).
     */
    private int persons = 0;
    /**
     * 是否是男性在澡堂.
     */
    private boolean isMale = false;

    /**
     * 男性进入浴室.
     */
    public void enterMale() {
        lock.lock();
        try {
            if (persons == 0) {
                ++persons;
                isMale = true;
            } else if (isMale) {
                ++persons;
            } else {
                while (persons > 0) {
                    try {
                        male.await();
                    } catch (InterruptedException ignore) {
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 男性离开澡堂.
     */
    public void leaveMale() {
        lock.lock();
        try {
            --persons;
            if (persons == 0) {
                female.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    public void enterFemale() {
        lock.lock();
        try {
            if (persons == 0) {
                ++persons;
                isMale = false;
            } else if (!isMale) {
                ++persons;
            } else {
                while (persons > 0) {
                    try {
                        female.await();
                    } catch (InterruptedException ignore) {
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void leaveFemale() {
        lock.lock();
        try {
            --persons;
            if (persons == 0) {
                male.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

}
