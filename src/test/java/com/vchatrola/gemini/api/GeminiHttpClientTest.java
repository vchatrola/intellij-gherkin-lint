package com.vchatrola.gemini.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vchatrola.gemini.dto.GeminiRecords;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import javax.net.ssl.SSLSession;
import org.junit.jupiter.api.Test;

class GeminiHttpClientTest {

  @Test
  void getModels_parsesSuccessfulResponse() {
    String json = "{\"models\":[{\"name\":\"models/gemini-1.5\"}]}";
    StubHttpClient stub = new StubHttpClient().enqueue(new StubResponse(200, json));
    GeminiHttpClient client = new GeminiHttpClient(stub, defaultMapper());

    GeminiRecords.ModelList result = client.getModels("key");

    assertNotNull(result);
    assertEquals(1, result.models().size());
    assertEquals("models/gemini-1.5", result.models().get(0).name());
  }

  @Test
  void generateContent_retriesOn429ThenSucceeds() {
    String json = "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"ok\"}]}}]}";
    StubHttpClient stub =
        new StubHttpClient()
            .enqueue(new StubResponse(429, "rate limit"))
            .enqueue(new StubResponse(200, json));
    GeminiHttpClient client = new GeminiHttpClient(stub, defaultMapper());
    GeminiRecords.GeminiRequest request = buildRequest("test");

    GeminiRecords.GeminiResponse response =
        client.generateContent("models/gemini-1.5", "key", request);

    assertNotNull(response);
    assertEquals(2, stub.requests.size());
  }

  @Test
  void getModels_throwsAfterRetriesOnServerError() {
    StubHttpClient stub =
        new StubHttpClient()
            .enqueue(new StubResponse(503, "service down"))
            .enqueue(new StubResponse(503, "service down"))
            .enqueue(new StubResponse(503, "service down"));
    GeminiHttpClient client = new GeminiHttpClient(stub, defaultMapper());

    RuntimeException ex = assertThrows(RuntimeException.class, () -> client.getModels("key"));

    assertTrue(ex.getMessage().contains("503"));
    assertEquals(3, stub.requests.size());
  }

  @Test
  void getModels_retriesOnIOException() {
    StubHttpClient stub =
        new StubHttpClient()
            .enqueue(new StubIOException())
            .enqueue(new StubResponse(200, "{\"models\":[]}"));
    GeminiHttpClient client = new GeminiHttpClient(stub, defaultMapper());

    GeminiRecords.ModelList result = client.getModels("key");

    assertNotNull(result);
    assertEquals(2, stub.requests.size());
  }

  @Test
  void getModels_doesNotRetryOnClientError() {
    StubHttpClient stub = new StubHttpClient().enqueue(new StubResponse(400, "bad request"));
    GeminiHttpClient client = new GeminiHttpClient(stub, defaultMapper());

    RuntimeException ex = assertThrows(RuntimeException.class, () -> client.getModels("key"));

    assertTrue(ex.getMessage().contains("400"));
    assertEquals(1, stub.requests.size());
  }

  @Test
  void generateContent_sendsJsonBody() {
    String json = "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"ok\"}]}}]}";
    StubHttpClient stub = new StubHttpClient().enqueue(new StubResponse(200, json));
    GeminiHttpClient client = new GeminiHttpClient(stub, defaultMapper());
    GeminiRecords.GeminiRequest request = buildRequest("hello");

    client.generateContent("models/gemini-1.5", "key", request);

    HttpRequest httpRequest = stub.requests.getFirst();
    assertTrue(
        httpRequest.headers().firstValue("Content-Type").orElse("").contains("application/json"));
    assertNotNull(stub.lastBody);
    assertTrue(stub.lastBody.contains("hello"));
  }

  @Test
  void request_containsApiKeyHeaderAndTimeout() {
    StubHttpClient stub = new StubHttpClient().enqueue(new StubResponse(200, "{\"models\":[]}"));
    GeminiHttpClient client = new GeminiHttpClient(stub, defaultMapper());

    client.getModels("key-123");

    HttpRequest request = stub.requests.getFirst();
    assertEquals("key-123", request.headers().firstValue("x-goog-api-key").orElse(""));
    Optional<Duration> timeout = request.timeout();
    assertTrue(timeout.isPresent());
    assertEquals(30, timeout.get().getSeconds());
  }

