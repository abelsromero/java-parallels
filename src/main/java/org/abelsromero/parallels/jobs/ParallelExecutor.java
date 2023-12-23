package org.abelsromero.parallels.jobs;

import java.util.concurrent.Callable;

/**
 * This class allows running a job a certain number of times by a certain number of workers.
 */
public interface ParallelExecutor {

    static ParallelExecutor platformThreads (int workers, int executions) {
        return new PlatformThreadsParallelExecutor(workers, executions);
    }

    ExecutionDetails run(Callable<Boolean> callable);
}
