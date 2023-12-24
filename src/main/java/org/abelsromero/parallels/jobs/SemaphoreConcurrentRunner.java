package org.abelsromero.parallels.jobs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Uses a {@link Semaphore} to limit concurrency, in case the
 * {@link ExecutorService} does not support any concurrency limit.
 */
class SemaphoreConcurrentRunner implements ConcurrentRunner<JobSummary> {

    private final ExecutorService executor;

    private final Semaphore semaphore;

    SemaphoreConcurrentRunner(ExecutorService executor, int concurrencyLimit) {
        this.executor = executor;
        this.semaphore = new Semaphore(concurrencyLimit);
    }

    @Override
    public List<Future<JobSummary>> invokeAll(List<Callable<JobSummary>> tasks) throws InterruptedException {

        final List<Callable<JobSummary>> collect = new ArrayList<>();
        for (Callable<JobSummary> task : tasks) {
            collect.add(new SafeCallable(task, semaphore));
        }

        return executor.invokeAll(collect);
    }

    @Override
    public void close() throws Exception {
        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.MINUTES);
    }

    private record SafeCallable<T>(Callable<T> task, Semaphore semaphore) implements Callable<T> {

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
}
