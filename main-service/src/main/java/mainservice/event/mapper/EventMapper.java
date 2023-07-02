package mainservice.event.mapper;

import mainservice.category.mapper.CategoryMapper;
import mainservice.category.model.Category;
import mainservice.event.dto.EventFullDto;
import mainservice.event.dto.EventShortDto;
import mainservice.event.dto.NewEventDto;
import mainservice.event.enums.EventState;
import mainservice.event.model.Event;
import mainservice.event.model.Location;
import mainservice.user.mapper.UserMapper;
import mainservice.user.model.User;
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
