package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

public interface UserService {
    UserDto create(UserDto userDto);

    UserDto get(long id);

    UserDto update(UserDto newUserData, long id);

    void delete(long id);

    void checkUserExist(long userId);

}
