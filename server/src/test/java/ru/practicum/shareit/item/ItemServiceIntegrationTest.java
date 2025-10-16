package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.common.exceptions.InternalServerException;
import ru.practicum.shareit.common.exceptions.NotFoundException;
import ru.practicum.shareit.common.exceptions.OwnershipException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemExtendedDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@Transactional
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceIntegrationTest {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    private final ItemServiceImpl itemService;

    private User user;
    private ItemDto itemDto;
    private Item item;
    private User booker1;
    private User booker2;
    private Booking lastBooking;
    private Booking nextBooking;
    private Comment comment1;
    private Comment comment2;

    @Test
    void createShouldThrowNotFoundExceptionIfUserNotExist() {
        assertThatThrownBy(() -> itemService.create(-1L, itemDto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void createShouldThrowNotFoundExceptionIfItemRequestNotExist() {
        initialize();
        itemDto.setRequestId(-1L);

        assertThatThrownBy(() -> itemService.create(user.getId(), itemDto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void createShouldReturnItemDtoWithCorrectFields() {
        initialize();

        ItemDto result = itemService.create(user.getId(), itemDto);

        assertThat(result.getName(), equalTo(itemDto.getName()));
        assertThat(result.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(result.getAvailable(), equalTo(itemDto.getAvailable()));
        assertThat(result.getOwnerId(), equalTo(user.getId()));
        assertThat(result.getRequestId(), equalTo(null));
    }

    @Test
    void updateShouldThrowNotFoundExceptionIfItemNotExist() {
        initialize();


        assertThatThrownBy(() -> itemService.update(user.getId(), null, -1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateShouldThrowNotFoundExceptionWhenUserDoesNotOwnItem() {
        initialize();

        User owner = new User();
        owner.setName("name");
        owner.setEmail("email@email.ru");
        userRepository.save(owner);

        item = new Item();
        item.setName("name");
        item.setDescription("description");
        item.setOwner(owner);
        itemRepository.save(item);

        long itemId = item.getId();

        assertThatThrownBy(() -> itemService.update(user.getId(), new ItemUpdateDto(), itemId))
                .isInstanceOf(OwnershipException.class);
    }

    @Test
    void updateShouldCallSaveWithUpdatedFields() {
        initialize();

        Item item = ItemMapper.toItem(itemDto, user, null);
        item = itemRepository.save(item);

        ItemUpdateDto itemUpdateDto = new ItemUpdateDto();
        itemUpdateDto.setName("newName");
        itemUpdateDto.setDescription("newDescription");
        itemUpdateDto.setAvailable(false);

        ItemDto result = itemService.update(user.getId(), itemUpdateDto, item.getId());

        assertThat(result.getId(), equalTo(item.getId()));
        assertThat(result.getName(), equalTo(itemUpdateDto.getName()));
        assertThat(result.getDescription(), equalTo(itemUpdateDto.getDescription()));
        assertThat(result.getAvailable(), equalTo(itemUpdateDto.getAvailable()));
        assertThat(result.getOwnerId(), equalTo(user.getId()));
        assertThat(result.getRequestId(), equalTo(null));
    }

    @Test
    void getShouldThrowNotFoundExceptionIfItemNotExist() {
        assertThatThrownBy(() -> itemService.get(-1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getShouldReturnDtoWithBookingsAndComments() {
        initialize();

        initializeForExtendedDtoMethods();

        long itemId = item.getId();

        ItemExtendedDto result = itemService.get(itemId);

        assertThat(result.getId(), equalTo(itemId));
        assertThat(result.getName(), equalTo(item.getName()));
        assertThat(result.getDescription(), equalTo(item.getDescription()));
        assertThat(result.getAvailable(), equalTo(item.isAvailable()));
        assertThat(result.getOwnerId(), equalTo(user.getId()));
        assertThat(result.getLastBooking().getId(), equalTo(lastBooking.getId()));
        assertThat(result.getNextBooking().getId(), equalTo(nextBooking.getId()));
        assertThat(result.getComments(), hasSize(2));
    }

    @Test
    void findItemsByOwnerIdShouldReturnItemExtendedDtoList() {
        initialize();

        initializeForExtendedDtoMethods();

        List<ItemExtendedDto> result = itemService.findItemsByOwnerId(user.getId());
        ItemExtendedDto first = result.getFirst();

        assertThat(first.getId(), equalTo(item.getId()));
        assertThat(first.getName(), equalTo(item.getName()));
        assertThat(first.getDescription(), equalTo(item.getDescription()));
        assertThat(first.getAvailable(), equalTo(item.isAvailable()));
        assertThat(first.getOwnerId(), equalTo(user.getId()));
        assertThat(first.getLastBooking().getId(), equalTo(lastBooking.getId()));
        assertThat(first.getNextBooking().getId(), equalTo(nextBooking.getId()));
        assertThat(first.getComments(), hasSize(2));
    }

    @Test
    void findItemShouldReturnEmptyList() {
        List<ItemDto> result = itemService.findItem("");
        assertThat(result, hasSize(0));
    }

    @Test
    void findItemShouldReturnItemDtoList() {
        initialize();

        Item item1 = new Item();
        item1.setOwner(user);
        item1.setName("name");
        item1.setDescription("someDescription");
        item1.setAvailable(true);
        item1 = itemRepository.save(item1);

        Item item2 = new Item();
        item2.setOwner(user);
        item2.setName("name");
        item2.setDescription("otherDescription");
        item2.setAvailable(true);
        item2 = itemRepository.save(item2);

        List<ItemDto> result = itemService.findItem("description");

        assertThat(result.getFirst().getId(), equalTo(item1.getId()));
        assertThat(result.getLast().getId(), equalTo(item2.getId()));
    }

    @Test
    void commentShouldThrowInternalServerExceptionIfUserDidNotBookItem() {
        initialize();
        initializeForExtendedDtoMethods();

        assertThatThrownBy(() -> itemService.comment(user.getId(), null, item.getId()))
                .isInstanceOf(InternalServerException.class);
    }

    @Test
    void commentShouldThrowNotFoundExceptionIfUserDoesNotExists() {
        assertThatThrownBy(() -> itemService.comment(-1L, null, -1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void commentShouldThrowNotFoundExceptionIfItemDoesNotExists() {
        initialize();

        assertThatThrownBy(() -> itemService.comment(user.getId(), null, -1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void commentShouldReturnCorrectDto() throws InterruptedException {
        booker1 = new User();
        booker1.setName("booker1");
        booker1.setEmail("booker1@email.ru");
        userRepository.save(booker1);

        item = new Item();
        item.setName("name");
        item.setDescription("description");
        item.setOwner(user);
        item = itemRepository.save(item);

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker1);
        booking.setStart(LocalDateTime.of(2025, 10, 15, 12, 30));
        booking.setEnd(LocalDateTime.of(2025, 10, 15, 12, 45));
        booking.setStatus(Status.APPROVED);
        bookingRepository.save(booking);

        CommentDto commentDto = new CommentDto();
        commentDto.setText("someText");

        CommentDto result = itemService.comment(booker1.getId(), commentDto, item.getId());

        assertThat(result.getText(), equalTo(commentDto.getText()));
        assertThat(result.getAuthorName(), equalTo(booker1.getName()));
    }

    @Test
    void checkItemExistsShouldThrowNotFoExceptionIfItemDoesNotExists() {
        assertThatThrownBy(() -> itemService.checkItemExists(-1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void checkItemOwnershipShouldThrowOwnershipExceptionIfUserDoesNotOwnItem() {
        initialize();
        item = new Item();
        item.setName("name");
        item.setDescription("description");
        item.setOwner(user);
        item = itemRepository.save(item);

        User otherUser = new User();
        otherUser.setName("booker1");
        otherUser.setEmail("booker1@email.ru");
        userRepository.save(otherUser);

        assertThatThrownBy(() -> itemService.checkItemOwnership(otherUser.getId(), item))
                .isInstanceOf(OwnershipException.class);
    }

    private void initializeForExtendedDtoMethods() {
        booker1 = new User();
        booker1.setName("booker1");
        booker1.setEmail("booker1@email.ru");
        userRepository.save(booker1);
        booker2 = new User();
        booker2.setName("booker2");
        booker2.setEmail("booker2@email.ru");
        userRepository.save(booker2);

        item = new Item();
        item.setName("name");
        item.setDescription("description");
        item.setOwner(user);
        item = itemRepository.save(item);

        lastBooking = new Booking();
        lastBooking.setItem(item);
        lastBooking.setBooker(booker1);
        lastBooking.setStart(LocalDateTime.of(2025, 10, 15, 12, 30));
        lastBooking.setEnd(LocalDateTime.of(2025, 12, 15, 12, 45));
        lastBooking.setStatus(Status.APPROVED);
        lastBooking = bookingRepository.save(lastBooking);

        nextBooking = new Booking();
        nextBooking.setItem(item);
        nextBooking.setBooker(booker2);
        nextBooking.setStart(LocalDateTime.of(2026, 2, 2, 12, 30));
        nextBooking.setEnd(LocalDateTime.of(2026, 2, 2, 12, 45));
        nextBooking.setStatus(Status.APPROVED);
        nextBooking = bookingRepository.save(nextBooking);

        comment1 = new Comment();
        comment1.setAuthor(booker1);
        comment1.setCreated(LocalDateTime.now());
        comment1.setText("text1");
        comment1.setItem(item);
        comment1 = commentRepository.save(comment1);

        comment2 = new Comment();
        comment2.setAuthor(booker2);
        comment2.setCreated(LocalDateTime.now());
        comment2.setText("text2");
        comment2.setItem(item);
        comment2 = commentRepository.save(comment2);
    }

    private void initialize() {
        user = makeUser("someName", "some@email.ru");
        user = userRepository.save(user);
        itemDto = makeItemDto("itemDtoName", "itemDtoDescription");
    }

    private ItemDto makeItemDto(String name, String description) {
        ItemDto itemDto = new ItemDto();
        itemDto.setName(name);
        itemDto.setDescription(description);
        itemDto.setAvailable(true);
        return itemDto;
    }

    private User makeUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return user;
    }
}
