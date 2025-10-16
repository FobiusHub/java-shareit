package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ItemRequestTest {
    @Test
    void usersShouldBeEquals() {
        ItemRequest itemRequest1 = new ItemRequest();
        itemRequest1.setId(1);

        ItemRequest itemRequest2 = new ItemRequest();
        itemRequest2.setId(1);

        assertTrue(itemRequest1.equals(itemRequest2));
    }

    @Test
    void usersAreNotEquals() {
        ItemRequest itemRequest1 = new ItemRequest();
        itemRequest1.setId(1);

        ItemRequest itemRequest2 = new ItemRequest();
        itemRequest2.setId(2);

        assertFalse(itemRequest1.equals(itemRequest2));
    }

    @Test
    void equalsReturnFalseIfNotUser() {
        ItemRequest itemRequest1 = new ItemRequest();
        itemRequest1.setId(1);

        List<ItemRequest> userList = List.of();

        assertFalse(itemRequest1.equals(userList));
    }
}
