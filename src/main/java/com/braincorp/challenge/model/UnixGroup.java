package com.braincorp.challenge.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class UnixGroup {
    private final Integer id;
    private final String name;
    private final List<String> memberNames;

    public UnixGroup(String[] line) {
        this.name = line[0];
        // line[1] is password
        this.id = Integer.parseInt(line[2]);
        if (line.length == 3) {
            this.memberNames = Collections.emptyList();
        } else {
            this.memberNames = Arrays.stream(line[3].split(","))
                .collect(Collectors.toList());
        }
    }


    public UnixGroup(Integer id, String name, List<String> memberNames) {
        this.id = id;
        this.name = name;
        this.memberNames = memberNames;
    }

    @JsonProperty("gid")
    public Integer getId() {
        return id;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("members")
    public List<String> getMemberNames() {
        return memberNames;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnixGroup unixGroup = (UnixGroup) o;
        return Objects.equals(id, unixGroup.id) &&
            Objects.equals(name, unixGroup.name) &&
            Objects.equals(memberNames, unixGroup.memberNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, memberNames);
    }
}
