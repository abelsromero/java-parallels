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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static org.abelsromero.parallels.scg.CacheTest.Configuration.getEnvInteger;
import static org.abelsromero.parallels.scg.CacheTest.Configuration.getEnvString;

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
        int loop = 1;

        final CacheTest cacheTest = new CacheTest();

        do {
            cacheTest.run();
            System.out.println("Completed iteration: " + loop);
            loop++;
        } while (true);
    }

    public void run() {

        final int workers = getEnvInteger("WORKERS").orElse(1);
        final int executions = getEnvInteger("EXECUTIONS").orElse(100);
        // test5 = cache
        // test6 = no cache
        final String contextPath = getEnvString("CONTEXT").orElse("test5");

        System.out.println("Workers: " + workers);
        System.out.println("Executions: " + executions);
        System.out.println("ContextPath: " + contextPath);

        // when
        final ParallelExecutor executor = new ParallelExecutor(workers, executions);
        final ExecutionDetails details = executor.run(() -> {
            try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {
                // Builds URLs of 1 to 2KB length
                final var httpRequest = new HttpGet(buildUrl(contextPath));
                httpRequest.addHeader("X-Variance", randomString());
                httpRequest.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
//            httpRequest.setEntity(randomEntity());

                final var response = httpclient.execute(httpRequest);
                int status = response.getStatusLine().getStatusCode();
                Thread.sleep(100l);
//            debugResponse(response);
                return status == 200;
            }
        });

        // Report
        long time = details.getTime();
        int success = details.getSuccessfulJobs().count();
        int fail = details.getFailedJobs().count();

        String report = new StringBuilder()
            .append("\n")
            .append("Total time (min): " + ((float) (time / 1000) / 60))
            .append("\n")
            .append("OK(s):\t" + success)
            .append("\n")
            .append("ERROR(s):\t" + fail)
            .append("\n")
            .toString();
        System.out.println(report);

    }

    private void debugResponse(CloseableHttpResponse response) throws IOException {
        final HttpEntity responseEntity = response.getEntity();
        final String contentType = responseEntity.getContentType().getValue();
        final long contentLength = responseEntity.getContentLength();

        InputStream is = responseEntity.getContent();
        String text = null;
        try (Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name())) {
            text = scanner.useDelimiter("\\A").next();
        }
        System.out.println("status: " + response.getStatusLine().getStatusCode());
        System.out.println("body: " + text);
    }

    private int randomInt(int start, int end) {
        return randomGenerator.nextInt(end - start) + start;
    }

    private StringEntity randomEntity() throws JsonProcessingException {
        var values = Map.of(
            "id", UUID.randomUUID(),
            "timestamp", LocalDateTime.now(),
            "text", loremIpsumGenerator.getText(randomInt(3000, 4096))
        );

        return new StringEntity(objectMapper.writeValueAsString(values), StandardCharsets.UTF_8);
    }

    private String buildUrl(String contextPath) {
        String value = loremIpsumGenerator.getText(randomInt(1000, 2000));
        String escapedHTML = value.replaceAll(" ", "%20");
//        return "http://localhost:8080/test5/" + randomString() + "/anything/" + escapedHTML;
        // Use ClusterIP service name within K8s cluster
        return "http://my-gateway/" + contextPath + "/" + randomString() + "/anything/" + escapedHTML;
    }

    private String randomString() {
        return UUID.randomUUID().toString();
    }


    class Configuration {
        static Optional<Integer> getEnvInteger(String key) {
            final String value = System.getenv(key);
            return (value == null) ? Optional.empty() : Optional.of(Integer.parseInt(value));
        }

        static Optional<String> getEnvString(String key) {
            return Optional.of(System.getenv(key));
        }
    }

    static class LoremIpsumGenerator {

        public static final String LOREM_FILE = "lorem_ipsum.txt";

        private final String content;

        LoremIpsumGenerator() throws IOException {
            InputStream inputStream = CacheTest.class.getResourceAsStream(LOREM_FILE);
            if (inputStream == null) {
                inputStream = LoremIpsumGenerator.class.getResourceAsStream("/" + LOREM_FILE);
                if (inputStream == null)
                    throw new RuntimeException("File not found: " + LOREM_FILE);
            }

            final String text = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
            this.content = text.replaceAll("\n", "");
        }

        String getText(int size) {
            if (size > 4096)
                throw new RuntimeException("Exceeded max size (4096):" + size);

            return content.substring(0, size);
        }
    }
}
