package org.abelsromero.parallels.files;

import java.io.File;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ParallelFiles {

    final ExecutorService executor;

    /**
     * @param workers number of concurrent workers to be used
     */
    public ParallelFiles(int workers) {
        executor = Executors.newFixedThreadPool(workers);
    }

    /**
     * Moves all files to a destination directory.
     * If the destination directory does not exist, it will be created.
     */
    public void moveFiles(File destination, File... files) throws InterruptedException {

        if (!destination.exists())
            destination.mkdirs();

        if (!destination.isDirectory())
            throw new IllegalArgumentException("destination must be a directory");

        final List<Future<Boolean>> futures = executor.invokeAll(
            Stream.of(files)
                .map(f -> (Callable<Boolean>) () -> {
                    final File dest = f.isDirectory() ? destination : new File(destination, f.getName());
                    return f.renameTo(dest);
                }).collect(Collectors.toList())
        );
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);
    }
}
