# GitHub Repositories API

A Spring Boot-based REST API that fetches and aggregates information about GitHub repositories for a given user. This project was developed as part of a recruitment task.

## Key Features

- **Non-fork Repository Listing**: Fetches all public repositories that are not forks.
- **Branch Aggregation**: Automatically lists all branches with their latest commit SHA for each repository.
- **Asynchronous Processing**: Uses `CompletableFuture` to fetch branches in parallel, significantly improving performance for users with many repositories.

## Tech Stack

- **Java 25**
- **Spring Boot 4.0.1**
- **Gradle**
- **Jackson**
- **WireMock**
- **JUnit 5 & MockMvc**

## API Documentation

### 1. Get User Repositories
Returns a list of public, non-forked repositories for a specific user.

- **URL:** `/api/v1/github/user/repos`
- **Method:** `GET`
- **Params:** `username=[string]`
- **Required Header:** `Accept: application/json`

#### Success Response (200 OK)
```json
[
  {
    "name": "microservices-architecture-template",
    "owner": {
      "login": "tech-solutions-inc"
    },
    "branches": [
      {
        "name": "main",
        "commit": {
          "sha": "7d2f4a5b6c7d8e9f0a1b2c3d4e5f6a7b8c9d0e1f"
        }
      },
      {
        "name": "develop",
        "commit": {
          "sha": "1a2b3c4d5e6f7g8h9i0j1k2l3m4n5o6p7q8r9s0t"
        }
      }
    ]
  },
  {
    "name": "reactive-data-streamer",
    "owner": {
      "login": "tech-solutions-inc"
    },
    "branches": [
      {
        "name": "master",
        "commit": {
          "sha": "b5a4c3d2e1f0a9b8c7d6e5f4g3h2i1j0k9l8m7n"
        }
      }
    ]
  }
]
```
#### Not Found Response (404 Not Found)
```json
{
  "status": 404,
  "message": "Not Found"
}
```

#### Invalid Data Type (406 Not Acceptable)
```json
{
  "status": 406,
  "message": "Not Acceptable"
}
```

#### GitHub API Rate Limit Exceeded (403 Forbidden)

```json
{
  "status": 403,
  "message": "API rate limit exceeded for [user-ip-address]. (But here's the good news: Authenticated requests get a higher rate limit. Check out the documentation for more details.)"
}
```





