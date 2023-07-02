package mainservice.event.service;

import mainservice.event.dto.*;
import mainservice.event.enums.EventSortType;
import mainservice.event.model.Event;
import mainservice.event.enums.EventState;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    List<EventFullDto> getEventsDtoByAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                           LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    List<EventShortDto> getAllEventsByPrivate(Long userId, Pageable pageable);

    EventFullDto createEventByPrivate(Long userId, NewEventDto newEventDto);

    EventFullDto getEventByPrivate(Long userId, Long eventId);

    EventFullDto updateEventByPrivate(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    List<EventShortDto> getEventsByPublic(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                          LocalDateTime rangeEnd, Boolean onlyAvailable, EventSortType sort,
                                          Integer from, Integer size, HttpServletRequest request);

    EventFullDto getEventByPublic(Long id, HttpServletRequest request);

    Event getEventById(Long eventId);

    List<Event> getEventsByIds(List<Long> eventsId);

    List<EventShortDto> toEventsShortDto(List<Event> events);

}
