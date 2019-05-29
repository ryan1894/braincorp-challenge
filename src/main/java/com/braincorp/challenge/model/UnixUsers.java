package com.braincorp.challenge.model;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.List;
import java.util.Objects;

public class UnixUsers {
    private List<UnixUser> users;

    public UnixUsers(List<UnixUser> users) {
        this.users = users;
    }

    @JsonValue
    public List<UnixUser> getUsers() {
        return users;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnixUsers unixUsers = (UnixUsers) o;
        return Objects.equals(users, unixUsers.users);
    }

    @Override
    public int hashCode() {
        return Objects.hash(users);
    }
}
