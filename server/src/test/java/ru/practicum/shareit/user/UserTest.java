package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class UserTest {
    @Test
    void usersShouldBeEquals() {
        User user1 = new User();
        user1.setId(1);
        user1.setName("name");

        User user2 = new User();
        user2.setId(1);
        user2.setName("name");

        assertTrue(user1.equals(user2));
    }

    @Test
    void usersAreNotEquals() {
        User user1 = new User();
        user1.setId(1);
        user1.setName("name");

        User user2 = new User();
        user2.setId(2);
        user2.setName("name2");

        assertFalse(user1.equals(user2));
    }

    @Test
    void equalsReturnFalseIfNotUser() {
        User user1 = new User();
        user1.setId(1);
        user1.setName("name");

        List<User> userList = List.of();

        assertFalse(user1.equals(userList));
    }
}
