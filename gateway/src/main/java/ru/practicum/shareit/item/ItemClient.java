package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> create(long userId, ItemDto itemDto) {
        return post("", userId, itemDto);
    }

    public ResponseEntity<Object> update(long userId, ItemUpdateDto itemUpdateDto, long itemId) {
        return patch("/{itemId}", userId, Map.of("itemId", itemId), itemUpdateDto);
    }

    public ResponseEntity<Object> get(long itemId) {
        return get("/{itemId}", null, Map.of("itemId", itemId));
    }

    public ResponseEntity<Object> findItemsByOwnerId(long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> findItem(String text) {
        return get("/search?text=" + text);
    }

    public ResponseEntity<Object> comment(long authorId, CommentDto commentDto, long itemId) {
        return post("/{itemId}/comment", authorId, Map.of("itemId", itemId), commentDto);
    }
}
