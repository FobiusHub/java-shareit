package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ResponseItemDto;
import ru.practicum.shareit.request.dto.ItemRequestExtendedDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestExtendedDtoJsonTest {
    private final JacksonTester<ItemRequestExtendedDto> json;

    @Test
    void testItemRequestExtendedDto() throws Exception {
        ResponseItemDto responseItemDto = new ResponseItemDto();
        responseItemDto.setId(5);
        responseItemDto.setName("responseItemDtoТame");
        responseItemDto.setOwnerId(15);

        ItemRequestExtendedDto itemRequestExtendedDto = new ItemRequestExtendedDto();
        itemRequestExtendedDto.setId(1);
        itemRequestExtendedDto.setDescription("itemRequestExtendedDtoDescription");
        itemRequestExtendedDto.setRequesterId(12);
        itemRequestExtendedDto.setItems(List.of(responseItemDto));
        itemRequestExtendedDto.setCreated(LocalDateTime.of(2025, 10, 10, 12, 45));

        JsonContent<ItemRequestExtendedDto> result = json.write(itemRequestExtendedDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo("itemRequestExtendedDtoDescription");
        assertThat(result).extractingJsonPathNumberValue("$.requesterId").isEqualTo(12);
        assertThat(result).extractingJsonPathArrayValue("$.items").hasSize(1);
        assertThat(result).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(5);
        assertThat(result).extractingJsonPathStringValue("$.items[0].name")
                .isEqualTo("responseItemDtoТame");
        assertThat(result).extractingJsonPathNumberValue("$.items[0].ownerId").isEqualTo(15);
        assertThat(result).extractingJsonPathStringValue("$.created")
                .isEqualTo("10.10.2025 12:45:00");
    }
}
