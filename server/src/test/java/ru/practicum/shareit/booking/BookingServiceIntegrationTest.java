package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.ResponseBookingDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.common.exceptions.InternalServerException;
import ru.practicum.shareit.common.exceptions.InvalidBookingDatesException;
import ru.practicum.shareit.common.exceptions.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingServiceIntegrationTest {
    private final BookingService bookingService;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    private BookingDto bookingDto;
    private User user;
    private Item item;

    /*
    ResponseBookingDto create(long userId, BookingDto bookingDto);
    */
    @Test
    void createShouldThrowInvalidBookingDatesException() {
        bookingDto = new BookingDto();
        bookingDto.setStart(LocalDateTime.MIN);
        bookingDto.setEnd(LocalDateTime.MIN);

        assertThrows(InvalidBookingDatesException.class, () -> {
            bookingService.create(0, bookingDto);
        });

        bookingDto.setStart(LocalDateTime.MAX);

        assertThrows(InvalidBookingDatesException.class, () -> {
            bookingService.create(0, bookingDto);
        });
    }

    @Test
    void createShouldThrowNotFoundExceptionIfItemDoesNotExist() {
        initialize();

        bookingDto.setItemId(-1L);

        assertThrows(NotFoundException.class, () -> {
            bookingService.create(0, bookingDto);
        });
    }

    @Test
    void createShouldThrowInternalServerExceptionIfItemIsNotAvailable() {
        initialize();
        item.setAvailable(false);
        bookingDto.setItemId(item.getId());

        assertThrows(InternalServerException.class, () -> {
            bookingService.create(0, bookingDto);
        });
    }

    @Test
    void createShouldThrowNotFoundExceptionIfUserDoesNotExist() {
        initialize();

        bookingDto.setItemId(item.getId());

        assertThrows(NotFoundException.class, () -> {
            bookingService.create(-1, bookingDto);
        });
    }

    @Test
    void createShouldReturnCorrectResponseBookingDto() {
        initialize();

        // Проверяем что возвращается ожидаемый результат
        ResponseBookingDto result = bookingService.create(user.getId(), bookingDto);
        assertResponseCorrect(result);
        assertThat(result.getStatus(), equalTo("WAITING"));

        // Проверяем, что возвращаемый результат соответствует базе данных
        ResponseBookingDto resultFromDb = bookingService.get(user.getId(), result.getId());
        assertResponseEquals(resultFromDb, result);
    }

    /*
    ResponseBookingDto update(long userId, long bookingId, boolean approved);
    */
    @Test
    void updateShouldThrowNotFoundExceptionIfBookingDoesNotExist() {
        assertThrows(NotFoundException.class, () -> {
            bookingService.update(0, -1, true);
        });
    }

    @Test
    void updateShouldThrowNotFoundExceptionIfUserDoesNotExist() {
        initialize();

        assertThrows(NotFoundException.class, () -> {
            bookingService.update(0, -1, true);
        });
    }

    @Test
    void updateShouldChangeBookingStatusAndItemAvailableIfApprove() {
        initialize();
        bookingDto.setId(bookingService.create(user.getId(), bookingDto).getId());

        // Проверяем что возвращается ожидаемый результат
        ResponseBookingDto result = bookingService.update(user.getId(), bookingDto.getId(), true);
        assertResponseCorrect(result);
        assertThat(result.getStatus(), equalTo("APPROVED"));

        // Проверяем, что возвращаемый результат соответствует базе данных
        ResponseBookingDto resultFromDb = bookingService.get(user.getId(), result.getId());
        assertResponseEquals(resultFromDb, result);

        // Проверяем, что статус Item меняется
        Item changedItem = itemRepository.findById(item.getId()).get();
        assertThat(changedItem.isAvailable(), equalTo(false));
    }

    @Test
    void updateShouldChangeBookingStatusIfNotApprove() {
        initialize();
        bookingDto.setId(bookingService.create(user.getId(), bookingDto).getId());

        // Проверяем что возвращается ожидаемый результат
        ResponseBookingDto result = bookingService.update(user.getId(), bookingDto.getId(), false);
        assertResponseCorrect(result);
        assertThat(result.getStatus(), equalTo("REJECTED"));

        // Проверяем, что возвращаемый результат соответствует базе данных
        ResponseBookingDto resultFromDb = bookingService.get(user.getId(), result.getId());
        assertResponseEquals(resultFromDb, result);

        // Проверяем, что статус Item НЕ меняется
        Item changedItem = itemRepository.findById(item.getId()).get();
        assertThat(changedItem.isAvailable(), equalTo(true));
    }


    /*
    ResponseBookingDto get(long userId, long bookingId);
    */
    @Test
    void getShouldThrowNotFoundExceptionIfUserDoesNotExist() {
        assertThrows(NotFoundException.class, () -> {
            bookingService.get(-1, -1);
        });
    }


    @Test
    void getShouldThrowNotFoundExceptionIfBookingDoesNotExist() {
        initialize();

        assertThrows(NotFoundException.class, () -> {
            bookingService.get(user.getId(), -1);
        });
    }

    @Test
    void getShouldThrowNotFoundExceptionIfUserDidNotBookItem() {
        initialize();

        bookingDto.setId(bookingService.create(user.getId(), bookingDto).getId());

        User otherUser = new User();
        otherUser.setName("otherName");
        otherUser.setEmail("otherEmail@email.ru");
        userRepository.save(otherUser);

        assertThrows(NotFoundException.class, () -> {
            bookingService.get(otherUser.getId(), bookingDto.getId());
        });
    }

    /*
    List<ResponseBookingDto> getUserBookings(long userId, String state);
    */
    @Test
    void getUserBookingsShouldThrowNotFoundExceptionIfUserDoesNotExist() {
        assertThrows(NotFoundException.class, () -> {
            bookingService.getUserBookings(-1, "ALL");
        });
    }

    @Test
    void getUserBookingsShouldThrowInternalServerExceptionIfStateIsIncorrect() {
        initialize();

        assertThrows(InternalServerException.class, () -> {
            bookingService.getUserBookings(user.getId(), "someState");
        });
    }

    @Test
    void getUserBookingsShouldShouldReturnCorrectDtoList() {
        initialize();

        User newUser = new User();
        newUser.setName("newUserName");
        newUser.setEmail("newUser@email.ru");
        newUser = userRepository.save(newUser);

        bookingDto.setBookerId(newUser.getId());
        bookingService.create(newUser.getId(), bookingDto);

        List<ResponseBookingDto> result = bookingService.getUserBookings(newUser.getId(), "ALL");
        assertThat(result, hasSize(1));

        ResponseBookingDto resp = result.getFirst();

        assertThat(resp, notNullValue());
        assertThat(resp.getStart(), equalTo(bookingDto.getStart()));
        assertThat(resp.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(resp.getItem().getId(), equalTo(item.getId()));
        assertThat(resp.getItem().getName(), equalTo(item.getName()));
        assertThat(resp.getBooker().getId(), equalTo(newUser.getId()));
        assertThat(resp.getBooker().getName(), equalTo(newUser.getName()));
    }

    /*
    List<ResponseBookingDto> getUserItemsBookings(long userId, String state);
    */
    @Test
    void getUserItemsBookingsShouldThrowNotFoundExceptionIfUserDoesNotExist() {
        assertThrows(NotFoundException.class, () -> {
            bookingService.getUserItemsBookings(-1, "ALL");
        });
    }

    @Test
    void getUserItemsBookingsShouldReturnEmptyListIfUserDoesNotOwnTheItems() {
        user = new User();
        user.setName("name");
        user.setEmail("email@email.ru");
        user = userRepository.save(user);

        List<ResponseBookingDto> userItems = bookingService.getUserItemsBookings(user.getId(), "ALL");

        assertThat(userItems, empty());
    }

    @Test
    void getUserItemsBookingsShouldThrowInternalServerExceptionIfStateIsIncorrect() {
        initialize();

        assertThrows(InternalServerException.class, () -> {
            bookingService.getUserItemsBookings(user.getId(), "someState");
        });
    }

    @Test
    void getUserItemsBookingsShouldShouldReturnCorrectDtoList() {
        initialize();

        User newUser = new User();
        newUser.setName("newUserName");
        newUser.setEmail("newUser@email.ru");
        newUser = userRepository.save(newUser);

        bookingDto.setBookerId(newUser.getId());
        bookingService.create(newUser.getId(), bookingDto);

        List<ResponseBookingDto> result = bookingService.getUserItemsBookings(user.getId(), "ALL");
        assertThat(result, hasSize(1));
        assertResponseCorrect(result.getFirst());
    }

    private void initialize() {
        user = new User();
        user.setName("name");
        user.setEmail("email@email.ru");
        user = userRepository.save(user);

        item = new Item();
        item.setName("itemName");
        item.setDescription("itemDescription");
        item.setAvailable(true);
        item.setOwner(user);
        item = itemRepository.save(item);

        bookingDto = new BookingDto();
        bookingDto.setStart(LocalDateTime.MIN);
        bookingDto.setEnd(LocalDateTime.MAX);
        bookingDto.setItemId(item.getId());
    }

    private void assertResponseCorrect(ResponseBookingDto resp) {
        assertThat(resp, notNullValue());
        assertThat(resp.getStart(), equalTo(bookingDto.getStart()));
        assertThat(resp.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(resp.getItem().getId(), equalTo(item.getId()));
        assertThat(resp.getItem().getName(), equalTo(item.getName()));
        assertThat(resp.getBooker().getId(), equalTo(user.getId()));
        assertThat(resp.getBooker().getName(), equalTo(user.getName()));
    }

    private void assertResponseEquals(ResponseBookingDto resp1, ResponseBookingDto resp2) {
        // Сравнивает все поля, у которых есть геттеры, за исключением указанных
        assertThat(resp1, samePropertyValuesAs(resp2, "item", "booker"));
        assertThat(resp1.getItem().getId(), equalTo(resp2.getItem().getId()));
        assertThat(resp1.getItem().getName(), equalTo(resp2.getItem().getName()));
        assertThat(resp1.getBooker().getId(), equalTo(resp2.getBooker().getId()));
        assertThat(resp1.getBooker().getName(), equalTo(resp2.getBooker().getName()));
    }
}
