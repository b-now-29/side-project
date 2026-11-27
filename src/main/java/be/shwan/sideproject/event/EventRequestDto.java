package be.shwan.sideproject.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public record EventRequestDto(
    @NotBlank(message = "제목을 입력해주세요") String title,
    @Positive int totalCount,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime eventStartDateTime,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime eventEndDateTime) {}
