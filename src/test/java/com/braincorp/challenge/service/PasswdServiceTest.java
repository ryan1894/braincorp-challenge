package com.braincorp.challenge.service;

import com.braincorp.challenge.model.UnixGroup;
import com.braincorp.challenge.model.UnixGroups;
import com.braincorp.challenge.model.UnixUser;
import com.braincorp.challenge.model.UnixUsers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class PasswdServiceTest {
    private PasswdService passwdService;

    @Value("${files.location.passwd:/etc/passwd}")
    private String passwdFileLocation;

    @Value("${files.location.group:/etc/group}")
    private String groupFileLocation;

    @Before
    public void before() throws Exception {
        passwdService = new PasswdService(passwdFileLocation, groupFileLocation);
    }

    @Test
    public void whenGetAllUsersThenReturnAllUsers() throws Exception {
        UnixUsers expected = new UnixUsers(
            List.of(
                new UnixUser("root", 0, 0, "root", "/root", "/bin/bash"),
                new UnixUser("daemon", 1, 1, "daemon", "/usr/sbin", "/usr/sbin/nologin"),
                new UnixUser("bin", 2, 2, "bin", "/bin", "/usr/sbin/nologin"),
                new UnixUser("sys", 3, 3, "sys", "/dev", "/usr/sbin/nologin"),
                new UnixUser("ryan1894", 1000, 1000, ",,,", "/home/ryan1894", "/bin/bash")
            )
        );
        UnixUsers actual = passwdService.getAllUnixUsers().toCompletableFuture().get();

        Assert.assertEquals("When all users are requested, then all users are returned", expected, actual);
    }

    @Test
    public void whenQueryUserByParamThenExpectResultsCorrectlyFiltered() throws Exception {
        UnixUsers expected;
        UnixUsers actual;
        expected = new UnixUsers(
            List.of(
                new UnixUser("root", 0, 0, "root", "/root", "/bin/bash")
            )
        );
        actual = passwdService.getUnixUsersWithFilter("root", null, null, null, null, null).toCompletableFuture().get();
        Assert.assertEquals("When users with name 'root' is requested, expect one entry with name 'root'", expected, actual);

        expected = new UnixUsers(
            List.of(
                new UnixUser("ryan1894", 1000, 1000, ",,,", "/home/ryan1894", "/bin/bash")
            )
        );
        actual = passwdService.getUnixUsersWithFilter("ryan1894", null, null, null, null, null).toCompletableFuture().get();
        Assert.assertEquals("When users with name 'ryan1894' is requested, expect one entry with name 'ryan1894'", expected, actual);

        expected = new UnixUsers(
            List.of(
                new UnixUser("daemon", 1, 1, "daemon", "/usr/sbin", "/usr/sbin/nologin"),
                new UnixUser("bin", 2, 2, "bin", "/bin", "/usr/sbin/nologin"),
                new UnixUser("sys", 3, 3, "sys", "/dev", "/usr/sbin/nologin")
            )
        );
        actual = passwdService.getUnixUsersWithFilter(null, null, null, null, null, "/usr/sbin/nologin").toCompletableFuture().get();
        Assert.assertEquals("When users with shell '/usr/sbin/nologin' requested, return three users with such shell", expected, actual);

        expected = new UnixUsers(
            List.of(
                new UnixUser("root", 0, 0, "root", "/root", "/bin/bash"),
                new UnixUser("ryan1894", 1000, 1000, ",,,", "/home/ryan1894", "/bin/bash")
            )
        );
        actual = passwdService.getUnixUsersWithFilter(null, null, null, null, null, "/bin/bash").toCompletableFuture().get();
        Assert.assertEquals("When users with shell '/bin/bash' requested, return two users with such shell", expected, actual);

        expected = new UnixUsers(
            List.of(
                new UnixUser("sys", 3, 3, "sys", "/dev", "/usr/sbin/nologin")
            )
        );
        actual = passwdService.getUnixUsersWithFilter(null, 3, 3, "sys", null, null).toCompletableFuture().get();
        Assert.assertEquals("When users with uid 3, gid 3, and comment 'sys' requested, return one such user", expected, actual);

        expected = new UnixUsers(
            List.of()
        );
        actual = passwdService.getUnixUsersWithFilter("ryan1894", 3, 3, "sys", null, null).toCompletableFuture().get();
        Assert.assertEquals("When users with name 'ryan1894', uid 3, gid 3, and comment 'sys' requested, return no such user", expected, actual);
    }

    @Test
    public void whenQueryUserByUserIdThenExpectSingleUserOrEmpty() throws Exception {
        Optional<UnixUser> expected;
        Optional<UnixUser> actual;
        expected = Optional.of(
            new UnixUser("root", 0, 0, "root", "/root", "/bin/bash")
        );
        actual = passwdService.getUnixUserByUserId(0).toCompletableFuture().get();
        Assert.assertEquals("When user with id 0 requested, return one user with name 'root'", expected, actual);

        expected = Optional.of(
            new UnixUser("ryan1894", 1000, 1000, ",,,", "/home/ryan1894", "/bin/bash")
        );
        actual = passwdService.getUnixUserByUserId(1000).toCompletableFuture().get();
        Assert.assertEquals("When user with id 1000 requested, return one user with name 'ryan1894'", expected, actual);

        expected = Optional.empty();
        actual = passwdService.getUnixUserByUserId(-1).toCompletableFuture().get();
        Assert.assertEquals("When user with id -1 requested, return no such user", expected, actual);
    }

    @Test
    public void whenQueryUserIdGroupsThenExpectUserGroupsOrEmptyList() throws Exception {
        UnixGroups expected;
        UnixGroups actual;
        expected = new UnixGroups(
            List.of(
                new UnixGroup(4, "adm", List.of("syslog", "ryan1894"))
            )
        );
        actual = passwdService.getUnixUserGroupsByUser(1000).toCompletableFuture().get();
        Assert.assertEquals("When groups for user with id 1000 requested, return one group with name 'adm'", expected, actual);

        expected = new UnixGroups(List.of());
        actual = passwdService.getUnixUserGroupsByUser(-1).toCompletableFuture().get();
        Assert.assertEquals("When groups for user with id -1 requested, return an empty list of groups", expected, actual);

        expected = new UnixGroups(List.of());
        actual = passwdService.getUnixUserGroupsByUser(0).toCompletableFuture().get();
        Assert.assertEquals("When groups for user with id 0 requested, return an empty list of groups", expected, actual);
    }

    @Test
    public void whenQueryAllGroupsThenExpectAllGroups() throws Exception {
        UnixGroups expected;
        UnixGroups actual;
        expected = new UnixGroups(
            List.of(
                new UnixGroup(0, "root", List.of()),
                new UnixGroup(1, "daemon", List.of()),
                new UnixGroup(2, "bin", List.of()),
                new UnixGroup(3, "sys", List.of()),
                new UnixGroup(4, "adm", List.of("syslog", "ryan1894"))
            )
        );
        actual = passwdService.getAllUnixUserGroups().toCompletableFuture().get();
        Assert.assertEquals("When all groups requested, return all groups", expected, actual);
    }

    @Test
    public void whenQueryAllGroupsWithFilterThenExpectFilteredResultGroups() throws Exception {
        UnixGroups expected;
        UnixGroups actual;
        expected = new UnixGroups(
            List.of(
                new UnixGroup(0, "root", List.of()),
                new UnixGroup(1, "daemon", List.of()),
                new UnixGroup(2, "bin", List.of()),
                new UnixGroup(3, "sys", List.of()),
                new UnixGroup(4, "adm", List.of("syslog", "ryan1894"))
            )
        );
        actual = passwdService.getAllUnixGroupsWithFilter(null, null, null).toCompletableFuture().get();
        Assert.assertEquals("When requesting groups with filter with null fields, return all groups", expected, actual);

        expected = new UnixGroups(
            List.of(
                new UnixGroup(0, "root", List.of())
            )
        );
        actual = passwdService.getAllUnixGroupsWithFilter("root", null, null).toCompletableFuture().get();
        Assert.assertEquals("When requesting groups with name filter 'root', return one group", expected, actual);

        expected = new UnixGroups(
            List.of(
                new UnixGroup(4, "adm", List.of("syslog", "ryan1894"))
            )
        );
        actual = passwdService.getAllUnixGroupsWithFilter("adm", null, null).toCompletableFuture().get();
        Assert.assertEquals("When requesting groups with name filter 'adm', return one group", expected, actual);

        expected = new UnixGroups(
            List.of(
                new UnixGroup(4, "adm", List.of("syslog", "ryan1894"))
            )
        );
        actual = passwdService.getAllUnixGroupsWithFilter("adm", 4, null).toCompletableFuture().get();
        Assert.assertEquals("When requesting groups with name filter 'adm' and gid filter 4, return one group", expected, actual);

        expected = new UnixGroups(
            List.of(
                new UnixGroup(4, "adm", List.of("syslog", "ryan1894"))
            )
        );
        actual = passwdService.getAllUnixGroupsWithFilter("adm", null, new String[] {"syslog"}).toCompletableFuture().get();
        Assert.assertEquals("When requesting groups with name filter 'adm' and member filter of {'syslog'}, return one group", expected, actual);

        expected = new UnixGroups(
            List.of(
                new UnixGroup(4, "adm", List.of("syslog", "ryan1894"))
            )
        );
        actual = passwdService.getAllUnixGroupsWithFilter("adm", null, new String[] {"ryan1894"}).toCompletableFuture().get();
        Assert.assertEquals("When requesting groups with name filter 'adm' and member filter of {'ryan1894'}, return one group", expected, actual);

        expected = new UnixGroups(
            List.of(
                new UnixGroup(4, "adm", List.of("syslog", "ryan1894"))
            )
        );
        actual = passwdService.getAllUnixGroupsWithFilter("adm", null, new String[] {"ryan1894", "syslog"}).toCompletableFuture().get();
        Assert.assertEquals("When requesting groups with name filter 'adm' and member filter of {'ryan1894', 'syslog'}, return one group", expected, actual);

        expected = new UnixGroups(List.of());
        actual = passwdService.getAllUnixGroupsWithFilter("adm", null, new String[] {"non_member"}).toCompletableFuture().get();
        Assert.assertEquals("When requesting groups with name filter 'adm' and member filter of {'non_member'}, return no such group", expected, actual);
    }
}
