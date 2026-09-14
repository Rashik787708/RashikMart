package com.rashik.rashikmart.loadtest;

import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Mechanical load test for the marketplace endpoint.
 *
 * <p>Usage: java com.rashik.rashikmart.loadtest.LoadTestRunner &lt;baseUrl&gt; &lt;concurrentUsers&gt; &lt;durationSeconds&gt;</p>
 *
 * <p>Each simulated user registers + logs in to obtain an independent session cookie,
 * then repeatedly GETs /buyer/marketplace for the configured duration. Results are
 * printed as an aggregate summary. Exit code is 0 only when zero HTTP errors occur.</p>
 */
public final class LoadTestRunner {

    private static final String ENDPOINT = "/buyer/marketplace";

    private final String baseUrl;
    private final int users;
    private final long durationMillis;
    private final String timestamp;

    private final AtomicInteger requests = new AtomicInteger();
    private final AtomicInteger errors = new AtomicInteger();
    private final AtomicLong totalLatencyNanos = new AtomicLong();
    private final AtomicLong minLatencyNanos = new AtomicLong(Long.MAX_VALUE);
    private final AtomicLong maxLatencyNanos = new AtomicLong(Long.MIN_VALUE);

    public LoadTestRunner(String baseUrl, int users, int durationSeconds) {
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.users = users;
        this.durationMillis = durationSeconds * 1000L;
        this.timestamp = String.valueOf(System.currentTimeMillis());
    }

    public static void main(String[] args) throws Exception {
        String baseUrl = args.length > 0 ? args[0] : "http://localhost:8080/RashikMart";
        int users = args.length > 1 ? Integer.parseInt(args[1]) : 10;
        int durationSeconds = args.length > 2 ? Integer.parseInt(args[2]) : 60;

        LoadTestRunner runner = new LoadTestRunner(baseUrl, users, durationSeconds);
        int exit = runner.run();
        System.exit(exit);
    }

    private int run() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(users);
        CountDownLatch prepared = new CountDownLatch(users);
        CountDownLatch startSignal = new CountDownLatch(1);
        List<java.util.concurrent.Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < users; i++) {
            final int workerId = i;
            futures.add(pool.submit(() -> {
                try {
                    CookieManager cookies = login(register(workerId));
                    prepared.countDown();
                    if (!startSignal.await(30, TimeUnit.SECONDS)) {
                        return;
                    }
                    load(workerId, cookies);
                } catch (Exception e) {
                    System.err.println("Worker " + workerId + " failed setup: " + e);
                    prepared.countDown();
                }
            }));
        }

        if (!prepared.await(60, TimeUnit.SECONDS)) {
            System.err.println("Not all users could authenticate before load phase.");
            pool.shutdownNow();
            return 1;
        }

        long startWall = System.nanoTime();
        startSignal.countDown();
        System.out.println("LOAD START: users=" + users + " durationMs=" + durationMillis
                + " endpoint=" + baseUrl + ENDPOINT);

        while (System.nanoTime() - startWall < durationMillis * 1_000_000L) {
            Thread.sleep(250);
        }

        long wallSeconds = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - startWall);
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);

        long totalMillis = totalLatencyNanos.get() / 1_000_000L;
        long avgMillis = requests.get() == 0 ? 0 : totalMillis / requests.get();

        System.out.println("LOAD DONE: wallSeconds=" + wallSeconds);
        System.out.println("requests=" + requests.get());
        System.out.println("requestsPerSecond=" + (wallSeconds == 0 ? 0.0 : (double) requests.get() / wallSeconds));
        System.out.println("errors=" + errors.get());
        System.out.println("avgLatencyMs=" + avgMillis);
        System.out.println("minLatencyMs=" + (minLatencyNanos.get() == Long.MAX_VALUE ? 0 : minLatencyNanos.get() / 1_000_000L));
        System.out.println("maxLatencyMs=" + (maxLatencyNanos.get() == Long.MIN_VALUE ? 0 : maxLatencyNanos.get() / 1_000_000L));

        return errors.get() == 0 && requests.get() > 0 ? 0 : 1;
    }

    private String register(int workerId) throws Exception {
        String email = "loadtest" + workerId + "_" + timestamp + "@test.com";
        String password = "LoadTest#2026";

        HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/RegisterServlet"))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form(
                        "name", "Load Tester " + workerId,
                        "email", email,
                        "password", password,
                        "role", "BUYER")))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 3) {
            throw new IllegalStateException("Registration failed for worker " + workerId
                    + " status=" + response.statusCode());
        }
        return email + "\u0001" + password;
    }

    private CookieManager login(String credentials) throws Exception {
        String[] parts = credentials.split("\u0001", 2);
        CookieManager cookieManager = new CookieManager();

        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .cookieHandler(cookieManager)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/login"))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form(
                        "email", parts[0],
                        "password", parts[1])))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 3) {
            throw new IllegalStateException("Login failed for " + parts[0]
                    + " status=" + response.statusCode());
        }
        return cookieManager;
    }

    private void load(int workerId, CookieManager cookies) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .cookieHandler(cookies)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + ENDPOINT))
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();

        while (!Thread.currentThread().isInterrupted()) {
            long start = System.nanoTime();
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                long latency = System.nanoTime() - start;
                requests.incrementAndGet();
                totalLatencyNanos.addAndGet(latency);
                minLatencyNanos.accumulateAndGet(latency, Long::min);
                maxLatencyNanos.accumulateAndGet(latency, Long::max);

                if (response.statusCode() != 200) {
                    errors.incrementAndGet();
                    System.err.println("Worker " + workerId + " non-200 status=" + response.statusCode());
                }
            } catch (Exception e) {
                errors.incrementAndGet();
                System.err.println("Worker " + workerId + " request failed: " + e);
            }
        }
    }

    private static String form(String... keyValues) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keyValues.length; i += 2) {
            if (i > 0) {
                sb.append('&');
            }
            sb.append(URLEncoder.encode(keyValues[i], StandardCharsets.UTF_8))
                    .append('=')
                    .append(URLEncoder.encode(keyValues[i + 1], StandardCharsets.UTF_8));
        }
        return sb.toString();
    }
}