package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

@Data
public class CommentDto {
    private long id;

    @NotBlank(message = "Необходимо заполнить текст комментария")
    @Length(max = 512, message = "Комментарий не должен быть длиннее 512 символов")
    private String text;

    private String authorName;

    private LocalDateTime created;
}
