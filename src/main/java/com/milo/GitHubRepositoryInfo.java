package com.milo;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubRepositoryInfo (
   String name,
   Owner owner,
   @JsonProperty("fork") boolean isFork
) {
    public record Owner(String login) {}
}