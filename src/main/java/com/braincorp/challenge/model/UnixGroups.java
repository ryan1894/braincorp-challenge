package com.braincorp.challenge.model;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UnixGroups {
    private final List<UnixGroup> groups;

    public UnixGroups() {
        this.groups = new ArrayList<>();
    }

    public UnixGroups(List<UnixGroup> groups) {
        this.groups = groups;
    }

    @JsonValue
    public List<UnixGroup> getGroups() {
        return groups;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnixGroups that = (UnixGroups) o;
        return Objects.equals(groups, that.groups);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groups);
    }
}
