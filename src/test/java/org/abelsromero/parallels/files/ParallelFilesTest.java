package org.abelsromero.parallels.files;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;


class ParallelFilesTest {

    @Test
    void should_fail_if_destination_is_not_a_directory() {
        // given
        File file = getFileFromClasspath("ruby.png");
        // when
        ParallelFiles files = new ParallelFiles(4);

        Throwable throwable = Assertions.catchThrowable(() -> files.moveFiles(file, file));

        assertThat(throwable)
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_move_a_single_file() throws InterruptedException {
        // given
        final File source = getFileFromClasspath("ruby.png");
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
            getFileFromClasspath("ruby.png"),
            getFileFromClasspath("sample.pdf"),
            getFileFromClasspath("lorem_ipsum.txt")
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
        final URL resource = this.getClass().getClassLoader().getResource(path);
        if (resource == null)
            throw new FileNotFoundException("classpath:" + path);
        return new File(resource.toURI());
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
