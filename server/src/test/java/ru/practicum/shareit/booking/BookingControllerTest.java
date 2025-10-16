package ru.practicum.shareit.booking;

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
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.ResponseBookingDto;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.common.controller.ErrorHandler;
import ru.practicum.shareit.common.exceptions.InternalServerException;
import ru.practicum.shareit.common.exceptions.InvalidBookingDatesException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class BookingControllerTest {
    @Mock
    BookingService service;

    @InjectMocks
    BookingController controller;

    // Маппер настроенный для корректной сериализации LocalDateTime в json
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private MockMvc mvc;

    private BookingDto bookingDto;
    private ResponseBookingDto responseBookingDto;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new ErrorHandler())
                .build();
    }

    /*
    @PostMapping
    public ResponseBookingDto create(@RequestHeader("X-Sharer-User-Id") long userId,
                                     @Valid @RequestBody BookingDto bookingDto) {
        return bookingService.create(userId, bookingDto);
    }
    */
    @Test
    void createTest() throws Exception {
        initialize();

        when(service.create(anyLong(), any(BookingDto.class))).thenReturn(responseBookingDto);

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 3L)
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(responseBookingDto)));

        verify(service).create(eq(3L), argThat(dto ->
                dto.getId() == bookingDto.getId() &&
                        dto.getItemId().equals(bookingDto.getItemId()) &&
                        dto.getBookerId().equals(bookingDto.getBookerId()))
        );
    }

    @Test
    void createShouldReturnInternalServerError() throws Exception {
        initialize();

        when(service.create(anyLong(), any(BookingDto.class)))
                .thenThrow(new InternalServerException("Ошибка"));

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 3L)
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Ошибка"));
    }

    @Test
    void createShouldReturnInvalidBookingDatesException() throws Exception {
        initialize();

        when(service.create(anyLong(), any(BookingDto.class)))
                .thenThrow(new InvalidBookingDatesException("Ошибка"));

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 3L)
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка"));
    }

    /*
    @PatchMapping("{bookingId}")
    public ResponseBookingDto update(@RequestHeader("X-Sharer-User-Id") long userId,
                                     @PathVariable long bookingId,
                                     @RequestParam boolean approved) {
        return bookingService.update(userId, bookingId, approved);
    }
    */
    @Test
    void updateTest() throws Exception {
        initialize();

        when(service.update(anyLong(), anyLong(), anyBoolean())).thenReturn(responseBookingDto);

        responseBookingDto.setStatus(Status.APPROVED.name());

        mvc.perform(patch("/bookings/2")
                        .header("X-Sharer-User-Id", 3L)
                        .param("approved", "true")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(responseBookingDto)));

        verify(service).update(eq(3L), eq(2L), eq(true));
    }

    /*
    @GetMapping("{bookingId}")
    public ResponseBookingDto read(@RequestHeader("X-Sharer-User-Id") long userId, @PathVariable long bookingId) {
        return bookingService.get(userId, bookingId);
    }
    */
    @Test
    void readTest() throws Exception {
        initialize();

        when(service.get(anyLong(), anyLong())).thenReturn(responseBookingDto);


        mvc.perform(get("/bookings/2")
                        .header("X-Sharer-User-Id", 3L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(responseBookingDto)));

        verify(service).get(eq(3L), eq(2L));
    }

    /*
    @GetMapping
    public List<ResponseBookingDto> getUserBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                                    @RequestParam(defaultValue = "ALL") String state) {
        return bookingService.getUserBookings(userId, state);
    }
    */
    @Test
    void getUserBookingsTest() throws Exception {
        initialize();

        List<ResponseBookingDto> result = List.of(responseBookingDto);

        when(service.getUserBookings(anyLong(), anyString())).thenReturn(result);

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 3L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(content().json(mapper.writeValueAsString(result)));

        verify(service).getUserBookings(eq(3L), eq("ALL"));
    }

    /*
    @GetMapping("owner")
    public List<ResponseBookingDto> getUserItemsBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                                         @RequestParam(defaultValue = "ALL") String state) {
        return bookingService.getUserItemsBookings(userId, state);
    }
    */
    @Test
    void getUserItemsBookings() throws Exception {
        initialize();

        List<ResponseBookingDto> result = List.of(responseBookingDto);

        when(service.getUserItemsBookings(anyLong(), anyString())).thenReturn(result);

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 3L)
                        .param("state", "CURRENT")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(content().json(mapper.writeValueAsString(result)));

        verify(service).getUserItemsBookings(eq(3L), eq("CURRENT"));
    }

    private void initialize() {
        bookingDto = new BookingDto();
        bookingDto.setId(5L);
        bookingDto.setItemId(2L);
        bookingDto.setBookerId(3L);
        bookingDto.setStart(LocalDateTime.now().plusHours(1));
        bookingDto.setEnd(bookingDto.getStart().plusDays(2));
        bookingDto.setStatus(Status.WAITING.name());

        responseBookingDto = new ResponseBookingDto();
        responseBookingDto.setId(bookingDto.getId());
        responseBookingDto.setStatus(bookingDto.getStatus());
    }
}
