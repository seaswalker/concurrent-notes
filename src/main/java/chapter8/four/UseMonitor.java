package chapter8.four;

/**
 * 使用内置的synchronized再次实现洗澡问题.
 *
 * @author skywalker
 */
public class UseMonitor {

    private final Object monitor = new Object();

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
        synchronized (monitor) {
            if (persons == 0) {
                ++persons;
                isMale = true;
            } else if (isMale) {
                ++persons;
            } else {
                while (persons > 0) {
                    try {
                        // 等待女人洗完
                        monitor.wait();
                    } catch (InterruptedException ignore) {
                    }
                }
            }
        }

    }
        /**
         * 男性离开澡堂.
         */
        public void leaveMale() {
            synchronized (monitor) {
                --persons;
                if (persons == 0) {
                    monitor.notifyAll();
                }
            }
        }

        public void enterFemale () {
            synchronized (monitor) {
                if (persons == 0) {
                    ++persons;
                    isMale = false;
                } else if (!isMale) {
                    ++persons;
                } else {
                    while (persons > 0) {
                        try {
                            monitor.wait();
                        } catch (InterruptedException ignore) {
                        }
                    }
                }
            }
        }

        public void leaveFemale () {
            synchronized (monitor) {
                --persons;
                if (persons == 0) {
                    monitor.notifyAll();
                }
            }
        }

    }
