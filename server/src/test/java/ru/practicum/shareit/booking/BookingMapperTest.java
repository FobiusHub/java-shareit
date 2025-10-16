package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class BookingMapperTest {
    //public BookingDto toBookingDto(Booking booking)
    @Test
    void toBookingDtoShouldReturnCorrectDto() {
        Item item = new Item();
        item.setId(1);

        User user = new User();
        user.setId(12);

        Booking booking = new Booking();
        booking.setId(1);
        booking.setStatus(Status.APPROVED);
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStart(LocalDateTime.of(2000, 01, 01, 12, 40));
        booking.setEnd(LocalDateTime.of(2000, 01, 01, 12, 45));

        BookingDto dto = BookingMapper.toBookingDto(booking);

        assertThat(dto.getId(), equalTo(booking.getId()));
        assertThat(dto.getStatus(), equalTo(booking.getStatus().name()));
        assertThat(dto.getItemId(), equalTo(booking.getItem().getId()));
        assertThat(dto.getBookerId(), equalTo(booking.getBooker().getId()));
        assertThat(dto.getStart(), equalTo(booking.getStart()));
        assertThat(dto.getEnd(), equalTo(booking.getEnd()));
    }
}
