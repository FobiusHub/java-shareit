package ru.practicum.shareit.booking.dto;

import lombok.Data;
import ru.practicum.shareit.item.dto.ShortItemDto;
import ru.practicum.shareit.user.dto.ShortUserDto;

import java.time.LocalDateTime;

@Data
public class ResponseBookingDto {
    private long id;

    private LocalDateTime start;

    private LocalDateTime end;

    private ShortItemDto item;

    private ShortUserDto booker;

    private String status;
}
