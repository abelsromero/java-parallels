package org.abelsromero.parallels.jobs;

class PlatformThreadTasksRunnerTest extends TasksRunnerTest {

    @Override
    TasksRunner runner(int tasks, int threads) {
        return TasksRunner.withPlatformThreads(tasks, threads);
    }
}
