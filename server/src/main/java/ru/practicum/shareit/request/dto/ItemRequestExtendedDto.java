package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import ru.practicum.shareit.item.dto.ResponseItemDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ItemRequestExtendedDto {
    private long id;

    private String description;

    private long requesterId;

    private List<ResponseItemDto> items;

    @JsonFormat(pattern = "dd.MM.yyyy HH:mm:ss")
    private LocalDateTime created;
}
