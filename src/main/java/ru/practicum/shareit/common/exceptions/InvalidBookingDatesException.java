package ru.practicum.shareit.common.exceptions;

public class InvalidBookingDatesException extends RuntimeException {
  public InvalidBookingDatesException(String message) {
    super(message);
  }
}
