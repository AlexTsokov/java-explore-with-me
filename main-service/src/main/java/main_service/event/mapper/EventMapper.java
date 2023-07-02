package main_service.event.mapper;

import main_service.category.mapper.CategoryMapper;
import main_service.category.model.Category;
import main_service.event.dto.EventFullDto;
import main_service.event.dto.EventShortDto;
import main_service.event.dto.NewEventDto;
import main_service.event.enums.EventState;
import main_service.event.model.Event;
import main_service.event.model.Location;
import main_service.user.mapper.UserMapper;
import main_service.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring", uses = {UserMapper.class, CategoryMapper.class, LocationMapper.class})
public interface EventMapper {
    @Mapping(target = "id", expression = "java(null)")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "location", source = "location")
    @Mapping(target = "publishedOn", expression = "java(null)")
    Event toEvent(NewEventDto newEventDto, User initiator, Category category,
                  Location location, LocalDateTime createdOn, EventState state);
    EventFullDto toEventFullDto(Event event, Long confirmedRequests, Long views);
    EventShortDto toEventShortDto(Event event, Long confirmedRequests, Long views);
}
