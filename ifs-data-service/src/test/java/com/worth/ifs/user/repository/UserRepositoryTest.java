package com.worth.ifs.user.repository;

import com.worth.ifs.BaseRepositoryIntegrationTest;
import com.worth.ifs.user.domain.ProcessRole;
import com.worth.ifs.user.domain.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;

/**
 * Created by dwatson on 02/10/15.
 */
public class UserRepositoryTest extends BaseRepositoryIntegrationTest<UserRepository> {

    @Override
    @Autowired
    protected void setRepository(UserRepository repository) {
        this.repository = repository;
    }

    @Test
    public void test_findAll() {

        //
        // Fetch the list of users
        //
        List<User> users = repository.findAll();
        assertEquals(6, users.size());

        //
        // Assert that we've got the users we were expecting
        //
        List<String> emailAddresses = users.stream().map(User::getEmail).collect(toList());
        List<String> expectedUsers = asList("steve.smith@empire.com", "jessica.doe@ludlow.co.uk", "paul.plum@gmail.com", "competitions@innovateuk.gov.uk", "finance@innovateuk.gov.uk", "pete.tom@egg.com");
        assertTrue(emailAddresses.containsAll(expectedUsers));
    }

    @Test
    @Transactional
    @Rollback
    public void test_createUser() {

        //
        // Create a new user
        //
        User newUser = repository.save(new User("New User", "new@example.com", "apassword", "worthsystemsandhiveittogetheratlast", "", new ArrayList<ProcessRole>()));
        assertNotNull(newUser.getId());

        //
        // Fetch the list of users and assert that the count has increased and the new user is present in the list of expected users
        //
        List<User> users = repository.findAll();
        assertEquals(7, users.size());
        List<String> emailAddresses = users.stream().map(User::getEmail).collect(toList());
        List<String> expectedUsers = asList("steve.smith@empire.com", "jessica.doe@ludlow.co.uk", "paul.plum@gmail.com", "competitions@innovateuk.gov.uk", "finance@innovateuk.gov.uk", "pete.tom@egg.com", "new@example.com");
        assertTrue(emailAddresses.containsAll(expectedUsers));
    }

    @Test
    @Transactional
    @Rollback
    public void test_deleteNewUser() {

        //
        // Create a new user
        //
        User newUser = repository.save(new User("New User", "new@example.com", "apassword", "worthsystemsandhiveittogetheratlast", "", new ArrayList<ProcessRole>()));

        //
        // and immediately delete them
        //
        repository.delete(newUser.getId());

        //
        // Fetch the list of users and assert that the user doesn't exist in this list
        //
        List<User> users = repository.findAll();
        assertEquals(6, users.size());
        List<String> emailAddresses = users.stream().map(User::getEmail).collect(toList());
        assertFalse(emailAddresses.contains("new@example.com"));
    }
}
