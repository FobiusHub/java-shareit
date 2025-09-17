package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * TODO Sprint add-controllers.
 */
@Data
@AllArgsConstructor
public class ItemDto {
    private long id;
    @NotBlank(message = "Название должно быть заполнено")
    private String name;
    @NotBlank(message = "Описание должно быть заполнено")
    private String description;
    private Boolean available;
    private long owner;
    private Long request;
}
