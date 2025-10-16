package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.ShortUserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Не тестирую existsByEmail, т.к. он косвенно проверяется в UserServiceIntegrationTest
 */
@Transactional
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserRepositoryTest {
    private final UserRepository userRepository;

    @Test
    void findShortUserDtoByIdShouldReturnCorrectDto() {
        User user = new User();
        user.setName("name");
        user.setEmail("email@email.ru");
        user = userRepository.save(user);

        long userId = user.getId();

        ShortUserDto result = userRepository.findShortUserDtoById(userId);

        assertThat(result, notNullValue());
        assertThat(result.getId(), equalTo(userId));
        assertThat(result.getName(), equalTo(user.getName()));
    }

    @Test
    void findShortUserDtoByIdShouldReturnNullIfUserDoesNotExist() {
        ShortUserDto result = userRepository.findShortUserDtoById(1);

        assertThat(result, equalTo(null));
    }

}