  private static ObjectMapper defaultMapper() {
    return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  private static final class StubHttpClient extends HttpClient {
    private final Deque<StubAction> actions = new ArrayDeque<>();
    private final Deque<HttpRequest> requests = new ArrayDeque<>();
    private String lastBody;

    StubHttpClient enqueue(StubAction action) {
      actions.add(action);
      return this;
    }

    @Override
    public <T> HttpResponse<T> send(
        HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler)
        throws IOException, InterruptedException {
      requests.add(request);
      if (request.bodyPublisher().isPresent()) {
        BodyCaptureSubscriber subscriber = new BodyCaptureSubscriber(this);
        request.bodyPublisher().get().subscribe(subscriber);
        subscriber.await();
      }
      StubAction action = actions.removeFirst();
      return action.apply(request);
    }

    @Override
    public <T> java.util.concurrent.CompletableFuture<HttpResponse<T>> sendAsync(
        HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
      throw new UnsupportedOperationException();
    }

    @Override
    public <T> java.util.concurrent.CompletableFuture<HttpResponse<T>> sendAsync(
        HttpRequest request,
        HttpResponse.BodyHandler<T> responseBodyHandler,
        HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Optional<java.net.CookieHandler> cookieHandler() {
      return Optional.empty();
    }

    @Override
    public Optional<Duration> connectTimeout() {
      return Optional.empty();
    }

    @Override
    public Redirect followRedirects() {
      return Redirect.NEVER;
    }

    @Override
    public Optional<java.net.ProxySelector> proxy() {
      return Optional.empty();
    }

    @Override
    public javax.net.ssl.SSLContext sslContext() {
      return null;
    }

    @Override
    public javax.net.ssl.SSLParameters sslParameters() {
      return null;
    }

    @Override
    public Optional<java.net.Authenticator> authenticator() {
      return Optional.empty();
    }

    @Override
    public Version version() {
      return Version.HTTP_1_1;
    }

    @Override
    public Optional<java.util.concurrent.Executor> executor() {
      return Optional.empty();
    }
  }

  private static final class BodyCaptureSubscriber
      implements java.util.concurrent.Flow.Subscriber<java.nio.ByteBuffer> {
    private final StubHttpClient client;
    private final StringBuilder builder = new StringBuilder();
    private final java.util.concurrent.CountDownLatch done =
        new java.util.concurrent.CountDownLatch(1);
    private java.util.concurrent.Flow.Subscription subscription;

    private BodyCaptureSubscriber(StubHttpClient client) {
      this.client = client;
    }

    @Override
    public void onSubscribe(java.util.concurrent.Flow.Subscription subscription) {
      this.subscription = subscription;
      subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(java.nio.ByteBuffer item) {
      builder.append(java.nio.charset.StandardCharsets.UTF_8.decode(item));
    }

    @Override
    public void onError(Throwable throwable) {
      if (subscription != null) {
        subscription.cancel();
      }
      done.countDown();
    }

    @Override
    public void onComplete() {
      client.lastBody = builder.toString();
      done.countDown();
    }

    void await() throws InterruptedException {
      done.await(1, java.util.concurrent.TimeUnit.SECONDS);
    }
  }

  private interface StubAction {
    <T> HttpResponse<T> apply(HttpRequest request) throws IOException;
  }

  private static final class StubResponse implements StubAction {
    private final int status;
    private final String body;

    private StubResponse(int status, String body) {
      this.status = status;
      this.body = body;
    }

    @Override
    public <T> HttpResponse<T> apply(HttpRequest request) {
      @SuppressWarnings("unchecked")
      HttpResponse<T> response =
          (HttpResponse<T>) new StubHttpResponse(status, body, request.uri());
      return response;
    }
  }

  private static final class StubIOException implements StubAction {
    @Override
    public <T> HttpResponse<T> apply(HttpRequest request) throws IOException {
      throw new IOException("network error");
    }
  }

  private static final class StubHttpResponse implements HttpResponse<String> {
    private final int status;
    private final String body;
    private final URI uri;

    private StubHttpResponse(int status, String body, URI uri) {
      this.status = status;
      this.body = body;
      this.uri = uri;
    }

    @Override
    public int statusCode() {
      return status;
    }

    @Override
    public HttpRequest request() {
      return null;
    }

    @Override
    public Optional<HttpResponse<String>> previousResponse() {
      return Optional.empty();
    }

    @Override
    public HttpHeaders headers() {
      return HttpHeaders.of(java.util.Map.of(), (k, v) -> true);
    }

    @Override
    public String body() {
      return body;
    }

    @Override
    public Optional<SSLSession> sslSession() {
      return Optional.empty();
    }

    @Override
    public URI uri() {
      return uri;
    }

    @Override
    public HttpClient.Version version() {
      return HttpClient.Version.HTTP_1_1;
    }
  }

  private static GeminiRecords.GeminiRequest buildRequest(String text) {
    GeminiRecords.TextPart part = new GeminiRecords.TextPart(text);
    GeminiRecords.Content content = new GeminiRecords.Content(java.util.List.of(part));
    return new GeminiRecords.GeminiRequest(java.util.List.of(content));
  }
}
