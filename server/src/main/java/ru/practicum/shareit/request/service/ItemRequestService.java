package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestExtendedDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto create(long userId, ItemRequestDto itemRequestDto);

    List<ItemRequestExtendedDto> getOwnRequests(long userId);

    List<ItemRequestDto> getAllRequests(long userId);

    ItemRequestExtendedDto getRequest(long userId, long requestId);
}
