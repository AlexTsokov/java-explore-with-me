package mainservice.compilation.mapper;

import mainservice.compilation.dto.CompilationDto;
import mainservice.compilation.dto.NewCompilationDto;
import mainservice.compilation.model.Compilation;
import mainservice.event.dto.EventShortDto;
import mainservice.event.model.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CompilationMapper {
    @Mapping(target = "id", expression = "java(null)")
    @Mapping(target = "events", expression = "java(events)")
    Compilation newDtoToCompilation(NewCompilationDto newCompilationDto, List<Event> events);

    @Mapping(target = "events", expression = "java(eventsShortDto)")
    CompilationDto toCompilationDto(Compilation compilation, List<EventShortDto> eventsShortDto);
}