package chapter8.four;

import org.junit.Test;

public class UserLockTest {

    @Test
    public void test() throws InterruptedException {
        UseLock lock = new UseLock();
        Thread male = new Thread(() -> {
            lock.enterMale();
            System.out.println("男人洗澡");
            lock.leaveMale();
            System.out.println("男人离开");
        });
        male.start();

        Thread female = new Thread(() -> {
            System.out.println("女人尝试进入");
            lock.enterFemale();
            System.out.println("女人洗澡");
            lock.leaveFemale();
        });
        female.start();

        Thread.sleep(1000);
        male.join();
        female.join();
    }

}
