package user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.user.UserClient;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {
    @Mock
    private UserClient client;

    @InjectMocks
    private UserController controller;

    private final ObjectMapper mapper = new ObjectMapper();
    private MockMvc mvc;

    private UserDto userDto;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    /*
    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody UserDto userDto) {
        log.info("Creating user {}, email {}", userDto.getName(), userDto.getEmail());
        return userClient.create(userDto);
    }
    */
    @Test
    void createTest() throws Exception {
        initialize();

        when(client.create(any(UserDto.class))).thenReturn(ResponseEntity.ok(userDto));

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(userDto)));

        verify(client).create(argThat(dto ->
                dto.getName().equals(userDto.getName()) &&
                        dto.getEmail().equals(userDto.getEmail()))
        );
    }

    /*
    @GetMapping("{id}")
    public ResponseEntity<Object> read(@PathVariable long id) {
        log.info("Get user id {}", id);
        return userClient.getUser(id);
    }
    */
    @Test
    void readTest() throws Exception {
        initialize();

        when(client.getUser(anyLong())).thenReturn(ResponseEntity.ok(userDto));

        mvc.perform(get("/users/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(userDto)));

        verify(client).getUser(1);
    }

    /*
    @PatchMapping("{id}")
    public ResponseEntity<Object> update(@Valid @RequestBody UserUpdateDto userUpdateDto, @PathVariable long id) {
        log.info("Updating user id {}: name {}, email {}", id, userUpdateDto.getName(), userUpdateDto.getEmail());
        return userClient.update(userUpdateDto, id);
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

        when(client.update(any(UserUpdateDto.class), anyLong())).thenReturn(ResponseEntity.ok(userDto));

        mvc.perform(patch("/users/1")
                        .content(mapper.writeValueAsString(userUpdateDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(userDto)));

        // Проверка вызова сервиса с правильными параметрами
        verify(client).update(userUpdateDto, 1);
    }

    /*
    @DeleteMapping("{id}")
    public void delete(@PathVariable long id) {
        log.info("Delete user id {}", id);
        userClient.delete(id);
    }*/
    @Test
    void deleteTest() throws Exception {
        initialize();

        when(client.delete(anyLong())).thenReturn(ResponseEntity.ok(userDto));

        mvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        // Проверка вызова сервиса с правильными параметрами
        verify(client).delete(1);
    }

    private void initialize() {
        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("userName");
        userDto.setEmail("user@email.ru");
    }
}
