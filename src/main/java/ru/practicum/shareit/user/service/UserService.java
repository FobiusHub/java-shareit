package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

public interface UserService {
    UserDto create(UserDto userDto);

    UserDto get(long id);

    UserDto update(UserUpdateDto userUpdateDto, long id);

    void delete(long id);

    void checkUserExist(long userId);
}
