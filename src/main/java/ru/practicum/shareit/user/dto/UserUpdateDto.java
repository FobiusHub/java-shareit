package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class UserUpdateDto {
    @Length(min = 1, max = 200, message = "Имя должно быть от 1 до 200 символов")
    private String name;

    @Email(message = "Некорректный email")
    private String email;
}
