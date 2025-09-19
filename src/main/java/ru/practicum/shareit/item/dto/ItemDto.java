package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ItemDto {
    private long id;
    @NotBlank(message = "Название должно быть заполнено")
    private String name;
    @NotBlank(message = "Описание должно быть заполнено")
    private String description;
    @NotNull(message = "Статус должен быть указан")
    private Boolean available;
    @NotNull(message = "Владелец должен быть указан")
    private long owner;
    private Long request;
}
