package main_service.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import main_service.event.enums.RequestStatus;

import java.time.LocalDateTime;

@Data
public class ParticipationRequestDto {
    private Long id;
    private Long event;
    private Long requester;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;
    private RequestStatus status;
}
