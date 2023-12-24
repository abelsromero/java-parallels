package org.abelsromero.parallels.jobs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

class ConcurrentExecutor<T> implements AutoCloseable {

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    private final Semaphore semaphore;

    ConcurrentExecutor(int concurrencyLimit) {
        this.semaphore = new Semaphore(concurrencyLimit);
    }

    List<Future<T>> invokeAll(List<Callable<T>> tasks) throws InterruptedException {

        final List<Callable<T>> collect = new ArrayList<>();
        for (Callable<T> task : tasks) {
            collect.add(new SynchronizedCallable(task, semaphore));
        }

        return executor.invokeAll(collect);
    }

    private record SynchronizedCallable<T>(Callable<T> task, Semaphore semaphore) implements Callable<T> {

        @Override
        public T call() {
            try {
                semaphore.acquire();
                T result = task.call();
                return result;
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                semaphore.release();
            }
        }
    }

    @Override
    public void close() throws Exception {
        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.MINUTES);
    }
}
