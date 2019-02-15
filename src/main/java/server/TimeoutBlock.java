package server;

public class TimeoutBlock {
    private final long timeoutMS;
    private final long timeoutInteval = 1000;

    public TimeoutBlock(long timeoutMilliSeconds){
        this.timeoutMS = timeoutMilliSeconds;
    }

    public void addBlock(Runnable runnable) throws Throwable {
        long collectIntervals = 0;
        Thread timeoutWorker = new Thread(runnable);
        timeoutWorker.start();
        do {
            if (collectIntervals >= this.timeoutMS) {
                timeoutWorker.stop();
                throw new Exception(timeoutMS + " ms. Thread Block Terminated.");
            }
            collectIntervals += timeoutInteval;
            Thread.sleep(timeoutInteval);
        } while (timeoutWorker.isAlive());
    }
}
