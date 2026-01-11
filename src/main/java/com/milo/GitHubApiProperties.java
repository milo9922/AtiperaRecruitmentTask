package com.milo;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "github")
public record GitHubApiProperties(String apiUrl, String apiVersion, String personalAccessToken) {}
