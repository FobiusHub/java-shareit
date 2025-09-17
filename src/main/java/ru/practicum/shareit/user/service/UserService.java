package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

public interface UserService {
    UserDto create(User user);

    UserDto get(long id);

    UserDto update(UserDto newUserData, long id);

    void delete(long id);

    void checkUserExist(long userId);

}
