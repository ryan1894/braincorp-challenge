package com.braincorp.challenge.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class UnixUser {
    private final String name;
    private final Integer userId;
    private final Integer groupId;
    private final String comment;
    private final String home;
    private final String shell;

    public UnixUser(String[] line) {
        if (line.length == 7) {
            this.name = line[0];
            // line[1] is password
            this.userId = Integer.parseInt(line[2]);
            this.groupId = Integer.parseInt(line[3]);
            this.comment = line[4];
            this.home = line[5];
            this.shell = line[6];
        } else {
            this.name = line[0];
            // line[1] is password
            this.userId = Integer.parseInt(line[2]);
            this.groupId = Integer.parseInt(line[3]);
            this.comment = line[4];
            this.home = line[5];
            this.shell = null;
        }
    }

    public UnixUser(String name, Integer userId, int groupId, String comment, String home, String shell) {
        this.name = name;
        this.userId = userId;
        this.groupId = groupId;
        this.comment = comment;
        this.home = home;
        this.shell = shell;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("uid")
    public Integer getUserId() {
        return userId;
    }

    @JsonProperty("gid")
    public Integer getGroupId() {
        return groupId;
    }

    @JsonProperty("comment")
    public String getComment() {
        return comment;
    }

    @JsonProperty("home")
    public String getHome() {
        return home;
    }

    @JsonProperty("shell")
    public String getShell() {
        return shell;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnixUser unixUser = (UnixUser) o;
        return Objects.equals(name, unixUser.name) &&
            Objects.equals(userId, unixUser.userId) &&
            Objects.equals(groupId, unixUser.groupId) &&
            Objects.equals(comment, unixUser.comment) &&
            Objects.equals(home, unixUser.home) &&
            Objects.equals(shell, unixUser.shell);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, userId, groupId, comment, home, shell);
    }
}
