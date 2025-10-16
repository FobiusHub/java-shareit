package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Не тестирую findLastBooking и findNextBooking, т.к. он косвенно проверяются в BookingServiceIntegrationTest
 */
@Transactional
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingRepositoryTest {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Test
    void existsFinishedBookingByBookerIdAndItemIdShouldReturnFalseIfNotExists() {
        boolean result = bookingRepository.existsFinishedBookingByBookerIdAndItemId(1,
                1, LocalDateTime.now());

        assertThat(result, equalTo(false));
    }

    @Test
    void existsFinishedBookingByBookerIdAndItemIdShouldReturnTrueIfExists() {
        User user = new User();
        user.setName("name");
        user.setEmail("email@email.ru");
        user = userRepository.save(user);

        Item item = new Item();
        item.setName("itemName");
        item.setDescription("itemDescription");
        item.setAvailable(true);
        item.setOwner(user);
        item = itemRepository.save(item);

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.of(2025, 5, 1, 10, 10));
        booking.setEnd(LocalDateTime.of(2025, 6, 1, 10, 10));
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(Status.APPROVED);
        bookingRepository.save(booking);

        boolean result = bookingRepository.existsFinishedBookingByBookerIdAndItemId(user.getId(),
                item.getId(), LocalDateTime.now());

        assertThat(result, equalTo(true));
    }
}
