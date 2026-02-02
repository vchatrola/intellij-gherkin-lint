# Legacy Spring GeminiInterface (Reference)

This file preserves the original Spring HTTP proxy interface used for Gemini
API calls. It is retained for reference only and is **not** used by the current
runtime.

```java
@Component
@HttpExchange("/v1beta/models/")
public interface GeminiInterface {

  @GetExchange
  ModelList getModels(@RequestHeader("x-goog-api-key") String apiKey);

  @PostExchange("{model}:countTokens")
  GeminiCountResponse countTokens(
      @PathVariable String model,
      @RequestHeader("x-goog-api-key") String apiKey,
      @RequestBody GeminiRequest request);

  @PostExchange("{model}:generateContent")
  GeminiResponse getCompletion(
      @PathVariable String model,
      @RequestHeader("x-goog-api-key") String apiKey,
      @RequestBody GeminiRequest request);
}
```
