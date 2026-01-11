package com.milo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/github")
public class GitHubApiController {

    private final GitHubApiService githubService;

    public GitHubApiController(GitHubApiService githubService) {
        this.githubService = githubService;
    }

    @GetMapping("user/repos")
    public ResponseEntity<?> getUserPublicRepositories(@RequestParam String username) {
        return ResponseEntity.ok(githubService.getPublicNonForkRepositories(username));
    }
}
