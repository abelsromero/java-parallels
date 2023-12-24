package org.abelsromero.parallels.jobs;

class NativeThreadTasksRunnerTest extends TasksRunnerTest {

    @Override
    TasksRunner runner(int tasks, int threads) {
        return TasksRunner.withNativeThreads(tasks, threads);
    }
}
