package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Map;
import java.util.HashMap;

@Component
public class InMemoryUserRepository implements UserRepository {
    private long id = 0;
    private final Map<Long, User> users = new HashMap<>();


    @Override
    public User add(User user) {
        id++;
        user.setId(id);
        users.put(id, user);
        return user;
    }

    @Override
    public User get(long id) {
        return users.getOrDefault(id, null);
    }

    @Override
    public User update(UserDto newUserData, long id) {
        User user = users.get(id);

        String newName = newUserData.getName();
        if (newName != null) {
            user.setName(newName);
        }

        String email = newUserData.getEmail();
        if (email != null) {
            user.setEmail(email);
        }

        return user;
    }

    @Override
    public void remove(long id) {
        users.remove(id);
    }

    @Override
    public boolean userExists(long id) {
        return users.containsKey(id);
    }

    @Override
    public boolean emailExists(User user) {
        return users.containsValue(user);
    }
}
