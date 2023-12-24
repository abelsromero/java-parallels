package org.abelsromero.parallels.jobs;

class PlatformThreadParallelExecutorTest extends ParallelExecutorTest {

    @Override
    ParallelExecutor executor(int workers, int executions) {
        return ParallelExecutor.platformThreads(workers, executions);
    }
}
