package mainservice.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RequestStats {
    private Long eventId;
    private Long confirmedRequests;
}
