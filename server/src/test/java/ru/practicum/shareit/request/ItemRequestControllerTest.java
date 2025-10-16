package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.item.dto.ResponseItemDto;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestExtendedDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ItemRequestControllerTest {
    @Mock
    private ItemRequestService service;

    @InjectMocks
    private ItemRequestController controller;

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private MockMvc mvc;

    private ItemRequestDto itemRequestDto;
    private ItemRequestExtendedDto itemRequestExtendedDto;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    /*
    @PostMapping
    public ItemRequestDto create (@RequestHeader("X-Sharer-User-Id") long userId,
                                  @Valid @RequestBody ItemRequestDto itemRequestDto) {
        return itemRequestService.create(userId, itemRequestDto);
    }
    */
    @Test
    void createTest() throws Exception {
        initialize();

        when(service.create(anyLong(), any(ItemRequestDto.class))).thenReturn(itemRequestDto);

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 7)
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(itemRequestDto)));

        verify(service).create(eq(7L), argThat(dto ->
                dto.getId() == itemRequestDto.getId() &&
                        dto.getDescription().equals(itemRequestDto.getDescription()))
        );
    }

    /*
    @GetMapping
    public List<ItemRequestExtendedDto> getOwnRequests(@RequestHeader("X-Sharer-User-Id") long userId) {
        return itemRequestService.getOwnRequests(userId);
    }
    */
    @Test
    void getOwnRequestsTest() throws Exception {
        extendedDtoInitialize();

        List<ItemRequestExtendedDto> result = List.of(itemRequestExtendedDto);

        when(service.getOwnRequests(anyLong())).thenReturn(result);

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 7)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(content().json(mapper.writeValueAsString(result)));

        verify(service).getOwnRequests(eq(7L));
    }

    /*
    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests(@RequestHeader("X-Sharer-User-Id") long userId) {
        return itemRequestService.getAllRequests(userId);
    }
    */
    @Test
    void getAllRequestsTest() throws Exception {
        initialize();

        List<ItemRequestDto> result = List.of(itemRequestDto);

        when(service.getAllRequests(anyLong())).thenReturn(result);

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 7)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(content().json(mapper.writeValueAsString(result)));

        verify(service).getAllRequests(eq(7L));
    }

    /*
    @GetMapping("/{requestId}")
    public ItemRequestExtendedDto getRequest(@RequestHeader("X-Sharer-User-Id") long userId,
                                             @PathVariable long requestId) {
        return itemRequestService.getRequest(userId, requestId);
    }
    */
    @Test
    void getRequestTest() throws Exception {
        extendedDtoInitialize();

        when(service.getRequest(anyLong(), anyLong())).thenReturn(itemRequestExtendedDto);

        mvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 7)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(itemRequestExtendedDto)));

        verify(service).getRequest(eq(7L), eq(1L));
    }

    private void initialize() {
        itemRequestDto = new ItemRequestDto();
        itemRequestDto.setId(1);
        itemRequestDto.setDescription("description");
        itemRequestDto.setCreated(LocalDateTime.now());
    }

    private void extendedDtoInitialize() {
        ResponseItemDto item1 = new ResponseItemDto();
        item1.setName("item1Name");
        item1.setOwnerId(1);
        ResponseItemDto item2 = new ResponseItemDto();
        item2.setName("item2Name");
        item2.setOwnerId(2);

        itemRequestExtendedDto = new ItemRequestExtendedDto();
        itemRequestExtendedDto.setId(1);
        itemRequestExtendedDto.setDescription("description");
        itemRequestExtendedDto.setRequesterId(7);
        itemRequestExtendedDto.setItems(List.of(item1, item2));
        itemRequestExtendedDto.setCreated(LocalDateTime.now());
    }
}
