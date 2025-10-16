package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.dto.ShortItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByOwnerId(long ownerId);

    @Query("SELECT i FROM Item as i " +
            "WHERE i.available = true AND " +
            "(LOWER(i.name) LIKE CONCAT('%', LOWER (:text), '%') OR " +
            "LOWER(i.description) LIKE CONCAT('%', LOWER (:text), '%'))")
    List<Item> findByText(@Param("text") String text);

    ShortItemDto findShortItemDtoById(long itemId);

    List<Item> findAllByRequestId(long requestId);

    List<Item> findAllByRequestIdIn(List<Long> requestIds);
}
