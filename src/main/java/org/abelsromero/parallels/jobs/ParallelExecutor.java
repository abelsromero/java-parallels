package org.abelsromero.parallels.jobs;

import java.util.concurrent.Callable;

public interface ParallelExecutor {

    static ParallelExecutor platformThreads(int workers, int executions) {
        return new PlatformThreadsParallelExecutor(workers, executions);
    }

    static ParallelExecutor nativeThreads(int workers, int executions) {
        return new NativeThreadsParallelExecutor(workers, executions);
    }

    ExecutionDetails run(Callable<Boolean> callable);
}
