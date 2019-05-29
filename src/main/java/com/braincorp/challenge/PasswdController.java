package com.braincorp.challenge;

import com.braincorp.challenge.model.UnixGroup;
import com.braincorp.challenge.model.UnixGroups;
import com.braincorp.challenge.model.UnixUser;
import com.braincorp.challenge.model.UnixUsers;
import com.braincorp.challenge.service.PasswdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

@RestController
public class PasswdController {
    private final PasswdService passwdService;

    @Autowired
    public PasswdController(PasswdService passwdService) {
        this.passwdService = passwdService;
    }

    @GetMapping(path = "/users")
    public CompletionStage<ResponseEntity<UnixUsers>> getAllUnixUsers() {
        return passwdService.getAllUnixUsers()
            .thenApply(ResponseEntity::ok);
    }

    @GetMapping(path = "/users/query")
    public CompletionStage<ResponseEntity<UnixUsers>> getUnixUsersWithFilter(@RequestParam(name = "name", required = false) String name,
                                                             @RequestParam(name = "uid", required = false)  Integer userId,
                                                             @RequestParam(name = "gid", required = false)  Integer groupId,
                                                             @RequestParam(name = "comment", required = false) String comment,
                                                             @RequestParam(name = "home", required = false)  String home,
                                                             @RequestParam(name = "shell", required = false)  String shell
    ) {
        return passwdService.getUnixUsersWithFilter(name, userId, groupId, comment, home, shell)
            .thenApply(ResponseEntity::ok);
    }

    @GetMapping(path = "/users/{userId}")
    public CompletionStage<ResponseEntity<UnixUser>> getUnixUserByUserId(@PathVariable(name = "userId") Integer userId) {
        return passwdService.getUnixUserByUserId(userId)
            .thenApply(x ->
                x
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build())
            );
    }

    @GetMapping(path = "/users/{userId}/groups")
    public CompletionStage<ResponseEntity<UnixGroups>> getUnixUserGroupsByUser(@PathVariable(name = "userId") Integer userId) {
        return passwdService.getUnixUserGroupsByUser(userId)
            .thenApply(ResponseEntity::ok);
    }

    @GetMapping(path = "/groups")
    public CompletionStage<ResponseEntity<UnixGroups>> getAllUnixUserGroups() {
        return passwdService.getAllUnixUserGroups()
            .thenApply(ResponseEntity::ok);

    }

    @GetMapping(path = "/groups/query")
    public CompletionStage<ResponseEntity<UnixGroups>> getAllUnixGroupsWithFilter(@RequestParam(name = "name", required = false) String groupName,
                                                                  @RequestParam(name = "gid", required = false) Integer groupId,
                                                                  @RequestParam(name = "member", required = false) String[] members
    ) {
        return passwdService.getAllUnixGroupsWithFilter(groupName, groupId, members)
            .thenApply(ResponseEntity::ok);
    }

    @GetMapping(path = "/groups/{groupId}")
    public CompletionStage<ResponseEntity<UnixGroup>> getUnixGroupById(@PathVariable(name = "groupId") Integer groupId) {
        return passwdService.getUnixGroupById(groupId)
            .thenApply(x ->
                x
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build())
            );
    }
}
