package ru.practicum.shareit.request.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.ResponseItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestExtendedDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@UtilityClass
public class ItemRequestMapper {
    public ItemRequest toItemRequest(ItemRequestDto itemRequestDto, User user) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(itemRequestDto.getId());
        itemRequest.setDescription(itemRequestDto.getDescription());
        itemRequest.setRequester(user);
        itemRequest.setCreated(itemRequestDto.getCreated());
        return itemRequest;
    }

    public ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setId(itemRequest.getId());
        itemRequestDto.setDescription(itemRequest.getDescription());
        itemRequestDto.setRequesterId(itemRequest.getRequester().getId());
        itemRequestDto.setCreated(itemRequest.getCreated());
        return itemRequestDto;
    }

    public ItemRequestExtendedDto toItemrequestExtendedDto(ItemRequest itemRequest,
                                                           List<ResponseItemDto> items) {
        ItemRequestExtendedDto itemRequestExtendedDto = new ItemRequestExtendedDto();
        itemRequestExtendedDto.setId(itemRequest.getId());
        itemRequestExtendedDto.setDescription(itemRequest.getDescription());
        itemRequestExtendedDto.setRequesterId(itemRequest.getRequester().getId());
        itemRequestExtendedDto.setItems(items);
        itemRequestExtendedDto.setCreated(itemRequest.getCreated());
        return itemRequestExtendedDto;
    }
}
