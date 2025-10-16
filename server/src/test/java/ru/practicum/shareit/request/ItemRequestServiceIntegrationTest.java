package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.common.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ResponseItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestExtendedDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestServiceIntegrationTest {
    private final ItemRequestService itemRequestService;
    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;

    private User user;
    private User itemOwner;
    private Item item;
    private Item item2;
    private ItemRequestDto itemRequestDto;
    private ItemRequestDto secondItemRequestDto;

    @Test
    void createShouldThrowNotFoundExceptionIfUserIsNotExist() {
        assertThrows(NotFoundException.class, () -> {
            itemRequestService.create(-1, new ItemRequestDto());
        });
    }

    @Test
    void createShouldCorrectlySaveItemRequestAndReturnCorrectDto() {
        initialize();

        itemRequestDto = itemRequestService.create(user.getId(), itemRequestDto);
        ItemRequest itemRequest = itemRequestRepository.findById(itemRequestDto.getId()).get();
        assertThat(itemRequestDto, notNullValue());
        assertThat(itemRequestDto.getId(), equalTo(itemRequest.getId()));
        assertThat(itemRequestDto.getDescription(), equalTo(itemRequest.getDescription()));
        assertThat(itemRequestDto.getRequesterId(), equalTo(itemRequest.getRequester().getId()));
        assertThat(itemRequestDto.getCreated(), equalTo(itemRequest.getCreated()));
    }

    @Test
    void getOwnRequestsShouldThrowNotFoundExceptionIfUserIsNotExist() {
        assertThrows(NotFoundException.class, () -> {
            itemRequestService.getOwnRequests(-1);
        });
    }

    @Test
    void getOwnRequestsShouldReturnEmptyListIfTheUserDidNotHaveAnyRequests() {
        initialize();

        List<ItemRequestExtendedDto> result = itemRequestService.getOwnRequests(user.getId());

        assertThat(result, empty());
    }

    @Test
    void getOwnRequestsShouldReturnCorrectDescSortedDtoList() throws InterruptedException {
        extendedInitialize();

        List<ItemRequestExtendedDto> result = itemRequestService.getOwnRequests(user.getId());
        ItemRequestExtendedDto first = result.getFirst();

        assertThat(result, hasSize(2));
        assertThat(first.getId(), equalTo(secondItemRequestDto.getId()));
        assertThat(first.getDescription(), equalTo(secondItemRequestDto.getDescription()));
        assertThat(first.getRequesterId(), equalTo(user.getId()));
        assertThat(first.getCreated(), equalTo(secondItemRequestDto.getCreated()));

        ResponseItemDto requestItem = first.getItems().getFirst();
        assertThat(requestItem.getId(), equalTo(item2.getId()));
        assertThat(requestItem.getName(), equalTo(item2.getName()));
        assertThat(requestItem.getOwnerId(), equalTo(itemOwner.getId()));
    }

    @Test
    void getAllRequestsShouldThrowNotFoundExceptionIfUserIsNotExist() {
        assertThrows(NotFoundException.class, () -> {
            itemRequestService.getAllRequests(-1);
        });
    }

    @Test
    void getAllRequestsShouldReturnCorrectDescSortedDtoList() throws InterruptedException {
        extendedInitialize();

        // В список не должны попасть свои запросы
        ItemRequestDto otherItemRequest = new ItemRequestDto();
        otherItemRequest.setDescription("otherDescription");
        itemRequestService.create(itemOwner.getId(), otherItemRequest);

        List<ItemRequestDto> result = itemRequestService.getAllRequests(itemOwner.getId());
        ItemRequestDto first = result.getFirst();

        assertThat(result, hasSize(2));
        assertThat(first.getId(), equalTo(secondItemRequestDto.getId()));
        assertThat(first.getDescription(), equalTo(secondItemRequestDto.getDescription()));
        assertThat(first.getRequesterId(), equalTo(user.getId()));
        assertThat(first.getCreated(), equalTo(secondItemRequestDto.getCreated()));
    }

    @Test
    void getRequestShouldThrowNotFoundExceptionIfUserIsNotExist() {
        assertThrows(NotFoundException.class, () -> {
            itemRequestService.getRequest(-1, -1);
        });
    }

    @Test
    void getRequestShouldThrowNotFoundExceptionIfRequestIsNotExist() {
        initialize();

        assertThrows(NotFoundException.class, () -> {
            itemRequestService.getRequest(user.getId(), -1);
        });
    }

    @Test
    void getRequestShouldReturnCorrectData() throws InterruptedException {
        extendedInitialize();

        ItemRequestExtendedDto result = itemRequestService.getRequest(user.getId(), itemRequestDto.getId());

        assertThat(result, notNullValue());
        assertThat(result.getId(), equalTo(itemRequestDto.getId()));
        assertThat(result.getDescription(), equalTo(itemRequestDto.getDescription()));
        assertThat(result.getRequesterId(), equalTo(user.getId()));
        assertThat(result.getCreated(), equalTo(itemRequestDto.getCreated()));
        assertThat(result.getItems(), hasSize(1));

        ResponseItemDto requestItem = result.getItems().getFirst();
        assertThat(requestItem.getId(), equalTo(item.getId()));
        assertThat(requestItem.getName(), equalTo(item.getName()));
        assertThat(requestItem.getOwnerId(), equalTo(itemOwner.getId()));
    }

    private void initialize() {
        user = new User();
        user.setName("userName");
        user.setEmail("user@email.ru");
        user = userRepository.save(user);

        itemRequestDto = new ItemRequestDto();
        itemRequestDto.setDescription("description");
    }

    private void extendedInitialize() throws InterruptedException {
        user = new User();
        user.setName("userName");
        user.setEmail("user@email.ru");
        user = userRepository.save(user);

        itemRequestDto = new ItemRequestDto();
        itemRequestDto.setDescription("description");
        itemRequestDto = itemRequestService.create(user.getId(), itemRequestDto);

        Thread.sleep(1000);

        secondItemRequestDto = new ItemRequestDto();
        secondItemRequestDto.setDescription("description2");
        secondItemRequestDto = itemRequestService.create(user.getId(), secondItemRequestDto);

        ItemRequest itemRequest = itemRequestRepository.findById(itemRequestDto.getId()).get();
        ItemRequest itemRequest2 = itemRequestRepository.findById(secondItemRequestDto.getId()).get();

        itemOwner = new User();
        itemOwner.setName("itemOwnerName");
        itemOwner.setEmail("itemOwner@email.ru");
        itemOwner = userRepository.save(itemOwner);

        item = new Item();
        item.setName("itemName");
        item.setDescription("description");
        item.setAvailable(true);
        item.setOwner(itemOwner);
        item.setRequest(itemRequest);
        item = itemRepository.save(item);

        item2 = new Item();
        item2.setName("item2Name");
        item2.setDescription("description2");
        item2.setAvailable(true);
        item2.setOwner(itemOwner);
        item2.setRequest(itemRequest2);
        item2 = itemRepository.save(item2);
    }
}
