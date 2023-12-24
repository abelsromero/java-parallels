package org.abelsromero.parallels.jobs;

class NativeThreadParallelExecutorTest extends ParallelExecutorTest {

    @Override
    ParallelExecutor executor(int workers, int executions) {
        return ParallelExecutor.nativeThreads(workers, executions);
    }
}
