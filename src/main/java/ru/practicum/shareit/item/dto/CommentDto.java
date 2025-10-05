package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentDto {
    private long id;

    @NotBlank(message = "Необходимо заполнить текст комментария")
    private String text;

    private String authorName;

    private LocalDateTime created;
}
