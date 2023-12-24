package org.abelsromero.parallels.jobs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Delegates concurrent work to the {@link ExecutorService} without imposing any restriction.
 */
class DefaultConcurrentRunner implements ConcurrentRunner<JobSummary> {

    private final ExecutorService executor;

    DefaultConcurrentRunner(ExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public List<Future<JobSummary>> invokeAll(List<Callable<JobSummary>> tasks) throws InterruptedException {

        final List<Callable<JobSummary>> collect = new ArrayList<>();
        for (Callable<JobSummary> task : tasks) {
            collect.add(task);
        }

        return executor.invokeAll(collect);
    }

    @Override
    public void close() throws Exception {
        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.MINUTES);
    }

    // TODO do we need to handle exceptions
//    private record SynchronizedCallable<T>(Callable<T> task) implements Callable<T> {
//
//        @Override
//        public T call() {
//            try {
//                return task.call();
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }
}
