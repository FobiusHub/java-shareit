package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.CommentDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CommentDtoJsonTest {
    private final JacksonTester<CommentDto> json;

    @Test
    void testCommentDto() throws Exception {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(1);
        commentDto.setText("commentDtoText");
        commentDto.setAuthorName("someAuthorName");
        commentDto.setCreated(LocalDateTime.of(2025, 10, 10, 12, 45));

        JsonContent<CommentDto> result = json.write(commentDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.text")
                .isEqualTo("commentDtoText");
        assertThat(result).extractingJsonPathStringValue("$.authorName").isEqualTo("someAuthorName");
        assertThat(result).extractingJsonPathStringValue("$.created")
                .isEqualTo("10.10.2025 12:45:00");
    }
}
