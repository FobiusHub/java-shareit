package ru.practicum.shareit.item.repository;


import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {
    Item add(Item item);

    Item get(long id);

    Item update(ItemDto itemDto, long id);

    List<Item> getAll(long userId);

    List<Item> findItem(String text);

    boolean itemExists(long id);
}
