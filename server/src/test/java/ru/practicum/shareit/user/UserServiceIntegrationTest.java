package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.common.exceptions.NotFoundException;
import ru.practicum.shareit.common.exceptions.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.service.UserService;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceIntegrationTest {
    private final UserService userService;

    @Test
    void createShouldCorrectlySaveUser() {
        UserDto userDto1 = new UserDto();
        userDto1.setName("name1");
        userDto1.setEmail("email@email.ru");
        userDto1 = userService.create(userDto1);

        UserDto userDtoFromDb = userService.get(userDto1.getId());

        assertThat(userDtoFromDb, notNullValue());
        assertThat(userDtoFromDb.getId(), equalTo(userDto1.getId()));
        assertThat(userDtoFromDb.getName(), equalTo(userDto1.getName()));
        assertThat(userDtoFromDb.getEmail(), equalTo(userDto1.getEmail()));
    }

    @Test
    void createShouldThrowValidationExceptionIfEmailExists() {
        UserDto userDto1 = new UserDto();
        userDto1.setName("name1");
        userDto1.setEmail("email@email.ru");
        userService.create(userDto1);

        UserDto userDto2 = new UserDto();
        userDto2.setName("name2");
        userDto2.setEmail("email@email.ru");
        assertThrows(ValidationException.class, () -> {
            userService.create(userDto2);
        });
    }

    @Test
    void getShouldThrowNotFoundExceptionIfUserDoesNotExist() {
        assertThrows(NotFoundException.class, () -> {
            userService.get(1);
        });
    }

    @Test
    void updateThrowValidationExceptionIfEmailExists() {
        UserDto userDto1 = new UserDto();
        userDto1.setName("name1");
        userDto1.setEmail("email@email.ru");
        userService.create(userDto1);

        UserDto userDto2 = new UserDto();
        userDto2.setName("name2");
        userDto2.setEmail("email2@email.ru");
        userDto2 = userService.create(userDto2);
        long userDto2Id = userDto2.getId();

        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setName("name3");
        updateDto.setEmail("email@email.ru");

        assertThrows(ValidationException.class, () -> {
            userService.update(updateDto, userDto2Id);
        });
    }

    @Test
    void updateShouldThrowNotFoundExceptionIfUserDoesNotExist() {
        assertThrows(NotFoundException.class, () -> {
            userService.get(1);
        });
    }

    @Test
    void updateShouldReturnChangedDto() {
        UserDto userDto1 = new UserDto();
        userDto1.setName("name1");
        userDto1.setEmail("email@email.ru");
        userDto1 = userService.create(userDto1);

        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setName("name3");
        updateDto.setEmail("newEmail@email.ru");

        UserDto updatedDto = userService.update(updateDto, userDto1.getId());

        assertThat(updatedDto, notNullValue());
        assertThat(updatedDto.getId(), equalTo(userDto1.getId()));
        assertThat(updatedDto.getName(), equalTo(updateDto.getName()));
        assertThat(updatedDto.getEmail(), equalTo(updateDto.getEmail()));
    }

    @Test
    void deleteShouldThrowNotFoundExceptionIfUserDoesNotExist() {
        assertThrows(NotFoundException.class, () -> {
            userService.delete(1);
        });
    }

    @Test
    void deleteShouldRemoveUserFromDb() {
        UserDto userDto1 = new UserDto();
        userDto1.setName("name1");
        userDto1.setEmail("email@email.ru");
        userDto1 = userService.create(userDto1);
        long userId = userDto1.getId();

        userService.delete(userId);

        assertThrows(NotFoundException.class, () -> {
            userService.delete(userId);
        });
    }
}
