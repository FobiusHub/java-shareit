package ru.practicum.shareit.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public UserDto create(@Valid @RequestBody User user) {
        return userService.create(user);
    }

    @GetMapping("{id}")
    public UserDto read(@PathVariable long id) {
        return userService.get(id);
    }

    @PatchMapping("{id}")
    public UserDto update(@Valid @RequestBody UserDto userDto, @PathVariable long id) {
        return userService.update(userDto, id);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable long id) {
        userService.delete(id);
    }
}
