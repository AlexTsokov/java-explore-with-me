package mainservice.comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import mainservice.user.dto.UserShortDto;

import java.time.LocalDateTime;

@Data
public class CommentDto {
    private long id;
    private UserShortDto author;
    private Long eventId;
    private String text;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime edited;
}
