package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.List;

@Component
public class InMemoryItemRepository implements ItemRepository {
    private long id = 0;
    private final HashMap<Long, Item> items = new HashMap<>();


    @Override
    public Item add(Item item) {
        id++;
        item.setId(id);
        items.put(id, item);
        return item;
    }

    @Override
    public Item get(long id) {
        return items.get(id);
    }

    @Override
    public Item update(ItemDto itemDto, long id) {
        Item item = items.get(id);

        String name = itemDto.getName();
        if (name != null && !name.isBlank()) {
            item.setName(name);
        }

        String description = itemDto.getDescription();
        if (description != null && !description.isBlank()) {
            item.setDescription(description);
        }

        Boolean available = itemDto.getAvailable();
        if (available != null) {
            item.setAvailable(available);
        }

        return item;
    }

    @Override
    public List<Item> getAll(long userId) {
        return items.values().stream()
                .filter(item -> item.getOwner().getId() == userId)
                .toList();
    }

    @Override
    public List<Item> findItem(String text) {
        String lowerCaseText = text.toLowerCase();
        return items.values().stream()
                .filter(Item::isAvailable)
                .filter(item ->
                        item.getName().toLowerCase().contains(lowerCaseText) ||
                                item.getDescription().toLowerCase().contains(lowerCaseText))
                .toList();
    }

    @Override
    public boolean itemExists(long id) {
        return items.containsKey(id);
    }
}
