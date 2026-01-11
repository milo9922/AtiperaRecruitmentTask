package com.milo;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "github.api-url=http://localhost:${wiremock.server.port}")
@ActiveProfiles("test")
@WireMockTest
class AtiperaRecruitmentTaskIntegrationTests {

    private static final String EXPECTED_USER_REPOS_WITH_BRANCHES_PATH = "json/app/user-repos-with-branches-expected-200.json";
    private static final String USER_REPOS_PATH = "json/github/user-repos-200.json";
    private static final String GITHUB_NO_USER_REPOS_FOUND_PATH = "json/github/user-repos-404.json";
    private static final String GITHUB_EXCEEDED_API_RATE_LIMIT_PATH = "json/github/exceeded-github-api-rate-limit-403.json";
    private static final String APP_NO_USER_REPOS_FOUND_PATH = "json/app/user-not-found-404.json";
    private static final String MULTIPLE_BRANCHES_PATH = "json/github/branches-multiple.json";
    private static final String SINGLE_BRANCH_PATH = "json/github/branches-single-master.json";
    private static final String INVALID_HEADER_DATA_TYPE_PATH = "json/app/invalid-data-type-406.json";
    private static final String VALID_USERNAME = "tech-solutions-inc";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
    }

    @Test
    void shouldFetchUserRepositoriesWithBranches() throws Exception {
        String multipleBranchesRepositoryName = "microservices-architecture-template";
        String singleBranchRepositoryName = "reactive-data-streamer";
        String expectedJson = readJsonFile(EXPECTED_USER_REPOS_WITH_BRANCHES_PATH);

        stubFor(get(urlPathEqualTo("/users/" + VALID_USERNAME + "/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile(USER_REPOS_PATH)));

        stubFor(get(urlPathMatching(
                "/repos/" + VALID_USERNAME + "/" + multipleBranchesRepositoryName + "/branches"
        ))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile(MULTIPLE_BRANCHES_PATH)));

        stubFor(get(urlPathMatching(
                "/repos/" + VALID_USERNAME + "/" + singleBranchRepositoryName + "/branches"
        ))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile(SINGLE_BRANCH_PATH)));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/github/user/repos")
                        .param("username", VALID_USERNAME))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }

    @Test
    void shouldReturn404WhenUserNotFound() throws Exception {
        String invalidUsername = "vYF0o8P0oaxFemnUDjZb";
        String expectedJson = readJsonFile(APP_NO_USER_REPOS_FOUND_PATH);

        stubFor(get(urlPathEqualTo("/users/" + invalidUsername + "/repos"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile(GITHUB_NO_USER_REPOS_FOUND_PATH)));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/github/user/repos")
                        .param("username", invalidUsername))
                .andExpect(status().isNotFound())
                .andExpect(content().json(expectedJson));
    }

    @Test
    void shouldReturn406ForInvalidAcceptHeader() throws Exception {
        String multipleBranchesRepositoryName = "microservices-architecture-template";
        String singleBranchRepositoryName = "reactive-data-streamer";

        stubFor(get(urlPathEqualTo("/users/" + VALID_USERNAME + "/repos"))
                .willReturn(okJson("[]")));

        stubFor(get(urlPathMatching(
                "/repos/" + VALID_USERNAME + "/" + multipleBranchesRepositoryName + "/branches"
        ))
                .willReturn(okJson("[]")));

        stubFor(get(urlPathMatching(
                "/repos/" + VALID_USERNAME + "/" + singleBranchRepositoryName + "/branches"
        ))
                .willReturn(okJson("[]")));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/github/user/repos")
                        .param("username", VALID_USERNAME)
                        .header("Accept", "application/xml"))
                .andExpect(status().isNotAcceptable())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(readJsonFile(INVALID_HEADER_DATA_TYPE_PATH)));
    }

    @Test
    void shouldReturn403WhenGitHubApiRateLimitExceeded() throws Exception {
        stubFor(get(urlPathEqualTo("/users/" + VALID_USERNAME + "/repos"))
                .willReturn(aResponse()
                        .withStatus(403)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readJsonFile(GITHUB_EXCEEDED_API_RATE_LIMIT_PATH))));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/github/user/repos")
                        .param("username", VALID_USERNAME))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", containsString("API rate limit exceeded for")));
    }

    private String readJsonFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get("src/test/resources/__files/" + path)));
    }

}
