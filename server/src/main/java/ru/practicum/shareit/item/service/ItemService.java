package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemExtendedDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {

    ItemDto create(long userId, ItemDto itemDto);

    ItemDto update(long userId, ItemUpdateDto itemUpdateDto, long itemId);

    ItemExtendedDto get(long itemId);

    List<ItemExtendedDto> findItemsByOwnerId(long userId);

    List<ItemDto> findItem(String text);

    CommentDto comment(long authorId, CommentDto commentDto, long itemId);

    void checkItemExists(long itemId);

    void checkItemOwnership(long userId, Item item);
}
