package org.abelsromero.parallels.jobs;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public interface TasksRunner {

    ExecutionDetails run(Callable<Boolean> callable);

    static TasksRunner withPlatformThreads(int tasks, int threads) {
        final var executor = Executors.newFixedThreadPool(threads);
        final ConcurrentRunner<JobSummary> concurrentRunner = new DefaultConcurrentRunner(executor);
        return new DefaultTasksRunner(concurrentRunner, tasks);
    }

    static TasksRunner withNativeThreads(int tasks, int threads) {
        final var executor = Executors.newVirtualThreadPerTaskExecutor();
        final ConcurrentRunner<JobSummary> concurrentRunner = new SemaphoreConcurrentRunner(executor, threads);
        return new DefaultTasksRunner(concurrentRunner, tasks);
    }
}
