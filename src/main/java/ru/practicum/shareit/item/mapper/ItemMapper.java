package ru.practicum.shareit.item.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.ShortBookingDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemExtendedDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@UtilityClass
public class ItemMapper {
    public ItemDto toItemDto(Item item) {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(item.getId());
        itemDto.setName(item.getName());
        itemDto.setDescription(item.getDescription());
        itemDto.setAvailable(item.isAvailable());
        itemDto.setOwnerId(item.getOwner().getId());
        return itemDto;
    }

    public ItemExtendedDto toItemExtendedDto(Item item,
                                             ShortBookingDto lastBooking,
                                             ShortBookingDto nextBooking,
                                             List<CommentDto> comments) {
        ItemExtendedDto itemExtendedDto = new ItemExtendedDto();
        itemExtendedDto.setId(item.getId());
        itemExtendedDto.setName(item.getName());
        itemExtendedDto.setDescription(item.getDescription());
        itemExtendedDto.setAvailable(item.isAvailable());
        itemExtendedDto.setOwnerId(item.getOwner().getId());
        itemExtendedDto.setLastBooking(lastBooking);
        itemExtendedDto.setNextBooking(nextBooking);
        itemExtendedDto.setComments(comments);
        return itemExtendedDto;
    }

    public Item toItem(ItemDto itemDto, User owner, ItemRequest request) {
        Item item = new Item();
        item.setId(itemDto.getId());
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setOwner(owner);
        item.setRequest(request);
        return item;
    }
}
