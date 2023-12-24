package org.abelsromero.parallels.jobs;

import lombok.SneakyThrows;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

class DeletageParallelExecutor implements TasksRunner {

    private final ConcurrentRunner executor;
    private final int executions;

    DeletageParallelExecutor(ConcurrentRunner executor, int executions) {
        this.executor = executor;
        this.executions = executions;
    }

    @SneakyThrows
    public ExecutionDetails run(Callable<Boolean> callable) {

        final List<Callable<JobSummary>> callables = LongStream
            .rangeClosed(1, executions)
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
        final List<Future<JobSummary>> results = executor.invokeAll(callables);
        executor.close();

        return ResultsProcessor.processExecutionResults(results, executions, System.currentTimeMillis() - startTime);
    }
}

