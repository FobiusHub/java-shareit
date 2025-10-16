package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

@Data
public class ItemRequestDto {
    private long id;

    @NotBlank(message = "Описание должно быть заполнено")
    @Length(max = 500, message = "Описание не должно превышать 500 символов")
    private String description;

    private long requesterId;

    @JsonFormat(pattern = "dd.MM.yyyy HH:mm:ss")
    private LocalDateTime created;
}
