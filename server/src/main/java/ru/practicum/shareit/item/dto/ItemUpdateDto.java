package ru.practicum.shareit.item.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class ItemUpdateDto {
    @Length(min = 1, max = 200, message = "Название должно быть от 1 до 200 символов")
    private String name;

    @Length(max = 500, message = "Описание не должно превышать 500 символов")
    private String description;

    private Boolean available;
}
