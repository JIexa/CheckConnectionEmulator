package com.malyshev;

import com.malyshev.metrics.Timer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadSupervisor implements Supervisor {

    private Timer timer;

    private int numberOfThread;

    private final transient AtomicInteger totalprocessingTime;

    private InteractionService interactionService;

    public ThreadSupervisor(InteractionService interactionService, int numberOfThread) {
        timer = new Timer();
        this.numberOfThread = numberOfThread;
        totalprocessingTime = new AtomicInteger();
        this.interactionService = interactionService;
    }

    @Override
    public void checkConnectionFor(List<String> devices) throws InterruptedException {

        System.out.println("============ processByFixedPool ============");

        timer.start();

        processByFixedPool(devices);

        timer.stop();


        System.out.println("============ processByForJoinPool ============");

        timer.start();

        processByForJoinPool(devices);

        timer.stop();


    }

    private void processByFixedPool(List<String> devices) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(1000);

        List<Callable<Integer>> tasks = prepareTasks(devices);
        List<Future<Integer>> futureList = executor.invokeAll(tasks);
        analyzeResults(futureList);
    }

    private void processByForJoinPool(List<String> devices) throws InterruptedException {
        ForkJoinPool forkJoinPool = new ForkJoinPool(1000);

        List<Callable<Integer>> tasks = prepareTasks(devices);
        List<Future<Integer>> futureList = forkJoinPool.invokeAll(tasks);
        analyzeResults(futureList);
    }

    private List<Callable<Integer>> prepareTasks(List<String> devices) {
        List<Callable<Integer>> tasks = new ArrayList<>();

        devices.forEach(device -> {
            Callable<Integer> task = new Callable<Integer>() {
                @Override
                public Integer call() {
                    return checkConnectionFor(device);
                }
            };
            tasks.add(task);
        });
        return tasks;
    }

    private void analyzeResults(List<Future<Integer>> futureList) throws InterruptedException {

        List<Integer> timeoutResponseCounter = new ArrayList<>();
        List<Integer> okResponseCounter = new ArrayList<>();
        futureList.forEach(f -> {

            Integer rc = null;
            try {
                rc = f.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            if ( rc == 200)
                okResponseCounter.add(rc);
            else if ( rc == 504)
                timeoutResponseCounter.add(rc);
        });

        System.out.printf("successful interactions: %d, failed interactions: %d%n", okResponseCounter.size(), timeoutResponseCounter.size());
    }

    private void processByParallelStream(List<String> devices) {
        devices.stream().parallel().forEach(
                d -> {
                    checkConnectionFor(d);
                }
        );
    }

    private int checkConnectionFor(String device) {

        return interactionService.sendTo(device, "check connection");
    }
}
