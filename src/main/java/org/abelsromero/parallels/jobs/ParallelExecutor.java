package org.abelsromero.parallels.jobs;

import lombok.SneakyThrows;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * This class allows running a job a certain number of times by a certain number of workers.
 */
public class ParallelExecutor {

    private final ExecutorService executor;
    private final int executions;

    public ParallelExecutor(int workers, int executions) {
        executor = Executors.newFixedThreadPool(workers);
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
        executor.shutdown();
        // TODO should log the result of awaitTermination
        executor.awaitTermination(60, TimeUnit.MINUTES);

        return processExecutionResults(results, System.currentTimeMillis() - startTime);
    }

    private ExecutionDetails processExecutionResults(List<Future<JobSummary>> results, long totalTime) throws InterruptedException, ExecutionException {
        // Used good old loop for performance
        long successMinTime = 0;
        long successMaxTime = 0;
        long successfulTimeSum = 0;

        long failureMinTime = 0;
        long failureMaxTime = 0;
        long failureTimeSum = 0;
        int failedOperations = 0;

        for (Future<JobSummary> futureJobSummary : results) {
            final JobSummary job = futureJobSummary.get();
            if (job.successful()) {
                if (job.executionTime() > successMaxTime)
                    successMaxTime = job.executionTime();
                if (job.executionTime() < successMinTime || successMinTime == 0)
                    successMinTime = job.executionTime();
                successfulTimeSum += job.executionTime();
            } else {
                if (job.executionTime() > failureMaxTime) {
                    failureMaxTime = job.executionTime();
                }
                if (job.executionTime() < failureMinTime || failureMinTime == 0) {
                    failureMinTime = job.executionTime();
                }
                failureTimeSum += job.executionTime();
                failedOperations++;
            }
        }

        final int successfulOperations = executions - failedOperations;
        return ExecutionDetails.builder()
            .time(totalTime)
            .jobsPerSecond(totalTime > 0 ? (executions / (double) (totalTime / 1000)) : 0)
            .successfulJobs(new JobsDetails(successfulOperations, successMinTime, successMaxTime, successfulOperations > 0 ? successfulTimeSum / successfulOperations : 0))
            .failedJobs(new JobsDetails(failedOperations, failureMinTime, failureMaxTime, failedOperations > 0 ? failureTimeSum / failedOperations : 0))
            .build();
    }
}
