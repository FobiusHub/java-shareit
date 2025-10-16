package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.service.UserService;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public UserDto create(@RequestBody UserDto userDto) {
        return userService.create(userDto);
    }

    @GetMapping("{id}")
    public UserDto read(@PathVariable long id) {
        return userService.get(id);
    }

    @PatchMapping("{id}")
    public UserDto update(@RequestBody UserUpdateDto userUpdateDto, @PathVariable long id) {
        return userService.update(userUpdateDto, id);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable long id) {
        userService.delete(id);
    }
}
