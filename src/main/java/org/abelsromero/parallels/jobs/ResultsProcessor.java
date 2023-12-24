package org.abelsromero.parallels.jobs;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

final class ResultsProcessor {

    static JobDetails process(List<Future<JobSummary>> results, int tasks, long totalTime) throws ExecutionException, InterruptedException {
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

        final int successfulOperations = tasks - failedOperations;
        return JobDetails.builder()
            .time(totalTime)
            .tasksPerSecond(totalTime > 0 ? (tasks / (double) (totalTime / 1000)) : 0)
            .successfulTasks(new TasksDetails(successfulOperations, successMinTime, successMaxTime, successfulOperations > 0 ? successfulTimeSum / successfulOperations : 0))
            .failedTasks(new TasksDetails(failedOperations, failureMinTime, failureMaxTime, failedOperations > 0 ? failureTimeSum / failedOperations : 0))
            .build();
    }
}
