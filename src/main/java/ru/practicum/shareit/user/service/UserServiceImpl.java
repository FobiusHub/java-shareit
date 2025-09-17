package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.common.exceptions.NotFoundException;
import ru.practicum.shareit.common.exceptions.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto create(User user) {
        checkEmailExist(user);
        return UserMapper.toUserDto(userRepository.add(user));
    }

    @Override
    public UserDto get(long id) {
        checkUserExist(id);
        return UserMapper.toUserDto(userRepository.get(id));
    }

    @Override
    public UserDto update(UserDto newUserData, long id) {
        checkUserExist(id);
        if (newUserData.getEmail() != null) {
            checkEmailExist(UserMapper.toUser(newUserData));
        }
        return UserMapper.toUserDto(userRepository.update(newUserData, id));
    }

    @Override
    public void delete(long id) {
        checkUserExist(id);
        userRepository.remove(id);
    }

    @Override
    public void checkUserExist(long userId) {
        if (!userRepository.userExists(userId)) {
            log.warn("При запросе данных пользователя возникла ошибка: Пользователь не найден");
            throw new NotFoundException("Пользователь " + userId + " не найден");
        }
    }

    private void checkEmailExist(User user) {
        if (userRepository.emailExists(user)) {
            log.warn("При проверке email возникла ошибка: email уже существует");
            throw new ValidationException("Email " + user.getEmail() + " уже существует");
        }
    }
}
