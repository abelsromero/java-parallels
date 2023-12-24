package org.abelsromero.parallels.jobs;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;

import static java.lang.Boolean.TRUE;
import static org.assertj.core.api.Assertions.assertThat;

abstract class TasksRunnerTest {

    // aka. you only had one job
    @Test
    void should_run_a_single_job() {
        // when
        final TasksRunner runner = runner(1, 1);
        final ExecutionDetails details = runner.run(() -> {
            Thread.sleep(10);
            return TRUE;
        });
        // then
        assertThat(details.getSuccessfulTasks().count()).isEqualTo(1);
        assertThat(details.getFailedTasks().count()).isEqualTo(0);
        assertThat(details.getTime()).isGreaterThanOrEqualTo(10);
    }

    @Test
    void should_run_1000_successful_executions() {
        // given
        final int tasks = 1000;
        final int threads = 1;
        // when
        final TasksRunner runner = runner(tasks, threads);
        final ExecutionDetails details = runner.run(() -> {
            Thread.sleep(1);
            return TRUE;
        });
        // then
        assertThat(details.getSuccessfulTasks().count()).isEqualTo(1000);
        assertThat(details.getFailedTasks().count()).isEqualTo(0);
        assertThat(details.getTime()).isGreaterThan(0);
    }

    @Test
    void should_run_1000_successful_and_300_failed_executions() {
        // given
        final int tasks = 1000;
        final int threads = 8;
        // when
        final TasksRunner runner = runner(tasks + 300, threads);
        final ExecutionDetails details = runner.run(new Callable<>() {
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
        final TasksDetails successfulTasks = details.getSuccessfulTasks();
        final TasksDetails failedTasks = details.getFailedTasks();

        assertThat(successfulTasks.count()).isEqualTo(1000);
        assertThat(failedTasks.count()).isEqualTo(300);
        assertThat(details.getTotalTasksCount()).isEqualTo(1300);
        assertThat(details.getTasksPerSecond()).isGreaterThan(0);
        // assert successful jobs details
        assertThat(successfulTasks.minTime())
            .isGreaterThan(0)
            .isLessThan(successfulTasks.avgTime());
        assertThat(successfulTasks.maxTime())
            .isGreaterThan(0)
            .isGreaterThan(successfulTasks.avgTime());
        // assert failed jobs details
        assertThat(failedTasks.minTime())
            .isGreaterThan(0)
            .isLessThan(failedTasks.avgTime());
        assertThat(failedTasks.maxTime())
            .isGreaterThan(0)
            .isGreaterThan(failedTasks.avgTime());

        assertThat(details.getTime()).isGreaterThan(0);
    }

    @Test
    void should_not_blow_when_callback_throws_exceptions_and_count_that_as_failed() {
        // given
        final int tasks = 10;
        final int threads = 2;
        // when
        final TasksRunner runner = runner(tasks, threads);
        final ExecutionDetails details = runner.run(() -> {
            throw new InterruptedException();
        });
        // then
        assertThat(details.getSuccessfulTasks().count()).isEqualTo(0);
        assertThat(details.getFailedTasks().count()).isEqualTo(10);
        assertThat(details.getTime()).isGreaterThanOrEqualTo(0);
    }

    abstract TasksRunner runner(int tasks, int threads);
}
