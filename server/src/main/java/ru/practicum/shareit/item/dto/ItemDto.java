package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class ItemDto {
    private long id;
    @NotBlank(message = "Название должно быть заполнено")
    @Length(min = 1, max = 200, message = "Название должно быть от 1 до 200 символов")
    private String name;
    @NotBlank(message = "Описание должно быть заполнено")
    @Length(max = 500, message = "Описание не должно превышать 500 символов")
    private String description;
    @NotNull(message = "Статус должен быть указан")
    private Boolean available;
    private Long ownerId;
    private Long requestId;
}
