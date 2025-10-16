package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody UserDto userDto) {
        log.info("Creating user {}, email {}", userDto.getName(), userDto.getEmail());
        return userClient.create(userDto);
    }

    @GetMapping("{id}")
    public ResponseEntity<Object> read(@PathVariable long id) {
        log.info("Get user id {}", id);
        return userClient.getUser(id);
    }

    @PatchMapping("{id}")
    public ResponseEntity<Object> update(@Valid @RequestBody UserUpdateDto userUpdateDto, @PathVariable long id) {
        log.info("Updating user id {}: name {}, email {}", id, userUpdateDto.getName(), userUpdateDto.getEmail());
        return userClient.update(userUpdateDto, id);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable long id) {
        log.info("Delete user id {}", id);
        userClient.delete(id);
    }
}
