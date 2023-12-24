package org.abelsromero.parallels.jobs;

import lombok.SneakyThrows;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

class DefaultTasksRunner implements TasksRunner {

    private final ConcurrentRunner concurrentRunner;
    private final int tasksCount;

    DefaultTasksRunner(ConcurrentRunner concurrentRunner, int tasksCount) {
        this.concurrentRunner = concurrentRunner;
        this.tasksCount = tasksCount;
    }

    @SneakyThrows
    public JobDetails run(Callable<Boolean> callable) {

        final List<Callable<JobSummary>> callables = LongStream
            .rangeClosed(1, tasksCount)
            .mapToObj(i -> (Callable<JobSummary>) () -> {
                long time = System.currentTimeMillis();
                Boolean succeed = Boolean.FALSE;
                try {
                    succeed = callable.call();
                } catch (Exception e) {

                }
                time = System.currentTimeMillis() - time;
                return succeed ? JobSummary.success(time) : JobSummary.failure(time);
            })
            .collect(Collectors.toList());

        long startTime = System.currentTimeMillis();
        final List<Future<JobSummary>> results = concurrentRunner.invokeAll(callables);
        concurrentRunner.close();

        return ResultsProcessor.process(results, tasksCount, System.currentTimeMillis() - startTime);
    }
}

