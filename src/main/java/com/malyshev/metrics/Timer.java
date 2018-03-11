package com.malyshev.metrics;

public class Timer {

    private long curTime;
    private long totalTime;

    public void start() {
        curTime = System.currentTimeMillis();
    }

    public void stop() {
        totalTime = System.currentTimeMillis() - curTime;
        System.out.printf("the total processing time is %d ms %n", totalTime);
    }

    public long getCurTime() {
        return curTime;
    }

    public long getTotalTime() {
        return totalTime;
    }
}
