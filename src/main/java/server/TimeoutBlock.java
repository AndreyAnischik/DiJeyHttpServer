package server;

public class TimeoutBlock {
    private final long TIMEOUT_MS;
    private final long TIMEOUT_INTERVAL = 1000;

    public TimeoutBlock(long timeoutMilliSeconds){
        this.TIMEOUT_MS = timeoutMilliSeconds;
    }

    public void addBlock(Runnable runnable) throws Throwable {
        long collectIntervals = 0;
        Thread timeoutWorker = new Thread(runnable);
        timeoutWorker.start();
        do {
            if (collectIntervals >= this.TIMEOUT_MS) {
                timeoutWorker.stop();
                throw new Exception(TIMEOUT_MS + " ms. Thread Block Terminated.");
            }
            collectIntervals += TIMEOUT_INTERVAL;
            Thread.sleep(TIMEOUT_INTERVAL);
        } while (timeoutWorker.isAlive());
    }
}
