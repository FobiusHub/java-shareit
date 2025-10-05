package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long>, QuerydslPredicateExecutor<Booking> {
    @Query("SELECT b from Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.end >= :now " +
            "AND b.start <= :now " +
            "ORDER BY b.end DESC " +
            "LIMIT 1")
    Optional<Booking> findLastBooking(@Param("itemId") long itemId, @Param("now") LocalDateTime now);

    @Query("SELECT b from Booking b " +
            "WHERE b.item.id = :itemId AND b.start > :now " +
            "ORDER BY b.start ASC " +
            "LIMIT 1")
    Optional<Booking> findNextBooking(@Param("itemId") long itemId, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.status = 'APPROVED'" +
            "AND b.booker.id = :bookerId " +
            "AND b.item.id = :itemId " +
            "AND b.end <= :now")
    boolean existsFinishedBookingByBookerIdAndItemId(@Param("bookerId") long bookerId,
                                                     @Param("itemId") long itemId,
                                                     @Param("now") LocalDateTime now);
}
