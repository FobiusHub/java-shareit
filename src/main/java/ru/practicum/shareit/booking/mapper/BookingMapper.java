package ru.practicum.shareit.booking.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.ResponseBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.dto.ShortItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.ShortUserDto;
import ru.practicum.shareit.user.model.User;

@UtilityClass
public class BookingMapper {
    public BookingDto toBookingDto(Booking booking) {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(booking.getId());
        bookingDto.setStart(booking.getStart());
        bookingDto.setEnd(booking.getEnd());
        bookingDto.setItemId(booking.getItem().getId());
        bookingDto.setBookerId(booking.getBooker().getId());
        bookingDto.setStatus(booking.getStatus().name());
        return bookingDto;
    }

    public Booking toBooking(BookingDto bookingDto, Item item, User user) {
        Booking booking = new Booking();
        booking.setId(bookingDto.getId());
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(Status.valueOf(bookingDto.getStatus()));
        return booking;
    }

    public ResponseBookingDto toResponseBookingDto(Booking booking,
                                                   ShortItemDto shortItemDto,
                                                   ShortUserDto shortUserDto) {
        ResponseBookingDto responseBookingDto = new ResponseBookingDto();
        responseBookingDto.setId(booking.getId());
        responseBookingDto.setStart(booking.getStart());
        responseBookingDto.setEnd(booking.getEnd());
        responseBookingDto.setItem(shortItemDto);
        responseBookingDto.setBooker(shortUserDto);
        responseBookingDto.setStatus(booking.getStatus().name());
        return responseBookingDto;
    }
}
