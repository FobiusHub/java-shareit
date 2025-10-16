package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ShortItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.hasProperty;

@Transactional
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRepositoryIntegrationTest {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestRepository itemRequestRepository;

    private User user;
    private ItemRequest itemRequest1;
    private ItemRequest itemRequest2;
    private Item item1;
    private Item item2;

    @Test
    void findAllByOwnerIdShouldReturnCorrectItem() {
        initialize();
        User user2 = makeUser("SomeName2", "SomeEmail2@mail.ru");
        user2 = userRepository.save(user2);

        Item item3 = makeItem("item3Name", "item3Description", user2, null);
        item3 = itemRepository.save(item3);

        List<Item> items = itemRepository.findAllByOwnerId(user2.getId());

        assertThat(items, hasSize(1));
        checkItemResult(items, item3);
    }

    @Test
    void findAllByOwnerIdShouldReturnEmptyList() {
        List<Item> items = itemRepository.findAllByOwnerId(-1L);
        assertThat(items, hasSize(0));
    }

    @Test
    void findByTextShouldReturnItemsIfDescriptionExists() {
        initialize();

        Item item3 = makeItem("item3Name", "RedTable", user, null);
        itemRepository.save(item3);

        List<Item> items = itemRepository.findByText("tool");

        assertThat(items, hasSize(2));
        checkItemResult(items, item1);
        checkItemResult(items, item2);
    }

    @Test
    void findByTextShouldReturnEmptyListIfDescriptionNotExists() {
        initialize();

        List<Item> items = itemRepository.findByText("spoon");

        assertThat(items, hasSize(0));
    }

    @Test
    void findShortItemDtoByIdShouldReturnCorrectDto() {
        initialize();

        ShortItemDto dto = itemRepository.findShortItemDtoById(item1.getId());

        assertThat(item1.getId(), equalTo(dto.getId()));
        assertThat(user.getName(), notNullValue());
        assertThat(item1.getName(), equalTo(dto.getName()));
    }

    @Test
    void findAllByRequestIdShouldReturnCorrectItems() {
        initialize();

        List<Item> items = itemRepository.findAllByRequestId(itemRequest1.getId());

        assertThat(items, hasSize(2));
        checkItemResult(items, item1);
        checkItemResult(items, item2);
    }

    @Test
    void findAllByRequestIdShouldReturnEmptyList() {
        List<Item> items = itemRepository.findAllByRequestId(-1L);
        assertThat(items, hasSize(0));
    }

    @Test
    void findAllByRequestIdInShouldReturnCorrectItems() {
        initialize();

        List<Long> requestIds = List.of(itemRequest1.getId(), itemRequest2.getId());

        List<Item> items = itemRepository.findAllByRequestIdIn(requestIds);

        assertThat(items, hasSize(2));

        checkItemResult(items, item1);
        checkItemResult(items, item2);
    }

    @Test
    void findAllByRequestIdInShouldReturnEmptyList() {
        List<Long> ids = List.of(-1L, -2L);

        List<Item> items = itemRepository.findAllByRequestIdIn(ids);
        assertThat(items, hasSize(0));
    }

    private void checkItemResult(List<Item> items, Item item) {
        assertThat(items, hasItem(allOf(
                hasProperty("id", equalTo(item.getId())),
                hasProperty("name", equalTo(item.getName())),
                hasProperty("description", equalTo(item.getDescription())),
                hasProperty("request", equalTo(item.getRequest())),
                hasProperty("owner", equalTo(item.getOwner())))));
    }

    private void initialize() {
        user = makeUser("SomeName", "SomeEmail@mail.ru");
        user = userRepository.save(user);

        itemRequest1 = makeItemRequest("someDescription1", user);
        itemRequest1 = itemRequestRepository.save(itemRequest1);

        itemRequest2 = makeItemRequest("someDescription2", user);
        itemRequest2 = itemRequestRepository.save(itemRequest2);

        item1 = makeItem("item1Name", "Tool1", user, itemRequest1);
        item1 = itemRepository.save(item1);

        item2 = makeItem("item2Name", "Tool2", user, itemRequest1);
        item2 = itemRepository.save(item2);
    }

    private ItemRequest makeItemRequest(String description, User requester) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(description);
        itemRequest.setRequester(requester);
        itemRequest.setCreated(LocalDateTime.now());
        return itemRequest;
    }

    private Item makeItem(String name, String description, User user, ItemRequest itemRequest) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(true);
        item.setOwner(user);
        item.setRequest(itemRequest);
        return item;
    }

    private User makeUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return user;
    }
}
