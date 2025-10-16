package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.common.exceptions.InternalServerException;
import ru.practicum.shareit.common.exceptions.NotFoundException;
import ru.practicum.shareit.common.exceptions.OwnershipException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemExtendedDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceUnitTest {
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private UserService userService;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User user;
    private ItemDto itemDto;
    private Booking lastBooking;
    private Booking nextBooking;
    private Comment comment1;
    private Comment comment2;

    /*
    public ItemDto create(long userId, ItemDto itemDto);
    */
    @Test
    void createShouldThrowNotFoundExceptionIfUserNotExist() {
        when(userRepository.findById(-1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.create(-1L, itemDto))
                .isInstanceOf(NotFoundException.class);

        verify(itemRepository, never()).save(any());
    }

    @Test
    void createShouldThrowNotFoundExceptionIfItemRequestNotExist() {
        initialize();
        itemDto.setRequestId(-1L);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(-1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.create(user.getId(), itemDto))
                .isInstanceOf(NotFoundException.class);

        verify(itemRepository, never()).save(any());
    }

    @Test
    void createShouldReturnItemDtoWithCorrectFields() {
        initialize();
        Item item = ItemMapper.toItem(itemDto, user, null);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        item.setId(1);
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        ItemDto result = itemService.create(user.getId(), itemDto);

        verify(itemRepository, times(1)).save(argThat(saved ->
                saved.getName().equals(itemDto.getName()) &&
                        saved.getDescription().equals(itemDto.getDescription()) &&
                        saved.isAvailable() == itemDto.getAvailable() &&
                        saved.getOwner().equals(user) &&
                        saved.getRequest() == null));

        assertThat(result.getId(), equalTo(1L));
        assertThat(result.getName(), equalTo(itemDto.getName()));
        assertThat(result.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(result.getAvailable(), equalTo(itemDto.getAvailable()));
        assertThat(result.getOwnerId(), equalTo(user.getId()));
        assertThat(result.getRequestId(), equalTo(null));
    }

    /*
    ItemDto update(long userId, ItemUpdateDto itemUpdateDto, long itemId);
    */

    @Test
    void updateShouldThrowNotFoundExceptionIfItemNotExist() {
        doNothing().when(userService).checkUserExist(anyLong());
        when(itemRepository.findById(-1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.update(1, null, -1L))
                .isInstanceOf(NotFoundException.class);

        verify(itemRepository, never()).save(any());
    }

    @Test
    void updateShouldThrowNotFoundExceptionWhenUserDoesNotOwnItem() {
        initialize();
        doNothing().when(userService).checkUserExist(anyLong());
        Item item = new Item();
        item.setOwner(user);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> itemService.update(-1L, null, -1L))
                .isInstanceOf(OwnershipException.class);

        verify(itemRepository, never()).save(any());
    }

    @Test
    void updateShouldCallSaveWithUpdatedFields() {
        initialize();
        doNothing().when(userService).checkUserExist(anyLong());
        Item item = ItemMapper.toItem(itemDto, user, null);
        item.setOwner(user);
        item.setId(1);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        ItemUpdateDto itemUpdateDto = new ItemUpdateDto();
        itemUpdateDto.setName("newName");
        itemUpdateDto.setDescription("newDescription");
        itemUpdateDto.setAvailable(false);


        when(itemRepository.save(any(Item.class))).thenReturn(item);
        ItemDto result = itemService.update(user.getId(), itemUpdateDto, item.getId());

        verify(itemRepository, times(1)).save(argThat(saved ->
                saved.getName().equals(itemUpdateDto.getName()) &&
                        saved.getDescription().equals(itemUpdateDto.getDescription()) &&
                        saved.isAvailable() == itemUpdateDto.getAvailable()));

        assertThat(result.getId(), equalTo(1L));
        assertThat(result.getName(), equalTo(itemUpdateDto.getName()));
        assertThat(result.getDescription(), equalTo(itemUpdateDto.getDescription()));
        assertThat(result.getAvailable(), equalTo(itemUpdateDto.getAvailable()));
        assertThat(result.getOwnerId(), equalTo(user.getId()));
        assertThat(result.getRequestId(), equalTo(null));
    }

    /*
    ItemExtendedDto get(long itemId);
    */

    @Test
    void getShouldThrowNotFoundExceptionIfItemNotExist() {
        when(itemRepository.findById(-1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.get(-1L))
                .isInstanceOf(NotFoundException.class);
    }

    //get должен вернуть Dto с bookings и comments
    @Test
    void getShouldReturnDtoWithBookingsAndComments() {
        initialize();

        Item item = new Item();
        item.setId(1);
        item.setOwner(user);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        initializeForExtendedDtoMethods();

        when(bookingRepository.findLastBooking(anyLong(), any(LocalDateTime.class)))
                .thenReturn(Optional.of(lastBooking));
        when(bookingRepository.findNextBooking(anyLong(), any(LocalDateTime.class)))
                .thenReturn(Optional.of(nextBooking));
        when(commentRepository.findAllByItemId(anyLong()))
                .thenReturn(List.of(comment1, comment2));

        ItemExtendedDto result = itemService.get(1);

        assertThat(result.getId(), equalTo(1L));
        assertThat(result.getName(), equalTo(item.getName()));
        assertThat(result.getDescription(), equalTo(item.getDescription()));
        assertThat(result.getAvailable(), equalTo(item.isAvailable()));
        assertThat(result.getOwnerId(), equalTo(user.getId()));
        assertThat(result.getLastBooking().getId(), equalTo(lastBooking.getId()));
        assertThat(result.getNextBooking().getId(), equalTo(nextBooking.getId()));
        assertThat(result.getComments(), hasSize(2));
    }

    /*
    List<ItemExtendedDto> findItemsByOwnerId(long userId);
    */
    @Test
    void findItemsByOwnerIdShouldReturnItemExtendedDtoList() {
        doNothing().when(userService).checkUserExist(anyLong());
        initialize();

        Item item = new Item();
        item.setId(1);
        item.setOwner(user);

        initializeForExtendedDtoMethods();

        when(itemRepository.findAllByOwnerId(anyLong())).thenReturn(List.of(item));
        when(bookingRepository.findLastBooking(anyLong(), any(LocalDateTime.class)))
                .thenReturn(Optional.of(lastBooking));
        when(bookingRepository.findNextBooking(anyLong(), any(LocalDateTime.class)))
                .thenReturn(Optional.of(nextBooking));
        when(commentRepository.findAllByItemId(anyLong()))
                .thenReturn(List.of(comment1, comment2));

        List<ItemExtendedDto> result = itemService.findItemsByOwnerId(user.getId());
        ItemExtendedDto first = result.getFirst();

        assertThat(first.getId(), equalTo(1L));
        assertThat(first.getName(), equalTo(item.getName()));
        assertThat(first.getDescription(), equalTo(item.getDescription()));
        assertThat(first.getAvailable(), equalTo(item.isAvailable()));
        assertThat(first.getOwnerId(), equalTo(user.getId()));
        assertThat(first.getLastBooking().getId(), equalTo(lastBooking.getId()));
        assertThat(first.getNextBooking().getId(), equalTo(nextBooking.getId()));
        assertThat(first.getComments(), hasSize(2));
    }

    /*
    List<ItemDto> findItem(String text);
    */
    @Test
    void findItemShouldReturnEmptyList() {
        List<ItemDto> result = itemService.findItem("");
        assertThat(result, hasSize(0));
    }

    @Test
    void findItemShouldReturnItemDtoList() {
        initialize();

        Item item1 = new Item();
        item1.setId(1);
        item1.setOwner(user);
        Item item2 = new Item();
        item2.setId(2);
        item2.setOwner(user);

        when(itemRepository.findByText("text")).thenReturn(List.of(item1, item2));

        //корректность работы маппера проверяется в createShouldReturnItemDtoWithCorrectFields
        //поэтому достаточно проверить, что возвращается корректный список
        List<ItemDto> result = itemService.findItem("text");

        assertThat(result.getFirst().getId(), equalTo(item1.getId()));
        assertThat(result.getLast().getId(), equalTo(item2.getId()));
    }

    /*
    CommentDto comment(long authorId, CommentDto commentDto, long itemId);
    */
    @Test
    void commentShouldThrowInternalServerExceptionIfUserDidNotBookItem() {
        when(bookingRepository.existsFinishedBookingByBookerIdAndItemId(anyLong(),
                anyLong(),
                any(LocalDateTime.class)))
                .thenReturn(false);

        assertThatThrownBy(() -> itemService.comment(-1L, null, -1L))
                .isInstanceOf(InternalServerException.class);

        verify(commentRepository, never()).save(any());
    }

    @Test
    void commentShouldThrowNotFoundExceptionIfUserDoesNotExists() {
        when(bookingRepository.existsFinishedBookingByBookerIdAndItemId(anyLong(),
                anyLong(),
                any(LocalDateTime.class)))
                .thenReturn(true);
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.comment(-1L, null, -1L))
                .isInstanceOf(NotFoundException.class);

        verify(commentRepository, never()).save(any());
    }

    @Test
    void commentShouldThrowNotFoundExceptionIfItemDoesNotExists() {
        initialize();
        when(bookingRepository.existsFinishedBookingByBookerIdAndItemId(anyLong(),
                anyLong(),
                any(LocalDateTime.class)))
                .thenReturn(true);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.comment(-1L, null, -1L))
                .isInstanceOf(NotFoundException.class);

        verify(commentRepository, never()).save(any());
    }

    @Test
    void commentShouldReturnCorrectDto() throws InterruptedException {
        initialize();
        when(bookingRepository.existsFinishedBookingByBookerIdAndItemId(anyLong(),
                anyLong(),
                any(LocalDateTime.class)))
                .thenReturn(true);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        Item item = new Item();
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        CommentDto commentDto = new CommentDto();
        commentDto.setText("someText");
        commentDto.setAuthorName(user.getName());
        commentDto.setId(1);
        commentDto.setCreated(LocalDateTime.now());

        when(commentRepository.save(any(Comment.class))).thenReturn(CommentMapper.toComment(commentDto, item, user));

        CommentDto result = itemService.comment(user.getId(), commentDto, item.getId());

        verify(commentRepository, times(1)).save(argThat(saved ->
                saved.getText().equals(commentDto.getText()) &&
                        saved.getAuthor().getName() == commentDto.getAuthorName()));

        assertThat(result.getText(), equalTo(commentDto.getText()));
        assertThat(result.getAuthorName(), equalTo(commentDto.getAuthorName()));
    }

    /*
    void checkItemExists(long itemId);
    */
    @Test
    void checkItemExistsShouldThrowNotFoExceptionIfItemDoesNotExists() {
        when(itemRepository.existsById(anyLong())).thenReturn(false);

        assertThatThrownBy(() -> itemService.checkItemExists(-1L))
                .isInstanceOf(NotFoundException.class);
    }

    /*
    void checkItemOwnership(long userId, Item item);
    */
    @Test
    void checkItemOwnershipShouldThrowOwnershipExceptionIfUserDoesNotOwnItem() {
        initialize();
        Item item = new Item();
        item.setOwner(user);

        assertThatThrownBy(() -> itemService.checkItemOwnership(-1L, item))
                .isInstanceOf(OwnershipException.class);
    }

    private void initializeForExtendedDtoMethods() {
        lastBooking = new Booking();
        lastBooking.setId(1);
        lastBooking.setBooker(new User());
        nextBooking = new Booking();
        nextBooking.setId(2);
        nextBooking.setBooker(new User());
        comment1 = new Comment();
        comment1.setAuthor(new User());
        comment2 = new Comment();
        comment2.setAuthor(new User());
    }

    private void initialize() {
        user = makeUser("someName", "some@email.ru");
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
        user.setId(1);
        user.setName(name);
        user.setEmail(email);
        return user;
    }
}
