package com.milo;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(GitHubApiProperties.class)
public class GitHubApiConfiguration {
    @Bean
    public RestClient gitHubRestClient(RestClient.Builder builder, GitHubApiProperties properties) {
        return builder
                .baseUrl(properties.apiUrl())
                .defaultHeader("Accept", "application/vnd.github.v3+json")
                .defaultHeader("X-GitHub-Api-Version", properties.apiVersion())
                .build();
    }
}
