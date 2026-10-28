package ar.edu.programacion2.catalog.catalog.infrastructure.rest;

import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogSnapshotSource;
import java.net.URI;
import java.net.http.HttpClient;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "catalog.catedra.enabled", havingValue = "true")
public class CatalogRestConfiguration {
    @Bean
    CatalogSnapshotSource catalogSnapshotSource(
            @Value("${catalog.catedra.base-url}") URI baseUrl,
            @Value("${catalog.catedra.jwt}") String jwt,
            @Value("${catalog.catedra.connect-timeout}") String connectTimeoutValue,
            @Value("${catalog.catedra.read-timeout}") String readTimeoutValue) {
        var connectTimeout = DurationStyle.detectAndParse(connectTimeoutValue);
        var readTimeout = DurationStyle.detectAndParse(readTimeoutValue);
        if (!("http".equals(baseUrl.getScheme()) || "https".equals(baseUrl.getScheme()))
                || baseUrl.getHost() == null || baseUrl.getUserInfo() != null
                || baseUrl.getQuery() != null || baseUrl.getFragment() != null) {
            throw new IllegalArgumentException("A valid HTTP base URL without credentials is required");
        }
        if (jwt.isBlank() || jwt.contains("\r") || jwt.contains("\n")) {
            throw new IllegalArgumentException("A technical JWT is required");
        }
        if (connectTimeout.isNegative() || connectTimeout.isZero()
                || readTimeout.isNegative() || readTimeout.isZero()) {
            throw new IllegalArgumentException("Timeouts must be positive");
        }
        var httpClient = HttpClient.newBuilder().connectTimeout(connectTimeout)
            .followRedirects(HttpClient.Redirect.NEVER).build();
        var factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(readTimeout);
        var client = RestClient.builder().baseUrl(baseUrl.toString()).requestFactory(factory)
            .defaultHeaders(headers -> headers.setBearerAuth(jwt)).build();
        return new RestCatalogSnapshotSource(client);
    }
}
