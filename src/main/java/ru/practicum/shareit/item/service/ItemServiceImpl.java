package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.common.exceptions.NotFoundException;
import ru.practicum.shareit.common.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;

    public ItemDto create(long userId, ItemDto itemDto) {
        userService.checkUserExist(userId);
        User user = UserMapper.toUser(userService.get(userId));
        //в этом спринте Request и Booking не реализуем, потому пока передаю Null
        Item item = ItemMapper.toItem(itemDto, user, null);
        item = itemRepository.add(item);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto update(long userId, ItemDto itemDto, long itemId) {
        userService.checkUserExist(userId);
        checkItemExists(itemId);
        checkItemOwnership(userId, itemId);
        Item item = itemRepository.update(itemDto, itemId);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto get(long id) {
        checkItemExists(id);
        return ItemMapper.toItemDto(itemRepository.get(id));
    }

    @Override
    public List<ItemDto> findItemsByOwnerId(long userId) {
        return itemRepository.getAll(userId).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public List<ItemDto> findItem(String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        return itemRepository.findItem(text).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    private void checkItemExists(long itemId) {
        if (!itemRepository.itemExists(itemId)) {
            log.warn("При запросе данных вещи возникла ошибка: Вещь не найден");
            throw new NotFoundException("Вещь " + itemId + " не найдена");
        }

    }

    private void checkItemOwnership(long userId, long itemId) {
        Item item = itemRepository.get(itemId);
        if (item.getOwner().getId() != userId) {
            log.warn("При проверке пользователя возникла ошибка: редактировать вещь может только владелец");
            throw new ValidationException("Пользователь id " + userId +
                    " не может вносить изменения в вещь id " + itemId);
        }
    }

}
