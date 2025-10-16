package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.common.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ResponseItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestExtendedDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ItemRepository itemRepository;

    @Transactional
    @Override
    public ItemRequestDto create(long userId, ItemRequestDto itemRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь " + userId + " не найден"));
        itemRequestDto.setCreated(LocalDateTime.now());
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto, user);
        itemRequest = itemRequestRepository.save(itemRequest);
        return ItemRequestMapper.toItemRequestDto(itemRequest);
    }

    @Override
    public List<ItemRequestExtendedDto> getOwnRequests(long userId) {
        userService.checkUserExist(userId);
        //получаем список запросов
        List<ItemRequest> ownRequests = itemRequestRepository.findItemRequestByRequesterId(userId);

        //если запросов не было - вернуть пустой список
        if (ownRequests.isEmpty()) {
            return List.of();
        }

        //получаем список id запросов
        List<Long> ownRequestsIds = ownRequests.stream().map(ItemRequest::getId).toList();
        //получаем список Item, созданных по запросам из списка
        List<Item> responseItemsList = itemRepository.findAllByRequestIdIn(ownRequestsIds);
        //группируем по id запроса и преобразуем в responseItemDto
        Map<Long, List<ResponseItemDto>> responseItemMap = toResponseItemMap(responseItemsList);

        //преобразуем map в список ItemRequestExtendedDto
        return ownRequests.stream()
                .map(itemRequest -> ItemRequestMapper.toItemrequestExtendedDto(itemRequest,
                        responseItemMap.getOrDefault(itemRequest.getId(), List.of())))
                .sorted((Comparator.comparing(ItemRequestExtendedDto::getCreated).reversed())).toList();
    }

    @Override
    public List<ItemRequestDto> getAllRequests(long userId) {
        userService.checkUserExist(userId);

        return itemRequestRepository.findItemRequestByRequesterIdNotOrderByCreatedDesc(userId).stream()
                .map(ItemRequestMapper::toItemRequestDto).toList();
    }

    @Override
    public ItemRequestExtendedDto getRequest(long userId, long requestId) {
        userService.checkUserExist(userId);
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос " + requestId + " не найден"));

        List<ResponseItemDto> items = itemRepository.findAllByRequestId(requestId).stream()
                .map(ItemMapper::toResponseItemDto).toList();

        return ItemRequestMapper.toItemrequestExtendedDto(itemRequest, items);
    }

    //метод группирует список Item по id запроса в map и преобразует item в ResponseItemDto
    private Map<Long, List<ResponseItemDto>> toResponseItemMap(List<Item> itemsList) {
        Map<Long, List<ResponseItemDto>> result = new HashMap<>();
        for (Item item : itemsList) {
            long itemId = item.getRequest().getId();
            //computeIfAbsent возвращает сущ. значение если ключ существует, иначе создает новое значение для ключа
            result.computeIfAbsent(itemId, key -> new ArrayList<>())
                    .add(ItemMapper.toResponseItemDto(item));
        }
        return result;
    }
}
