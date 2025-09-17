package ru.practicum.shareit.user.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * TODO Sprint add-controllers.
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(of = {"email"})
public class User {
    private long id;
    @NotBlank(message = "Имя должно быть заполнено")
    private String name;
    @Email(message = "Некорректный email")
    @NotBlank(message = "Некорректный email")
    private String email;
}
