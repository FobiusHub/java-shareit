package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingDto {
    private long id;

    @Future(message = "Недопустимая дата начала бронирования")
    @NotNull(message = "Необходимо указать дату начала бронирования")
    private LocalDateTime start;

    @Future(message = "Недопустимая дата конца бронирования")
    @NotNull(message = "Необходимо указать дату окончания бронирования")
    private LocalDateTime end;

    @NotNull(message = "Необходимо указать itemId")
    private Long itemId;

    private Long bookerId;

    private String status;
}
