import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.dto.ResponseBookingDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class BookingControllerTest {
    @Mock
    BookingClient client;

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
                .build();
    }

    /*
    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") long userId,
                                         @Valid @RequestBody BookingDto bookingDto) {
        return bookingClient.create(userId, bookingDto);
    }
    */
    @Test
    void createTest() throws Exception {
        initialize();

        when(client.create(anyLong(), any(BookingDto.class))).thenReturn(ResponseEntity.ok(responseBookingDto));

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 3L)
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(responseBookingDto)));

        verify(client).create(eq(3L), argThat(dto ->
                dto.getId() == bookingDto.getId() &&
                        dto.getItemId().equals(bookingDto.getItemId()) &&
                        dto.getBookerId().equals(bookingDto.getBookerId()))
        );
    }

    /*
    @PatchMapping("{bookingId}")
    public ResponseEntity<Object> update(@RequestHeader("X-Sharer-User-Id") long userId,
                                         @PathVariable long bookingId,
                                         @RequestParam boolean approved) {
        return bookingClient.update(userId, bookingId, approved);
    }
    */
    @Test
    void updateTest() throws Exception {
        initialize();

        when(client.update(anyLong(), anyLong(), anyBoolean())).thenReturn(ResponseEntity.ok(responseBookingDto));

        responseBookingDto.setStatus(Status.APPROVED.name());

        mvc.perform(patch("/bookings/2")
                        .header("X-Sharer-User-Id", 3L)
                        .param("approved", "true")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(responseBookingDto)));

        verify(client).update(eq(3L), eq(2L), eq(true));
    }

    /*
    @GetMapping("{bookingId}")
    public ResponseEntity<Object> read(@RequestHeader("X-Sharer-User-Id") long userId, @PathVariable long bookingId) {
        return bookingClient.get(userId, bookingId);
    }
    */
    @Test
    void readTest() throws Exception {
        initialize();

        when(client.get(anyLong(), anyLong())).thenReturn(ResponseEntity.ok(responseBookingDto));


        mvc.perform(get("/bookings/2")
                        .header("X-Sharer-User-Id", 3L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(responseBookingDto)));

        verify(client).get(eq(3L), eq(2L));
    }

    /*
    @GetMapping
    public ResponseEntity<Object> getUserBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                                  @RequestParam(defaultValue = "ALL") String stateParam) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        return bookingClient.getUserBookings(userId, state);
    }
    */
    @Test
    void getUserBookingsTest() throws Exception {
        initialize();

        List<ResponseBookingDto> result = List.of(responseBookingDto);

        when(client.getUserBookings(anyLong(), any(BookingState.class))).thenReturn(ResponseEntity.ok(result));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 3L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(content().json(mapper.writeValueAsString(result)));

        verify(client).getUserBookings(eq(3L), eq(BookingState.ALL));
    }

    /*
    @GetMapping("owner")
    public ResponseEntity<Object> getUserItemsBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                                       @RequestParam(defaultValue = "ALL") String stateParam) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        return bookingClient.getUserItemsBookings(userId, state);
    }
    */
    @Test
    void getUserItemsBookings() throws Exception {
        initialize();

        List<ResponseBookingDto> result = List.of(responseBookingDto);

        when(client.getUserItemsBookings(anyLong(), any(BookingState.class))).thenReturn(ResponseEntity.ok(result));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 3L)
                        .param("stateParam", "CURRENT")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(content().json(mapper.writeValueAsString(result)));

        verify(client).getUserItemsBookings(eq(3L), eq(BookingState.CURRENT));
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

    private enum Status {
        WAITING,
        APPROVED
    }
}



