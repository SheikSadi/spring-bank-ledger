package com.paypay.learn.ledger;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

public class TestUtils {

    private static final int defaultWaitForSeconds = 5;

    public static <T> List<T> runConcurrentlyWithSupplier(
        int nThreads, Supplier<T> supplier, int waitForSeconds
    ) throws InterruptedException {

        // Thread-safe list to collect results
        List<T> accumList = Collections.synchronizedList(
            new ArrayList<T>()
        );

        ExecutorService executor = Executors.newFixedThreadPool(nThreads);
        CountDownLatch readyLatch = new CountDownLatch(nThreads);
        CountDownLatch doneLatch = new CountDownLatch(nThreads);
        CountDownLatch triggerLatch = new CountDownLatch(1);

        for (int i = 0; i < nThreads; i++) {
            executor.submit(
                () -> {
                    readyLatch.countDown();
                    try {
                      triggerLatch.await();
                      accumList.add(supplier.get());  
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            );
        }

        readyLatch.await(); // Prepare
        triggerLatch.countDown(); // pistol
        doneLatch.await(waitForSeconds, TimeUnit.SECONDS);
    
        return accumList;
    }

    public static <T> List<T> runConcurrentlyWithSupplier(
        int nThreads, Supplier<T> supplier
    ) throws InterruptedException {
        List<T> accumList = runConcurrentlyWithSupplier(nThreads, supplier, 5);
        return accumList;
    }

    public static void runConcurrently(
        int nThreads, Runnable action, int waitForSeconds
    ) throws InterruptedException {

        ExecutorService executor = Executors.newFixedThreadPool(nThreads);
        CountDownLatch readyLatch = new CountDownLatch(nThreads);
        CountDownLatch doneLatch = new CountDownLatch(nThreads);
        CountDownLatch triggerLatch = new CountDownLatch(1);

        for (int i = 0; i < nThreads; i++) {
            executor.submit(
                () -> {
                    readyLatch.countDown();
                    try {
                      triggerLatch.await();
                      action.run();  
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            );
        }

        readyLatch.await(); // Prepare
        triggerLatch.countDown(); // pistol
        doneLatch.await(waitForSeconds, TimeUnit.SECONDS);
    }

    public static void runConcurrently(
        int nThreads, Runnable action
    ) throws InterruptedException {
        runConcurrently(nThreads, action, defaultWaitForSeconds);
    }
}
