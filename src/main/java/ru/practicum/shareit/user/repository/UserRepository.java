package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

public interface UserRepository {
    User add(User user);

    User get(long id);

    User update(UserDto userDto, long id);

    void remove(long id);

    boolean userExists(long id);

    boolean emailExists(User user);
}
