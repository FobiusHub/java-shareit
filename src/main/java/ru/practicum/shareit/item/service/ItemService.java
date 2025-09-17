package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto create(long userId, ItemDto itemDto);

    ItemDto update(long userId, ItemDto itemDto, long itemId);

    ItemDto get(long id);

    List<ItemDto> findItemsByOwnerId(long userId);

    List<ItemDto> findItem(String text);
}
