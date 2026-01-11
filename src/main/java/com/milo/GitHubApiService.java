package com.milo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GitHubApiService {

    private static final String COULD_NOT_PARSE_ERROR_MESSAGE = "Couldn't parse error message";
    private static final String ERROR_MESSAGE_NOT_FOUND = "Couldn't parse error message";
    private static final Logger log = LoggerFactory.getLogger(GitHubApiService.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public GitHubApiService(RestClient restClient, ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    public List<GitHubRepositoryInfo> getUserPublicRepositories(String username) {
        GitHubRepositoryInfo[] repositoriesInfo = restClient.get()
                .uri("/users/{user}/repos", username)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    handleGitHubError(response);
                })
                .body(GitHubRepositoryInfo[].class);
        return repositoriesInfo != null ? Arrays.asList(repositoriesInfo) : new ArrayList<>();
    }

    public Map<String, GitHubRepositoryBranch[]> getUserPublicRepositoriesBranches(List<GitHubRepositoryInfo> repositoriesInfo) {
        Map<String, GitHubRepositoryBranch[]> branches = new ConcurrentHashMap<>();

        var futures = repositoriesInfo.stream()
                .map(repo -> CompletableFuture.runAsync(() -> {
                    GitHubRepositoryBranch[] repoBranches = fetchBranches(repo.owner().login(), repo.name());
                    branches.put(repo.name(), repoBranches);
                }))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
        return branches;
    }

    private void handleGitHubError(ClientHttpResponse response) throws IOException {
        String message = retrieveGitHubErrorMessage(response);
        HttpStatusCode statusCode = response.getStatusCode();

        switch (statusCode) {
            case HttpStatus.NOT_FOUND -> throw new UserNotFoundException(message);
            case HttpStatus.NOT_ACCEPTABLE -> throw new NotAcceptableException(message);
            case HttpStatus.FORBIDDEN -> throw new ApiLimitExceededException(message);
            default -> throw new RuntimeException("Unexpected status code: " + statusCode);
        }
    }

    private GitHubRepositoryBranch[] fetchBranches(String ownerLogin, String repoName) {
        try {
            GitHubRepositoryBranch[] response = restClient.get()
                    .uri("/repos/{user}/{repository}/branches", ownerLogin, repoName)
                    .retrieve()
                    .body(GitHubRepositoryBranch[].class);
            return response != null ? response : new GitHubRepositoryBranch[0];
        } catch (Exception e) {
            log.error("Failed to fetch branches for {}: {}", repoName, e.getMessage());
            return new GitHubRepositoryBranch[0];
        }
    }

    public List<GitHubRepository> getPublicNonForkRepositories(String ownerLogin) {
        List<GitHubRepositoryInfo> allReposInfo = getUserPublicRepositories(ownerLogin);

        List<GitHubRepositoryInfo> nonForkReposInfo = allReposInfo.stream()
                .filter(repo -> !repo.isFork())
                .toList();

        Map<String, GitHubRepositoryBranch[]> branches = getUserPublicRepositoriesBranches(nonForkReposInfo);

        return nonForkReposInfo.stream()
                .map(repo -> new GitHubRepository(
                        repo.name(),
                        repo.owner(),
                        Arrays.asList(branches.getOrDefault(repo.name(), new GitHubRepositoryBranch[0]))
                ))
                .toList();
    }

    private String retrieveGitHubErrorMessage(ClientHttpResponse response) {
        try {
            byte[] bodyBytes = response.getBody().readAllBytes();
            if(bodyBytes.length == 0) {
                return ERROR_MESSAGE_NOT_FOUND;
            }
            JsonNode json = objectMapper.readTree(bodyBytes);
            if(json.isArray() && !json.isEmpty()) {
                json = json.get(0);
            }

            if(json.has("message")) {
                return json.get("message").asString();
            }
        } catch (IOException exception) {
            return COULD_NOT_PARSE_ERROR_MESSAGE;
        }
        return ERROR_MESSAGE_NOT_FOUND;
    }

}
