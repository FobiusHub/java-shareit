package ru.practicum.shareit.item;

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
import ru.practicum.shareit.booking.dto.ShortBookingDto;
import ru.practicum.shareit.common.controller.ErrorHandler;
import ru.practicum.shareit.common.exceptions.OwnershipException;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemExtendedDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class ItemControllerTest {
    // Создаем мок-объект для UserService (заглушка бизнес-логики)
    @Mock
    private ItemService service;

    // Создаем реальный контроллер и автоматически внедряем в него мок-сервис
    @InjectMocks
    private ItemController controller;

    // Jackson объект для преобразования Java-объектов в JSON и обратно
    private final ObjectMapper mapper = new ObjectMapper();
    // MockMvc - основной инструмент для тестирования Spring MVC контроллеров
    private MockMvc mvc;

    private ItemDto itemDto;
    private ItemUpdateDto itemUpdateDto;
    private ItemExtendedDto itemExtendedDto;
    private ShortBookingDto lastBooking;
    private ShortBookingDto nextBooking;
    private CommentDto comment1;
    private CommentDto comment2;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new ErrorHandler())
                .build();
    }

    @Test
    void createTest() throws Exception {
        initialize();
        itemDto.setId(1L);

        when(service.create(anyLong(), any(ItemDto.class))).thenReturn(itemDto);

        // Выполняем HTTP POST запрос к эндпоинту /items
        mvc.perform(post("/items")
                        // Указываем заголовок с userId
                        .header("X-Sharer-User-Id", 1)
                        // Преобразуем Java-объект userDto в JSON строку для тела запроса
                        .content(mapper.writeValueAsString(itemDto))
                        // Указываем кодировку
                        .characterEncoding(StandardCharsets.UTF_8)
                        // Указываем тип контента (отправляем JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        // Указываем, что ожидаем JSON в ответе
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())));

        // Проверка вызова сервиса с правильными параметрами
        verify(service).create(eq(1L), argThat(dto ->
                dto.getName().equals(itemDto.getName()) &&
                        dto.getDescription().equals(itemDto.getDescription()) &&
                        dto.getAvailable().equals(itemDto.getAvailable()))
        );
    }

    @Test
    void updateTest() throws Exception {
        initialize();

        itemDto.setName(itemUpdateDto.getName());
        itemDto.setDescription(itemUpdateDto.getDescription());
        itemDto.setAvailable(itemUpdateDto.getAvailable());

        when(service.update(anyLong(), any(ItemUpdateDto.class), anyLong())).thenReturn(itemDto);

        mvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(itemUpdateDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(itemUpdateDto.getName())))
                .andExpect(jsonPath("$.description", is(itemUpdateDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemUpdateDto.getAvailable())));

        verify(service).update(eq(1L), argThat(dto ->
                dto.getName().equals(itemUpdateDto.getName()) &&
                        dto.getDescription().equals(itemUpdateDto.getDescription()) &&
                        dto.getAvailable().equals(itemUpdateDto.getAvailable())), eq(1L)
        );
    }

    @Test
    void updateShouldReturnForbidden() throws Exception {
        initialize();

        when(service.update(anyLong(), any(ItemUpdateDto.class), anyLong()))
                .thenThrow(new OwnershipException("Ошибка"));

        mvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(itemUpdateDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Ошибка"));
    }

    @Test
    void readTest() throws Exception {
        initialize();
        initializeForExtendedDtoMethods();

        when(service.get(anyLong())).thenReturn(itemExtendedDto);

        mvc.perform(get("/items/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemExtendedDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemExtendedDto.getName())))
                .andExpect(jsonPath("$.description", is(itemExtendedDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemExtendedDto.getAvailable())))
                .andExpect(jsonPath("$.ownerId", is(itemExtendedDto.getOwnerId()), Long.class))
                .andExpect(jsonPath("$.requestId", is(itemExtendedDto.getRequestId()), Long.class))
                .andExpect(jsonPath("$.lastBooking").exists())
                .andExpect(jsonPath("$.nextBooking").exists())
                .andExpect(jsonPath("$.comments", hasSize(2)));

        verify(service).get(eq(1L));
    }

    @Test
    void findItemsByOwnerIdTest() throws Exception {
        initialize();
        initializeForExtendedDtoMethods();

        when(service.findItemsByOwnerId(anyLong())).thenReturn(List.of(itemExtendedDto));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 7)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemExtendedDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemExtendedDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemExtendedDto.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemExtendedDto.getAvailable())))
                .andExpect(jsonPath("$[0].ownerId", is(itemExtendedDto.getOwnerId()), Long.class))
                .andExpect(jsonPath("$[0].requestId", is(itemExtendedDto.getRequestId()), Long.class))
                .andExpect(jsonPath("$[0].lastBooking").exists())
                .andExpect(jsonPath("$[0].nextBooking").exists())
                .andExpect(jsonPath("$[0].comments", hasSize(2)));

        verify(service).findItemsByOwnerId(eq(7L));
    }

    @Test
    void findItemTest() throws Exception {
        initialize();

        when(service.findItem("text")).thenReturn(List.of(itemDto));

        mvc.perform(get("/items/search")
                        .param("text", "text")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$[0].ownerId", is(itemDto.getOwnerId()), Long.class))
                .andExpect(jsonPath("$[0].requestId", is(itemDto.getRequestId()), Long.class));

        verify(service).findItem(eq("text"));
    }

    @Test
    void commentTest() throws Exception {
        initialize();
        User user = new User();
        user.setId(145);
        user.setName("userName");

        CommentDto commentDto = new CommentDto();
        commentDto.setId(657);
        commentDto.setText("someText");
        commentDto.setAuthorName(user.getName());

        when(service.comment(user.getId(), commentDto, 12)).thenReturn(commentDto);

        mvc.perform(post("/items/12/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 145)
                        .content(mapper.writeValueAsString(commentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText())))
                .andExpect(jsonPath("$.authorName", is(commentDto.getAuthorName())));

        verify(service).comment(eq(145L), argThat(dto ->
                dto.getText().equals(commentDto.getText()) &&
                        dto.getAuthorName().equals(commentDto.getAuthorName())), eq(12L));
    }

    private void initialize() {
        itemDto = new ItemDto();
        itemDto.setName("itemDtoName");
        itemDto.setDescription("itemDtoDescription");
        itemDto.setAvailable(true);
        itemDto.setOwnerId(1L);
        itemDto.setRequestId(1L);

        itemUpdateDto = new ItemUpdateDto();
        itemUpdateDto.setName("itemUpdateDtoName");
        itemUpdateDto.setDescription("itemUpdateDtoDescription");
        itemUpdateDto.setAvailable(false);
    }

    private void initializeForExtendedDtoMethods() {
        lastBooking = new ShortBookingDto();
        lastBooking.setId(1L);
        lastBooking.setBookerId(1L);
        nextBooking = new ShortBookingDto();
        nextBooking.setId(2L);
        nextBooking.setBookerId(2L);
        comment1 = new CommentDto();
        comment1.setAuthorName("name1");
        comment1.setText("text1");
        comment2 = new CommentDto();
        comment2.setAuthorName("name2");
        comment2.setText("text2");
        itemExtendedDto = new ItemExtendedDto();
        itemExtendedDto.setId(1);
        itemExtendedDto.setName(itemDto.getName());
        itemExtendedDto.setDescription(itemDto.getDescription());
        itemExtendedDto.setAvailable(itemDto.getAvailable());
        itemExtendedDto.setOwnerId(itemDto.getOwnerId());
        itemExtendedDto.setRequestId(itemDto.getRequestId());
        itemExtendedDto.setLastBooking(lastBooking);
        itemExtendedDto.setNextBooking(nextBooking);
        itemExtendedDto.setComments(List.of(comment1, comment2));
    }
}
