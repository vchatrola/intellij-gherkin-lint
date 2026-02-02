# Legacy Spring AppConfig (Reference)

This file preserves the original Spring Boot configuration used to create the
Gemini REST client and HTTP proxy interface. It is retained for reference only
and is **not** used by the current runtime.

```java
@Configuration
@ComponentScan(basePackages = "com.vchatrola.gemini")
@PropertySource("classpath:application.properties")
public class AppConfig {

  @Bean
  public RestClient geminiRestClient(@Value("${gemini.baseurl}") String baseUrl) {
    RestClient restClient = RestClient.builder()
        .baseUrl(baseUrl)
        .defaultHeader("Content-Type", "application/json")
        .defaultHeader("Accept", "application/json")
        .build();
    return restClient;
  }

  @Bean
  public GeminiInterface geminiInterface(@Qualifier("geminiRestClient") RestClient client) {
    RestClientAdapter adapter = RestClientAdapter.create(client);
    HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
    return factory.createClient(GeminiInterface.class);
  }
}
```
