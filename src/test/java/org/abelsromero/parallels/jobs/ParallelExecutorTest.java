package org.abelsromero.parallels.jobs;


import lombok.SneakyThrows;
import org.junit.Test;

import java.util.concurrent.Callable;

import static java.lang.Boolean.TRUE;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ParallelExecutorTest {

    // aka. you only had one job
    @Test
    public void should_run_a_single_job() {
        // when
        final ParallelExecutor executor = new ParallelExecutor(1, 1);
        final ExecutionDetails details = executor.run(() -> TRUE);
        // then
        assertThat(details.getSuccessfulJobs().count(), equalTo(1));
        assertThat(details.getFailedJobs().count(), equalTo(0));
        // It can return 0
        assertThat(details.getTime(), greaterThanOrEqualTo(0l));
    }

    @Test
    public void should_run_1000_successful_executions() {
        // given
        final int workers = 1;
        final int executions = 1000;
        // when
        final ParallelExecutor executor = new ParallelExecutor(workers, executions);
        final ExecutionDetails details = executor.run(() -> {
            Thread.sleep(1l);
            return TRUE;
        });
        // then
        assertThat(details.getSuccessfulJobs().count(), equalTo(1000));
        assertThat(details.getFailedJobs().count(), equalTo(0));
        assertThat(details.getTime(), greaterThan(0l));
    }

    @Test
    public void should_run_1000_successful_and_300_failed_executions() {
        // given
        final int workers = 8;
        final int executions = 1000;
        // when
        final ParallelExecutor executor = new ParallelExecutor(workers, executions + 300);
        final ExecutionDetails details = executor.run(new Callable<Boolean>() {
            private int counter = -1;

            @Override
            @SneakyThrows
            public synchronized Boolean call() {
                counter++;
                Thread.sleep(20l);
                return counter < 1000;
            }
        });
        // then
        assertThat(details.getSuccessfulJobs().count(), equalTo(1000));
        assertThat(details.getFailedJobs().count(), equalTo(300));
        assertThat(details.getTotalJobsCount(), equalTo(1300));
        assertThat(details.getJobsPerSecond(), greaterThan(0d));
        // assert successful jobs details
        assertThat(details.getSuccessfulJobs().minTime(),
            allOf(greaterThan(0l), lessThan(details.getSuccessfulJobs().avgTime())));
        assertThat(details.getSuccessfulJobs().maxTime(),
            allOf(greaterThan(0l), greaterThan(details.getSuccessfulJobs().avgTime())));
        assertThat(details.getSuccessfulJobs().avgTime(),
            allOf(greaterThan(details.getSuccessfulJobs().minTime()), lessThan(details.getSuccessfulJobs().maxTime())));
        // assert failed jobs details
        assertThat(details.getFailedJobs().minTime(),
            allOf(greaterThan(0l), lessThan(details.getFailedJobs().avgTime())));
        assertThat(details.getFailedJobs().maxTime(),
            allOf(greaterThan(0l), greaterThan(details.getFailedJobs().avgTime())));
        assertThat(details.getFailedJobs().avgTime(),
            allOf(greaterThan(details.getFailedJobs().minTime()), lessThan(details.getFailedJobs().maxTime())));

        assertThat(details.getTime(), greaterThan(0l));
    }

    @Test
    public void should_not_blow_when_callback_throws_exceptions_and_count_that_as_failed() {
        // given
        final int workers = 2;
        final int executions = 10;
        // when
        final ParallelExecutor executor = new ParallelExecutor(workers, executions);
        final ExecutionDetails details = executor.run(() -> {
            throw new InterruptedException();
        });
        // then
        assertThat(details.getSuccessfulJobs().count(), equalTo(0));
        assertThat(details.getFailedJobs().count(), equalTo(10));
        assertThat(details.getTime(), greaterThanOrEqualTo(0l));
    }

}
