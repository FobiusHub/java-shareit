package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentDto {
    private long id;

    private String text;

    private String authorName;

    @JsonFormat(pattern = "dd.MM.yyyy HH:mm:ss")
    private LocalDateTime created;
}
