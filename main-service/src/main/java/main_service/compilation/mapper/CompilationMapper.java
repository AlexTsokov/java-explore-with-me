package main_service.compilation.mapper;

import main_service.compilation.dto.CompilationDto;
import main_service.compilation.dto.NewCompilationDto;
import main_service.compilation.model.Compilation;
import main_service.event.dto.EventShortDto;
import main_service.event.model.Event;
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