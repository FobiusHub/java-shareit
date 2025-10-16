package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

public interface UserService {
    UserDto create(UserDto userDto);

    UserDto get(long userId);

    UserDto update(UserUpdateDto userUpdateDto, long userId);

    void delete(long userId);

    void checkUserExist(long userId);
}
