package org.abelsromero.parallels.jobs;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Runs all tasks at once (no ramp-up).
 **/
public interface ConcurrentRunner<T> extends AutoCloseable {

    List<Future<T>> invokeAll(List<Callable<T>> callables) throws InterruptedException;

}
