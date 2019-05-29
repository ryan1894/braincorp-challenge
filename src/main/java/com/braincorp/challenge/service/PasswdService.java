package com.braincorp.challenge.service;

import com.braincorp.challenge.Application;
import com.braincorp.challenge.model.UnixGroup;
import com.braincorp.challenge.model.UnixGroups;
import com.braincorp.challenge.model.UnixUser;
import com.braincorp.challenge.model.UnixUsers;
import org.apache.commons.io.FileUtils;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Service
public class PasswdService {

    private final ReentrantReadWriteLock readWriteLock;
    private final ReentrantReadWriteLock.WriteLock writeLock;
    private final ReentrantReadWriteLock.ReadLock readLock;
    private UnixUsers unixUsers;
    private UnixGroups unixGroups;

    public PasswdService(@Value("${files.location.passwd:/etc/passwd}") String passwdFileLocation, @Value("${files.location.group:/etc/group}") String groupFileLocation) throws IOException, InterruptedException {
        this.readWriteLock = new ReentrantReadWriteLock();
        this.writeLock = readWriteLock.writeLock();
        this.readLock = readWriteLock.readLock();
        System.out.println(String.format("Using passwd and group files: passwd_file_location=\"%s\", group_file_location=\"%s\"", passwdFileLocation, groupFileLocation));
        Path passwdFile = Paths.get(passwdFileLocation);
        Path groupFile = Paths.get(groupFileLocation);
        loadPasswdFile(passwdFile);
        loadGroupFile(groupFile);
        FileWatcher.watchFile(passwdFile, () -> loadPasswdFile(passwdFile));
        FileWatcher.watchFile(groupFile, () -> loadGroupFile(groupFile));
    }

    public void loadGroupFile(Path groupFile) {
        writeLock.lock();
        try {
            this.unixGroups = new UnixGroups(
                FileUtils.readLines(groupFile.toFile(), Charset.defaultCharset())
                    .stream()
                    .map(line -> line.split(":"))
                    .filter(line -> line.length == 4 || line.length == 3)
                    .map(UnixGroup::new)
                    .collect(Collectors.toList())
            );
        } catch (Exception ex) {
            System.err.println(String.format("Could not read group file! group_file_path=\"%s\", stack_trace=%s", groupFile.toAbsolutePath(), ex.toString()));
            System.exit(1);
        } finally {
            writeLock.unlock();
        }
    }

    public void loadPasswdFile(Path passwdFile) {
        writeLock.lock();
        try {
            this.unixUsers = new UnixUsers(
                FileUtils.readLines(passwdFile.toFile(), Charset.defaultCharset())
                    .stream()
                    .map(line -> line.split(":"))
                    .filter(line -> line.length == 7 || line.length == 6)
                    .map(UnixUser::new)
                    .collect(Collectors.toList())
            );
        } catch (Exception ex) {
            System.err.println(String.format("Could not read passwd file! passwd_file_path=\"%s\", stack_trace=%s", passwdFile.toAbsolutePath(), ex.toString()));
            System.exit(1);
        } finally {
            writeLock.unlock();
        }
    }

    public CompletionStage<UnixUsers> getAllUnixUsers() {
        readLock.lock();
        try {
            return CompletableFuture.completedFuture(unixUsers);
        } finally {
            readLock.unlock();
        }
    }

    public CompletionStage<UnixUsers> getUnixUsersWithFilter(String name, Integer userId, Integer groupId, String comment, String home, String shell) {
        readLock.lock();
        try {
            return CompletableFuture.completedFuture(
                new UnixUsers(
                    unixUsers.getUsers().stream()
                        .filter(
                            user ->
                                (StringUtils.isEmpty(name) || user.getName().equals(name)) &&
                                    (userId == null || user.getUserId().equals(userId)) &&
                                    (groupId == null || user.getGroupId().equals(groupId)) &&
                                    (StringUtils.isEmpty(comment) || user.getComment().equals(comment)) &&
                                    (StringUtils.isEmpty(home) || user.getHome().equals(home)) &&
                                    (StringUtils.isEmpty(shell) || user.getShell().equals(shell))
                        )
                        .collect(Collectors.toList())
                )
            );
        } finally {
            readLock.unlock();
        }
    }

    public CompletionStage<Optional<UnixUser>> getUnixUserByUserId(Integer userId) {
        readLock.lock();
        try {
            return CompletableFuture.completedFuture(unixUsers.getUsers().stream()
                .filter(user -> user.getUserId().equals(userId))
                .findFirst()
            );
        } finally {
            readLock.unlock();
        }
    }

    public CompletionStage<UnixGroups> getUnixUserGroupsByUser(Integer userId) {
        readLock.lock();
        Optional<UnixUser> userOpt = unixUsers.getUsers().stream()
            .filter(u -> u.getUserId().equals(userId))
            .findFirst();
        UnixGroups unixGroups =
            userOpt.map(
                user ->
                    new UnixGroups(
                        this.unixGroups.getGroups().stream()
                            .filter(group -> group.getMemberNames().contains(user.getName()))
                            .collect(Collectors.toList())
                    )

            ).orElse(new UnixGroups());
        try {
            return CompletableFuture.completedFuture(unixGroups);
        } finally {
            readLock.unlock();
        }
    }

    public CompletionStage<UnixGroups> getAllUnixUserGroups() {
        return CompletableFuture.completedFuture(unixGroups);
    }

    public CompletionStage<UnixGroups> getAllUnixGroupsWithFilter(String groupName, Integer groupId, String[] members) {
        readLock.lock();
        try {
            return CompletableFuture.completedFuture(
                new UnixGroups(
                    unixGroups.getGroups().stream()
                    .filter(group ->
                            (StringUtils.isEmpty(groupName) || group.getName().equals(groupName)) &&
                                (groupId == null || group.getId().equals(groupId)) &&
                                    (members == null || group.getMemberNames().containsAll(Lists.newArrayList(members)))
                    )
                    .collect(Collectors.toList())
                )
            );
        } finally {
            readLock.unlock();
        }
    }

    public CompletionStage<Optional<UnixGroup>> getUnixGroupById(Integer groupId) {
        readLock.lock();
        try {
            return CompletableFuture.completedFuture(unixGroups.getGroups().stream()
                .filter(group -> group.getId().equals(groupId))
                .findFirst()
            );
        } finally {
            readLock.unlock();
        }
    }
}
