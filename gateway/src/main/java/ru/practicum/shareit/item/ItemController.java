package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;


@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") long userId,
                                         @Valid @RequestBody ItemDto itemDto) {
        log.info("Create item with name {}, description {} and available {}",
                itemDto.getName(), itemDto.getDescription(), itemDto.getAvailable());
        return itemClient.create(userId, itemDto);
    }

    @PatchMapping("{itemId}")
    public ResponseEntity<Object> update(@RequestHeader("X-Sharer-User-Id") long userId,
                                         @Valid @RequestBody ItemUpdateDto itemUpdateDto,
                                         @PathVariable long itemId) {
        log.info("Updating item id {} with name {}, description {} and available {}",
                itemId, itemUpdateDto.getName(), itemUpdateDto.getDescription(), itemUpdateDto.getAvailable());
        return itemClient.update(userId, itemUpdateDto, itemId);
    }

    @GetMapping("{itemId}")
    public ResponseEntity<Object> read(@PathVariable long itemId) {
        log.info("Get item id {}", itemId);
        return itemClient.get(itemId);
    }

    @GetMapping
    public ResponseEntity<Object> findItemsByOwnerId(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Find items by owner id {}", userId);
        return itemClient.findItemsByOwnerId(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> findItem(@RequestParam String text) {
        log.info("Find items by text {}", text);
        return itemClient.findItem(text);
    }

    @PostMapping("{itemId}/comment")
    public ResponseEntity<Object> comment(@RequestHeader("X-Sharer-User-Id") long authorId,
                                          @RequestBody CommentDto commentDto,
                                          @PathVariable long itemId) {
        log.info("Comment: {} for item id {} from author id {}", commentDto.getText(), itemId, authorId);
        return itemClient.comment(authorId, commentDto, itemId);
    }
}
