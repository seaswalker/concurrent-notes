package chapter7.backoff;

import java.util.Random;

/**
 * 指数后退.
 *
 * @author skywalker
 */
class Backoff {

    private final int minDelay;
    private final int maxDelay;

    private int limit;
    private Random random;

    Backoff(int minDelay, int maxDelay) {
        this.minDelay = minDelay;
        this.maxDelay = maxDelay;
        this.limit = minDelay;
        this.random = new Random();
    }

    void backoff() throws InterruptedException {
        int delay = random.nextInt(limit);
        limit = Math.min(maxDelay, 2 * limit);
        Thread.sleep(delay);
    }

}
