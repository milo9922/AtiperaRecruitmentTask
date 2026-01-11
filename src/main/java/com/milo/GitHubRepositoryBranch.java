package com.milo;

public record GitHubRepositoryBranch(String name, Commit commit) {
    public record Commit(String sha) {
    }
}


