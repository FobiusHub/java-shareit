package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.ResponseBookingDto;

import java.util.List;

public interface BookingService {

    ResponseBookingDto create(long userId, BookingDto bookingDto);

    ResponseBookingDto update(long userId, long bookingId, boolean approved);

    ResponseBookingDto get(long userId, long bookingId);

    List<ResponseBookingDto> getUserBookings(long userId, String state);

    List<ResponseBookingDto> getUserItemsBookings(long userId, String state);
}
