package org.abelsromero.parallels.jobs;

import lombok.SneakyThrows;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * This class allows running a job a certain number of times by a certain number of workers.
 */
public class ParallelExecutor {

    private final ExecutorService executor;
    private final long executions;

    public ParallelExecutor(int workers, long executions) {
        executor = Executors.newFixedThreadPool(workers);
        this.executions = executions;
    }

    @SneakyThrows
    public ExecutionDetails run(Callable<Boolean> callable) {

        final List<Callable<Boolean>> callables = LongStream
            .rangeClosed(1, executions)
            .mapToObj(i -> callable)
            .collect(Collectors.toList());

        long init = System.currentTimeMillis();
        final List<Future<Boolean>> results = executor.invokeAll(callables);
        executor.shutdown();
        // TODO should log the result of awaitTermination
        executor.awaitTermination(60, TimeUnit.MINUTES);

        final Integer failedOperations = results.stream()
            .map(this::getBooleanAsInt)
            .reduce((v1, v2) -> v1 + v2)
            .get();

        return ExecutionDetails.builder()
            .time(System.currentTimeMillis() - init)
            .successfulOperations(executions - failedOperations)
            .failedOperations(failedOperations)
            .build();
    }

    /**
     * @return true -> 0, false -> 1
     */
    private Integer getBooleanAsInt(Future<Boolean> r) {
        try {
            return r.get(60, TimeUnit.SECONDS) ? 0 : 1;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            System.out.println("Error getting value");
            e.printStackTrace();
            return 1;
        }
    }

}
