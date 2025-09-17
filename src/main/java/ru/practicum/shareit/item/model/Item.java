package ru.practicum.shareit.item.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

/**
 * TODO Sprint add-controllers.
 */
@Data
@AllArgsConstructor
public class Item {
    private long id;
    @NotBlank(message = "Название должно быть заполнено")
    private String name;
    @NotBlank(message = "Описание должно быть заполнено")
    private String description;
    private boolean available;
    @NotNull(message = "Владелец должен быть указан")
    private User owner;
    private ItemRequest request;
}
