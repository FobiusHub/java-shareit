package ru.practicum.shareit.booking.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.ResponseBookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.QBooking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.common.exceptions.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.ShortUserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final UserService userService;
    private final UserRepository userRepository;
    private final ItemService itemService;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    @Override
    public ResponseBookingDto create(long userId, BookingDto bookingDto) {
        checkBookingDates(bookingDto);

        long itemId = bookingDto.getItemId();

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь " + itemId + " не найдена"));

        if (!item.isAvailable()) {
            log.warn("Вещь не доступна для бронирования");
            throw new InternalServerException("Вещь " + itemId + " не доступна для бронирования");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь " + userId + " не найден"));

        bookingDto.setStatus(Status.WAITING.name());
        Booking booking = BookingMapper.toBooking(bookingDto, item, user);
        booking = bookingRepository.save(booking);

        return BookingMapper.toResponseBookingDto(booking,
                itemRepository.findShortItemDtoById(itemId),
                userRepository.findShortUserDtoById(userId));
    }

    @Transactional
    @Override
    public ResponseBookingDto update(long userId, long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование " + bookingId + " не найдено"));

        long itemId = booking.getItem().getId();

        if (approved) {
            Item item = itemRepository.findById(itemId)
                    .orElseThrow(() -> new NotFoundException("Вещь " + itemId + " не найдена"));
            itemService.checkItemOwnership(userId, item);
            booking.setStatus(Status.APPROVED);
            item.setAvailable(false);
            itemRepository.save(item);
        } else {
            booking.setStatus(Status.REJECTED);
        }

        booking = bookingRepository.save(booking);

        return BookingMapper.toResponseBookingDto(booking,
                itemRepository.findShortItemDtoById(itemId),
                userRepository.findShortUserDtoById(booking.getBooker().getId()));
    }

    @Override
    public ResponseBookingDto get(long userId, long bookingId) {
        userService.checkUserExist(userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование " + bookingId + " не найдено"));

        //Может быть выполнено либо автором бронирования, либо владельцем вещи, к которой относится бронирование
        Item item = booking.getItem();
        if (item.getOwner().getId() != userId && booking.getBooker().getId() != userId) {
            log.warn("Получение данных о бронировании может быть выполнено автором бронирования или владельцем вещи.");
            throw new NotFoundException(userId + " не является автором бронирования или владельцем вещи.");
        }

        return BookingMapper.toResponseBookingDto(booking,
                itemRepository.findShortItemDtoById(booking.getItem().getId()),
                userRepository.findShortUserDtoById(userId));
    }

    @Override
    public List<ResponseBookingDto> getUserBookings(long userId, String state) {
        userService.checkUserExist(userId);
        BooleanExpression predicate = QBooking.booking.booker.id.eq(userId);

        return getBookings(userId, state, predicate);
    }

    @Override
    public List<ResponseBookingDto> getUserItemsBookings(long userId, String state) {
        userService.checkUserExist(userId);
        List<Item> userItems = itemRepository.findAllByOwnerId(userId);

        if (userItems.isEmpty()) {
            return new ArrayList<>();
        }

        BooleanExpression predicate = QBooking.booking.item.in(userItems);

        return getBookings(userId, state, predicate);
    }

    private List<ResponseBookingDto> getBookings(long userId, String state, BooleanExpression predicate) {
        BookingState bookingState;
        try {
            bookingState = BookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InternalServerException("Ошибка при запросе бронирований пользователя: " + state +
                    " - недопустимый параметр.");
        }

        LocalDateTime now = LocalDateTime.now();

        switch (bookingState) {
            case ALL -> {
            }
            case CURRENT -> predicate = predicate.and(QBooking.booking.start.after(now))
                    .and(QBooking.booking.end.after(now));
            case PAST -> predicate = predicate.and(QBooking.booking.end.before(now));
            case FUTURE -> predicate = predicate.and(QBooking.booking.start.after(now));
            case WAITING -> predicate = predicate.and(QBooking.booking.status.eq(Status.WAITING));
            case REJECTED -> predicate = predicate.and(QBooking.booking.status.eq(Status.REJECTED));
        }

        ShortUserDto shortUserDto = userRepository.findShortUserDtoById(userId);
        List<Booking> userBookings = (List<Booking>) bookingRepository
                .findAll(predicate, Sort.by("start").descending());

        return userBookings.stream()
                .map(booking -> BookingMapper.toResponseBookingDto(booking,
                        itemRepository.findShortItemDtoById(booking.getItem().getId()),
                        shortUserDto)).toList();
    }

    private void checkBookingDates(BookingDto bookingDto) {
        LocalDateTime start = bookingDto.getStart();
        LocalDateTime end = bookingDto.getEnd();

        if (start.equals(end)) {
            log.warn("Даты начала и окончания бронирования должны различаться");
            throw new InvalidBookingDatesException("Даты начала и окончания бронирования должны различаться");
        }

        if (start.isAfter(end)) {
            log.warn("Дата начала бронирования не может быть после даты окончания");
            throw new InvalidBookingDatesException("Дата начала бронирования не может быть после даты окончания");
        }
    }
}
