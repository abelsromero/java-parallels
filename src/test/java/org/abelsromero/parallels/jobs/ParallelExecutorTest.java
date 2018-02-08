package org.abelsromero.parallels.jobs;


import org.junit.Test;

import java.util.concurrent.Callable;

import static java.lang.Boolean.TRUE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

public class ParallelExecutorTest {

    // aka. you only had one job
    @Test
    public void should_run_a_single_job() {
        // when
        final ParallelExecutor executor = new ParallelExecutor(1, 1);
        final ExecutionDetails details = executor.run(() -> TRUE);
        // then
        assertThat(details.getSuccessfulOperations(), equalTo(1l));
        assertThat(details.getFailedOperations(), equalTo(0l));
        assertThat(details.getTime(), greaterThan(0l));
    }

    @Test
    public void should_run_1000_successful_executions() {
        // given
        final int workers = 1;
        final long executions = 1000;
        // when
        final ParallelExecutor executor = new ParallelExecutor(workers, executions);
        final ExecutionDetails details = executor.run(() -> TRUE);
        // then
        assertThat(details.getSuccessfulOperations(), equalTo(1000l));
        assertThat(details.getFailedOperations(), equalTo(0l));
        assertThat(details.getTime(), greaterThan(0l));
    }

    @Test
    public void should_run_1000_successful_and_300_failed_executions() {
        // given
        final int workers = 8;
        final long executions = 1000;
        // when
        final ParallelExecutor executor = new ParallelExecutor(workers, executions + 300);
        final ExecutionDetails details = executor.run(new Callable<Boolean>() {
            private int counter = -1;

            @Override
            public synchronized Boolean call() {
                counter++;
                return counter < 1000;
            }
        });
        // then
        assertThat(details.getSuccessfulOperations(), equalTo(1000l));
        assertThat(details.getFailedOperations(), equalTo(300l));
        assertThat(details.getTime(), greaterThan(0l));
    }

    @Test
    public void should_not_blow_when_callback_throws_exceptions_and_count_that_as_failed() {
        // given
        final int workers = 2;
        final long executions = 10;
        // when
        final ParallelExecutor executor = new ParallelExecutor(workers, executions);
        final ExecutionDetails details = executor.run(() -> {
            throw new InterruptedException();
        });
        // then
        assertThat(details.getSuccessfulOperations(), equalTo(0l));
        assertThat(details.getFailedOperations(), equalTo(10l));
        assertThat(details.getTime(), greaterThan(0l));
    }

}
