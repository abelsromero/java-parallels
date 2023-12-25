package org.abelsromero.parallels.files;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;


class ParallelFilesTest {

    private static final String RUBY_PNG = "ruby.png";
    private static final String SAMPLE_PDF = "sample.pdf";
    private static final String LOREM_IPSUM_TXT = "lorem_ipsum.txt";

    @Test
    void should_fail_if_destination_is_not_a_directory() {
        // given
        File file = getFileFromClasspath(RUBY_PNG);
        // when
        ParallelFiles files = new ParallelFiles(4);

        Throwable throwable = Assertions.catchThrowable(() -> files.moveFiles(file, file));

        assertThat(throwable)
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_move_a_single_file() throws InterruptedException {
        // given
        final File source = getFileFromClasspath(RUBY_PNG);
        final File file = new File(getTestDirectory(), source.getName());
        copy(source, file);
        assertThat(file).exists();
        final File destination = new File(getTestDirectory());
        // when
        ParallelFiles pfiles = new ParallelFiles(1);
        pfiles.moveFiles(destination, file);
        // then
        assertThat(file).doesNotExist();
        assertThat(new File(destination, file.getName())).exists();
    }

    @Test
    void should_move_multiple_files() throws InterruptedException {
        // given
        final String testDirectory = getTestDirectory();
        final File[] sources = new File[]{
            getFileFromClasspath(RUBY_PNG),
            getFileFromClasspath(SAMPLE_PDF),
            getFileFromClasspath(LOREM_IPSUM_TXT)
        };
        final File[] files = new File[sources.length];
        for (int i = 0; i < sources.length; i++) {
            files[i] = new File(testDirectory, sources[i].getName());
            copy(sources[i], files[i]);
        }
        final File destination = new File(getTestDirectory());
        // when
        ParallelFiles pfiles = new ParallelFiles(2);
        pfiles.moveFiles(destination, files);
        // then
        for (File f : files) {
            assertThat(f).doesNotExist();
            assertThat(new File(destination, f.getName())).exists();
        }
    }

    @SneakyThrows
    private File getFileFromClasspath(String path) {
        // Load from folder to avoid configuring resources for native tests
        return new File("src/test/resources/", path);
    }

    private String getTestDirectory() {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("HHmmss-mmm");
        return String.format("target/temp-4-test-%s-%s", ThreadLocalRandom.current().nextInt(100), dateFormat.format(new Date()));
    }

    @SneakyThrows
    private void copy(File file, File target) {
        target.getParentFile().mkdirs();
        Files.copy(file.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}
