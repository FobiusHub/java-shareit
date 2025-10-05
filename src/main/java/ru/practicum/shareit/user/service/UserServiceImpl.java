package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.common.exceptions.NotFoundException;
import ru.practicum.shareit.common.exceptions.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto create(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        String email = userDto.getEmail();
        validateEmailUnique(email);
        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    @Override
    public UserDto get(long id) {
        checkUserExist(id);
        //Проверка наличия записи в БД происходит выше, потому в проверке Optional на Present необходимости нет
        return UserMapper.toUserDto(userRepository.findById(id).get());
    }

    @Override
    public UserDto update(UserUpdateDto userUpdateDto, long id) {
        checkUserExist(id);

        String newEmail = userUpdateDto.getEmail();
        if (newEmail != null) {
            validateEmailUnique(newEmail);
        }

        //Проверка наличия записи в БД происходит выше, потому в проверке Optional на Present необходимости нет
        User user = userRepository.findById(id).get();

        user.setEmail(newEmail);

        String newName = userUpdateDto.getName();
        if (newName != null) {
            user.setName(newName);
        }

        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    public void delete(long id) {
        checkUserExist(id);
        userRepository.deleteById(id);
    }

    @Override
    public void checkUserExist(long userId) {
        if (!userRepository.existsById(userId)) {
            log.warn("При запросе данных пользователя возникла ошибка: Пользователь не найден");
            throw new NotFoundException("Пользователь " + userId + " не найден");
        }
    }

    private void validateEmailUnique(String email) {
        if (userRepository.existsByEmail(email)) {
            log.warn("При проверке email возникла ошибка: email уже существует");
            throw new ValidationException("Email " + email + " уже существует");
        }
    }
}
