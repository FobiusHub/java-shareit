package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.common.controller.ErrorHandler;
import ru.practicum.shareit.common.exceptions.NotFoundException;
import ru.practicum.shareit.common.exceptions.ValidationException;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {
    @Mock
    private UserService service;

    @InjectMocks
    private UserController controller;

    private final ObjectMapper mapper = new ObjectMapper();
    private MockMvc mvc;

    private UserDto userDto;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new ErrorHandler())
                .build();
    }

    /*
    @PostMapping
    public UserDto create(@Valid @RequestBody UserDto userDto) {
        return userService.create(userDto);
    }
    */
    @Test
    void createTest() throws Exception {
        initialize();

        when(service.create(any(UserDto.class))).thenReturn(userDto);

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(userDto)));

        verify(service).create(argThat(dto ->
                dto.getName().equals(userDto.getName()) &&
                        dto.getEmail().equals(userDto.getEmail()))
        );
    }

    @Test
    void createShouldReturnCinflict() throws Exception {
        UserDto user = new UserDto();

        when(service.create(any(UserDto.class))).thenThrow(new ValidationException("Некорректное тело запроса"));

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(user))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Некорректное тело запроса"));
    }

    /*
    @GetMapping("{id}")
    public UserDto read(@PathVariable long id) {
        return userService.get(id);
    }
    */
    @Test
    void readTest() throws Exception {
        initialize();

        when(service.get(anyLong())).thenReturn(userDto);

        mvc.perform(get("/users/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(userDto)));

        verify(service).get(1);
    }

    @Test
    void readShouldReturnNotFound() throws Exception {
        initialize();

        when(service.get(anyLong())).thenThrow(new NotFoundException("Пользователь не найден"));

        mvc.perform(get("/users/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь не найден"));
    }

    /*
    @PatchMapping("{id}")
    public UserDto update(@Valid @RequestBody UserUpdateDto userUpdateDto, @PathVariable long id) {
        return userService.update(userUpdateDto, id);
    }
    */
    @Test
    void updateTest() throws Exception {
        initialize();

        UserUpdateDto userUpdateDto = new UserUpdateDto();
        userUpdateDto.setName("newName");
        userUpdateDto.setEmail("new@email.ru");

        userDto.setName(userUpdateDto.getName());
        userDto.setEmail(userUpdateDto.getEmail());

        when(service.update(any(UserUpdateDto.class), anyLong())).thenReturn(userDto);

        mvc.perform(patch("/users/1")
                        .content(mapper.writeValueAsString(userUpdateDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(userDto)));

        // Проверка вызова сервиса с правильными параметрами
        verify(service).update(userUpdateDto, 1);
    }

    /*
    @DeleteMapping("{id}")
    public void delete(@PathVariable long id) {
        userService.delete(id);
    }
    */
    @Test
    void deleteTest() throws Exception {
        initialize();

        doNothing().when(service).delete(anyLong());

        mvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        // Проверка вызова сервиса с правильными параметрами
        verify(service).delete(1);
    }

    private void initialize() {
        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("userName");
        userDto.setEmail("user@email.ru");
    }
}
