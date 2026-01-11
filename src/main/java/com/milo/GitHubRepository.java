package com.milo;

import java.util.List;

import static com.milo.GitHubRepositoryInfo.Owner;

public class GitHubRepository {

    private String name;
    private Owner owner;
    private List<GitHubRepositoryBranch> branches;

    public GitHubRepository(String name, Owner owner, List<GitHubRepositoryBranch> branches) {
        this.name = name;
        this.owner = owner;
        this.branches = branches;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public List<GitHubRepositoryBranch> getBranches() {
        return branches;
    }

    public void setBranches(List<GitHubRepositoryBranch> branches) {
        this.branches = branches;
    }
}
