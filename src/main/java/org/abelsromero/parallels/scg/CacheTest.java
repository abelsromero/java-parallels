package org.abelsromero.parallels.scg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.abelsromero.parallels.jobs.ExecutionDetails;
import org.abelsromero.parallels.jobs.ParallelExecutor;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class CacheTest {

    private static final Random randomGenerator = ThreadLocalRandom.current();
    private static final ObjectMapper objectMapper;
    private static final LoremIpsumGenerator loremIpsumGenerator;

    static {
        try {
            objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());

            loremIpsumGenerator = new LoremIpsumGenerator();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {

        final int workers = 50;
        final int executions = 100_000;
        // when
        final ParallelExecutor executor = new ParallelExecutor(workers, executions);
        final ExecutionDetails details = executor.run(() -> {

            final CloseableHttpClient httpclient = HttpClients.createDefault();
            // Builds URLs of 1 to 2KB length
            final var httpRequest = new HttpGet(buildUrl());
            httpRequest.addHeader("X-Variance", randomString());
            httpRequest.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
//            httpRequest.setEntity(randomEntity());

            final var response = httpclient.execute(httpRequest);

            int status = response.getStatusLine().getStatusCode();
//            debugResponse(response);
            return status == 200;
        });


        // Report
        long time = details.getTime();
        int success = details.getSuccessfulJobs().count();
        int fail = details.getFailedJobs().count();

        String report = new StringBuilder()
            .append("======================")
            .append("\n")
            .append("Time: " + ((float) (time / 1000) / 60))
            .append("\n")
            .append("OK:\t" + success)
            .append("\n")
            .append("ERROR:\t" + fail)
            .append("\n")
            .toString();
        System.out.println(report);

    }

    private static void debugResponse(CloseableHttpResponse response) throws IOException {
        final HttpEntity responseEntity = response.getEntity();
        final String contentType = responseEntity.getContentType().getValue();
        final long contentLength = responseEntity.getContentLength();

        InputStream is = responseEntity.getContent();
        String text = null;
        try (Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name())) {
            text = scanner.useDelimiter("\\A").next();
        }
    }

    private static int randomInt(int start, int end) {
        return randomGenerator.nextInt(end - start) + start;
    }

    private static StringEntity randomEntity() throws JsonProcessingException {
        var values = Map.of(
            "id", UUID.randomUUID(),
            "timestamp", LocalDateTime.now(),
            "text", loremIpsumGenerator.getText(randomInt(3000, 4096))
        );

        return new StringEntity(objectMapper.writeValueAsString(values), StandardCharsets.UTF_8);
    }

    private static String buildUrl() {
        String value = loremIpsumGenerator.getText(randomInt(1000, 2000));
        String escapedHTML = value.replaceAll(" ", "%20");
        return "http://localhost:8080/test5/" + randomString() + "/anything/" + escapedHTML;
    }

    private static String randomString() {
        return UUID.randomUUID().toString();
    }

    static class LoremIpsumGenerator {

        public static final String LOREM_FILE = "/lorem_ipsum.txt";

        private final String content;

        LoremIpsumGenerator() throws IOException {
            URL url = LoremIpsumGenerator.class.getResource(LOREM_FILE);

            if (url == null)
                throw new RuntimeException("File not found: " + LOREM_FILE);

            final Path path = Path.of(url.getPath());
            this.content = Files.readString(path).replaceAll("\n", "");
        }

        String getText(int size) {
            if (size > 4096)
                throw new RuntimeException("Exceeded max size (4096):" + size);

            return content.substring(0, size);
        }
    }
}
