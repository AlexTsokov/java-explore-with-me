package mainservice.compilation.dto;

import lombok.Data;
import mainservice.event.dto.EventShortDto;

import javax.validation.constraints.Size;
import java.util.List;

@Data
public class CompilationDto {
    private Long id;
    @Size(min = 1, max = 50)
    private String title;
    private Boolean pinned;
    private List<EventShortDto> events;
}
