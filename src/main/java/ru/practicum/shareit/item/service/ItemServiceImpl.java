package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.common.exceptions.InternalServerException;
import ru.practicum.shareit.common.exceptions.NotFoundException;
import ru.practicum.shareit.common.exceptions.OwnershipException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemExtendedDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public ItemDto create(long userId, ItemDto itemDto) {
        userService.checkUserExist(userId);
        //Проверка наличия записи в БД происходит выше, потому в проверке Optional на Present необходимости нет
        User user = userRepository.findById(userId).get();

        //в этом спринте Request не реализуем, потому пока передаю Null
        Item item = ItemMapper.toItem(itemDto, user, null);
        item = itemRepository.save(item);
        return ItemMapper.toItemDto(item);
    }

    @Transactional
    @Override
    public ItemDto update(long userId, ItemUpdateDto itemUpdateDto, long itemId) {
        userService.checkUserExist(userId);
        checkItemExists(itemId);
        //Проверка наличия записи в БД происходит выше, потому в проверке Optional на Present необходимости нет
        Item item = itemRepository.findById(itemId).get();

        checkItemOwnership(userId, item);

        String name = itemUpdateDto.getName();
        if (name != null && !name.isBlank()) {
            item.setName(name);
        }

        String description = itemUpdateDto.getDescription();
        if (description != null && !description.isBlank()) {
            item.setDescription(description);
        }

        Boolean available = itemUpdateDto.getAvailable();
        if (available != null) {
            item.setAvailable(available);
        }

        item = itemRepository.save(item);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemExtendedDto get(long itemId) {
        checkItemExists(itemId);
        //Проверка наличия записи в БД происходит выше, потому в проверке Optional на Present необходимости нет
        Item item = itemRepository.findById(itemId).get();

        LocalDateTime now = LocalDateTime.now();

        Optional<Booking> lastBooking = bookingRepository.findLastBooking(itemId, now);
        Optional<Booking> nextBooking = bookingRepository.findNextBooking(itemId, now);

        LocalDateTime lastBookingDate = lastBooking.isPresent() ? lastBooking.get().getEnd() : null;
        LocalDateTime nextBookingDate = nextBooking.isPresent() ? nextBooking.get().getStart() : null;

        List<CommentDto> comments = commentRepository.findAllByItemId(itemId).stream()
                .map(CommentMapper::toCommentDto).toList();

        return ItemMapper.toItemExtendedDto(item, lastBookingDate, nextBookingDate, comments);
    }

    @Override
    public List<ItemExtendedDto> findItemsByOwnerId(long userId) {
        userService.checkUserExist(userId);
        LocalDateTime now = LocalDateTime.now();

        return itemRepository.findAllByOwnerId(userId).stream()
                .map(item -> {
                    long itemId = item.getId();
                    Optional<Booking> lastBooking = bookingRepository.findLastBooking(itemId, now);
                    Optional<Booking> nextBooking = bookingRepository.findNextBooking(itemId, now);

                    LocalDateTime lastBookingDate = lastBooking.isPresent() ? lastBooking.get().getEnd() : null;
                    LocalDateTime nextBookingDate = nextBooking.isPresent() ? nextBooking.get().getStart() : null;

                    List<CommentDto> comments = commentRepository.findAllByItemId(itemId).stream()
                            .map(CommentMapper::toCommentDto).toList();
                    return ItemMapper.toItemExtendedDto(item, lastBookingDate, nextBookingDate, comments);
                })
                .toList();
    }

    @Override
    public List<ItemDto> findItem(String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        return itemRepository.findByText(text).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Transactional
    @Override
    public CommentDto comment(long authorId, CommentDto commentDto, long itemId) {
        userService.checkUserExist(authorId);
        checkItemExists(itemId);

        if (!bookingRepository.existsFinishedBookingByBookerIdAndItemId(authorId, itemId, LocalDateTime.now())) {
            log.warn("Пользователь {} не арендовал вещь {} или аренда еще на завершилась", authorId, itemId);
            throw new InternalServerException("Пользователь " + authorId +
                    " не арендовал вещь " + itemId + " или аренда еще на завершилась");
        }

        //Проверка наличия записи в БД происходит выше, потому в проверке Optional на Present необходимости нет
        User author = userRepository.findById(authorId).get();
        Item item = itemRepository.findById(itemId).get();

        Comment comment = CommentMapper.toComment(commentDto, item, author);
        comment.setCreated(LocalDateTime.now());

        comment = commentRepository.save(comment);

        return CommentMapper.toCommentDto(comment);
    }

    @Override
    public void checkItemExists(long itemId) {
        if (!itemRepository.existsById(itemId)) {
            log.warn("При запросе данных вещи возникла ошибка: Вещь не найдена");
            throw new NotFoundException("Вещь " + itemId + " не найдена");
        }

    }

    @Override
    public void checkItemOwnership(long userId, Item item) {
        if (item.getOwner().getId() != userId) {
            log.warn("При проверке пользователя возникла ошибка: пользователь не является владельцем вещи");
            long itemId = item.getId();
            throw new OwnershipException("Пользователь id " + userId +
                    " не владеет вещью " + itemId);
        }
    }
}
