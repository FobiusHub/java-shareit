package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemExtendedDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.dto.CommentDto;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") long userId, @Valid @RequestBody ItemDto itemDto) {
        return itemService.create(userId, itemDto);
    }

    @PatchMapping("{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") long userId,
                          @Valid @RequestBody ItemUpdateDto itemUpdateDto,
                          @PathVariable long itemId) {
        return itemService.update(userId, itemUpdateDto, itemId);
    }

    @GetMapping("{itemId}")
    public ItemExtendedDto read(@PathVariable long itemId) {
        return itemService.get(itemId);
    }

    @GetMapping
    public List<ItemExtendedDto> findItemsByOwnerId(@RequestHeader("X-Sharer-User-Id") long userId) {
        return itemService.findItemsByOwnerId(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> findItem(@RequestParam String text) {
        return itemService.findItem(text);
    }

    @PostMapping("{itemId}/comment")
    public CommentDto comment(@RequestHeader("X-Sharer-User-Id") long authorId,
                              @RequestBody CommentDto commentDto,
                              @PathVariable long itemId) {
        return itemService.comment(authorId, commentDto, itemId);
    }
}
