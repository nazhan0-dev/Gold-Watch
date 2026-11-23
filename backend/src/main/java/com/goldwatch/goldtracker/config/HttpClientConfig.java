package com.goldwatch.goldtracker.config;

import java.net.http.HttpClient;
import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(GoldTrackerProperties.class)
public class HttpClientConfig {

    /** One shared client for all outbound price sources: explicit timeouts and an honest User-Agent. */
    @Bean
    RestClient priceSourceRestClient(GoldTrackerProperties properties) {
        GoldTrackerProperties.Http http = properties.http();
        HttpClient httpClient = HttpClient.newBuilder()
                // All sources are simple HTTPS endpoints; HTTP/1.1 avoids HTTP/2 negotiation problems
                // seen with some servers when running on cloud hosts.
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(http.connectTimeout())
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(http.readTimeout());
        return RestClient.builder()
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.USER_AGENT, http.userAgent())
                .build();
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
