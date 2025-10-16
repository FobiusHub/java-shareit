package ru.practicum.shareit.booking;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") long userId,
                                         @Valid @RequestBody BookingDto bookingDto) {
        return bookingClient.create(userId, bookingDto);
    }

    @PatchMapping("{bookingId}")
    public ResponseEntity<Object> update(@RequestHeader("X-Sharer-User-Id") long userId,
                                         @PathVariable long bookingId,
                                         @RequestParam boolean approved) {
        return bookingClient.update(userId, bookingId, approved);
    }

    @GetMapping("{bookingId}")
    public ResponseEntity<Object> read(@RequestHeader("X-Sharer-User-Id") long userId, @PathVariable long bookingId) {
        return bookingClient.get(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getUserBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                                  @RequestParam(defaultValue = "ALL") String stateParam) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        return bookingClient.getUserBookings(userId, state);
    }

    @GetMapping("owner")
    public ResponseEntity<Object> getUserItemsBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                                       @RequestParam(defaultValue = "ALL") String stateParam) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        return bookingClient.getUserItemsBookings(userId, state);
    }
}
