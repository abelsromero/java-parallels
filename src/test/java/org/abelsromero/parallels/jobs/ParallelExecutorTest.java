package org.abelsromero.parallels.jobs;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;

import static java.lang.Boolean.TRUE;
import static org.assertj.core.api.Assertions.assertThat;

abstract class ParallelExecutorTest {

    // aka. you only had one job
    @Test
    void should_run_a_single_job() {
        // when
        final ParallelExecutor executor = executor(1, 1);
        final ExecutionDetails details = executor.run(() -> {
            Thread.sleep(10);
            return TRUE;
        });
        // then
        assertThat(details.getSuccessfulJobs().count()).isEqualTo(1);
        assertThat(details.getFailedJobs().count()).isEqualTo(0);
        assertThat(details.getTime()).isGreaterThanOrEqualTo(10);
    }

    @Test
    void should_run_1000_successful_executions() {
        // given
        final int workers = 1;
        final int executions = 1000;
        // when
        final ParallelExecutor executor = executor(workers, executions);
        final ExecutionDetails details = executor.run(() -> {
            Thread.sleep(1);
            return TRUE;
        });
        // then
        assertThat(details.getSuccessfulJobs().count()).isEqualTo(1000);
        assertThat(details.getFailedJobs().count()).isEqualTo(0);
        assertThat(details.getTime()).isGreaterThan(0);
    }

    @Test
    void should_run_1000_successful_and_300_failed_executions() {
        // given
        final int workers = 8;
        final int executions = 1000;
        // when
        final ParallelExecutor executor = executor(workers, executions + 300);
        final ExecutionDetails details = executor.run(new Callable<>() {
            private int counter = -1;

            @Override
            @SneakyThrows
            public synchronized Boolean call() {
                counter++;
                Thread.sleep(10);
                return counter < 1000;
            }
        });
        // then
        final JobsDetails successfulJobs = details.getSuccessfulJobs();
        final JobsDetails failedJobs = details.getFailedJobs();

        assertThat(successfulJobs.count()).isEqualTo(1000);
        assertThat(failedJobs.count()).isEqualTo(300);
        assertThat(details.getTotalJobsCount()).isEqualTo(1300);
        assertThat(details.getJobsPerSecond()).isGreaterThan(0);
        // assert successful jobs details
        assertThat(successfulJobs.minTime())
            .isGreaterThan(0)
            .isLessThan(successfulJobs.avgTime());
        assertThat(successfulJobs.maxTime())
            .isGreaterThan(0)
            .isGreaterThan(successfulJobs.avgTime());
        // assert failed jobs details
        assertThat(failedJobs.minTime())
            .isGreaterThan(0)
            .isLessThan(failedJobs.avgTime());
        assertThat(failedJobs.maxTime())
            .isGreaterThan(0)
            .isGreaterThan(failedJobs.avgTime());

        assertThat(details.getTime()).isGreaterThan(0);
    }

    @Test
    void should_not_blow_when_callback_throws_exceptions_and_count_that_as_failed() {
        // given
        final int workers = 2;
        final int executions = 10;
        // when
        final ParallelExecutor executor = executor(workers, executions);
        final ExecutionDetails details = executor.run(() -> {
            throw new InterruptedException();
        });
        // then
        assertThat(details.getSuccessfulJobs().count()).isEqualTo(0);
        assertThat(details.getFailedJobs().count()).isEqualTo(10);
        assertThat(details.getTime()).isGreaterThanOrEqualTo(0);
    }

    abstract ParallelExecutor executor(int workers, int executions);
}
