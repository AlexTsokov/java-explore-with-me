package mainservice.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mainservice.category.model.Category;
import mainservice.category.repository.CategoryRepository;
import mainservice.event.dto.*;
import mainservice.event.enums.EventSortType;
import mainservice.event.enums.EventState;
import mainservice.event.mapper.EventMapper;
import mainservice.event.mapper.LocationMapper;
import mainservice.event.model.Event;
import mainservice.event.model.Location;
import mainservice.event.repository.EventRepository;
import mainservice.event.repository.LocationRepository;
import mainservice.exception.DataException;
import mainservice.exception.NotFoundException;
import mainservice.user.model.User;
import mainservice.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final StatsService statsService;
    private final LocationRepository locationRepository;
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final LocationMapper locationMapper;

    @Override
    public List<EventFullDto> getEventsDtoByAdmin(List<Long> users, List<EventState> states,
                                                  List<Long> categories, LocalDateTime rangeStart,
                                                  LocalDateTime rangeEnd, Integer from, Integer size) {
        log.info("MainService: users={}, states={}, categoriesId={}, rangeStart={}, " +
                "rangeEnd={}, from={}, size={}", users, states, categories, rangeStart, rangeEnd, from, size);
        checkStartIsBeforeEnd(rangeStart, rangeEnd);
        List<Event> events = eventRepository.getEventsByAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
        return toEventsFullDto(events);
    }

    @Override
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        log.info("MainService: eventId={}, updateEventAdminRequest={}",
                eventId, updateEventAdminRequest);
        checkNewEventDate(updateEventAdminRequest.getEventDate(), LocalDateTime.now().plusHours(1));
        Event event = getEventById(eventId);
        if (updateEventAdminRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventAdminRequest.getAnnotation());
        }
        if (updateEventAdminRequest.getDescription() != null) {
            event.setDescription(updateEventAdminRequest.getDescription());
        }
        if (updateEventAdminRequest.getCategory() != null) {
            Long catId = updateEventAdminRequest.getCategory();
            event.setCategory(categoryRepository.findById(catId)
                    .orElseThrow(() -> new NotFoundException("Категория с ID " + catId + " не найдена")));
        }
        if (updateEventAdminRequest.getEventDate() != null) {
            event.setEventDate(updateEventAdminRequest.getEventDate());
        }
        if (updateEventAdminRequest.getPaid() != null) {
            event.setPaid(updateEventAdminRequest.getPaid());
        }
        if (updateEventAdminRequest.getLocation() != null) {
            event.setLocation(getOrSaveLocation(updateEventAdminRequest.getLocation()));
        }
        if (updateEventAdminRequest.getParticipantLimit() != null) {
            checkIsNewLimitNotLessOld(
                    updateEventAdminRequest.getParticipantLimit(),
                    statsService.getConfirmedRequests(List.of(event)).getOrDefault(eventId, 0L));
            event.setParticipantLimit(updateEventAdminRequest.getParticipantLimit());
        }
        if (updateEventAdminRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventAdminRequest.getRequestModeration());
        }
        if (updateEventAdminRequest.getStateAction() != null) {
            if (!event.getState().equals(EventState.PENDING)) {
                throw new DataIntegrityViolationException("Статус должен быть PENDING");
            }
            switch (updateEventAdminRequest.getStateAction()) {
                case PUBLISH_EVENT:
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                case REJECT_EVENT:
                    event.setState(EventState.REJECTED);
                    break;
            }
        }
        if (updateEventAdminRequest.getTitle() != null) {
            event.setTitle(updateEventAdminRequest.getTitle());
        }
        return toEventFullDto(eventRepository.save(event));
    }

    @Override
    public List<EventShortDto> getAllEventsByPrivate(Long userId, Pageable pageable) {
        log.info("MainService: userId={}, pageable={}", userId, pageable);
        checkUserInBase(userId);
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);
        return toEventsShortDto(events);
    }

    @Override
    public EventFullDto createEventByPrivate(Long userId, NewEventDto newEventDto) {
        log.info("MainService: userId={}, newEventDto={}", userId, newEventDto);
        checkNewEventDate(newEventDto.getEventDate(), LocalDateTime.now().plusHours(2));
        User eventUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
        Long catId = newEventDto.getCategory();
        Category eventCategory = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с ID " + catId + " не найдена"));
        Location eventLocation = getOrSaveLocation(newEventDto.getLocation());
        Event newEvent = eventMapper.toEvent(newEventDto, eventUser, eventCategory, eventLocation, LocalDateTime.now(),
                EventState.PENDING);
        return toEventFullDto(eventRepository.save(newEvent));
    }

    @Override
    public EventFullDto getEventByPrivate(Long userId, Long eventId) {
        log.info("MainService: userId={}, eventId={}", userId, eventId);
        checkUserInBase(userId);
        Event event = getEventByIdAndInitiatorId(eventId, userId);
        return toEventFullDto(eventRepository.save(event));
    }

    @Override
    public EventFullDto updateEventByPrivate(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        log.info("MainService: userId={}, eventId={}, {}", userId, eventId, updateEventUserRequest);
        checkNewEventDate(updateEventUserRequest.getEventDate(), LocalDateTime.now().plusHours(2));
        checkUserInBase(userId);
        Event event = getEventByIdAndInitiatorId(eventId, userId);
        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new DataIntegrityViolationException("Невозможно изменить событие со статусом PUBLISHED");
        }
        if (updateEventUserRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventUserRequest.getAnnotation());
        }
        if (updateEventUserRequest.getCategory() != null) {
            Long catId = updateEventUserRequest.getCategory();
            event.setCategory(categoryRepository.findById(catId)
                    .orElseThrow(() -> new NotFoundException("Категория с ID " + catId + " не найдена")));
        }
        if (updateEventUserRequest.getDescription() != null) {
            event.setDescription(updateEventUserRequest.getDescription());
        }
        if (updateEventUserRequest.getEventDate() != null) {
            event.setEventDate(updateEventUserRequest.getEventDate());
        }
        if (updateEventUserRequest.getLocation() != null) {
            event.setLocation(getOrSaveLocation(updateEventUserRequest.getLocation()));
        }
        if (updateEventUserRequest.getPaid() != null) {
            event.setPaid(updateEventUserRequest.getPaid());
        }
        if (updateEventUserRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventUserRequest.getParticipantLimit());
        }
        if (updateEventUserRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventUserRequest.getRequestModeration());
        }
        if (updateEventUserRequest.getStateAction() != null) {
            switch (updateEventUserRequest.getStateAction()) {
                case SEND_TO_REVIEW:
                    event.setState(EventState.PENDING);
                    break;
                case CANCEL_REVIEW:
                    event.setState(EventState.CANCELED);
                    break;
            }
        }
        if (updateEventUserRequest.getTitle() != null) {
            event.setTitle(updateEventUserRequest.getTitle());
        }
        return toEventFullDto(eventRepository.save(event));
    }

    @Override
    public List<EventShortDto> getEventsByPublic(
            String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd,
            Boolean onlyAvailable, EventSortType sort, Integer from, Integer size, HttpServletRequest request) {
        log.info("MainService: text={}, categoriesId={}, paid={}, " +
                        "rangeStart={}, rangeEnd={}, onlyAvailable={}, sort={}, from={}, size={}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
        checkStartIsBeforeEnd(rangeStart, rangeEnd);
        List<Event> events = eventRepository.getEventsByPublic(text, categories, paid, rangeStart, rangeEnd, from, size);

        if (events.isEmpty()) {
            return List.of();
        }
        Map<Long, Integer> eventsParticipantLimit = events.stream()
                .collect(Collectors.toMap(Event::getId, Event::getParticipantLimit));
        List<EventShortDto> eventsShortDto = toEventsShortDto(events);
        if (onlyAvailable) {
            eventsShortDto = eventsShortDto.stream()
                    .filter(eventShort -> (eventsParticipantLimit.get(eventShort.getId()) == 0 ||
                            eventsParticipantLimit.get(eventShort.getId()) > eventShort.getConfirmedRequests()))
                    .collect(Collectors.toList());
        }
        if (needSort(sort, EventSortType.VIEWS)) {
            eventsShortDto.sort(Comparator.comparing(EventShortDto::getViews));
        } else if (needSort(sort, EventSortType.EVENT_DATE)) {
            eventsShortDto.sort(Comparator.comparing(EventShortDto::getEventDate));
        }
        statsService.addHit(request);
        return eventsShortDto;
    }

    @Override
    public EventFullDto getEventByPublic(Long eventId, HttpServletRequest request) {
        log.info("MainService: eventId={}", eventId);
        Event event = getEventById(eventId);
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("Событие с ID " + eventId + " не найдено");
        }
        statsService.addHit(request);
        return toEventFullDto(event);
    }

    @Override
    public Event getEventById(Long eventId) {
        log.info("MainService: eventId={}", eventId);
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено"));
    }

    @Override
    public List<Event> getEventsByIds(List<Long> eventsId) {
        log.info("MainService: eventsId={}", eventsId);
        if (eventsId.isEmpty()) {
            return new ArrayList<>();
        }
        return eventRepository.findAllByIdIn(eventsId);
    }

    @Override
    public List<EventShortDto> toEventsShortDto(List<Event> events) {
        Map<Long, Long> views = statsService.getViews(events);
        Map<Long, Long> confirmedRequests = statsService.getConfirmedRequests(events);
        return events.stream()
                .map((event) -> eventMapper.toEventShortDto(
                        event,
                        confirmedRequests.getOrDefault(event.getId(), 0L),
                        views.getOrDefault(event.getId(), 0L)))
                .collect(Collectors.toList());
    }

    private List<EventFullDto> toEventsFullDto(List<Event> events) {
        Map<Long, Long> views = statsService.getViews(events);
        Map<Long, Long> confirmedRequests = statsService.getConfirmedRequests(events);
        return events.stream()
                .map((event) -> eventMapper.toEventFullDto(
                        event,
                        confirmedRequests.getOrDefault(event.getId(), 0L),
                        views.getOrDefault(event.getId(), 0L)))
                .collect(Collectors.toList());
    }

    private EventFullDto toEventFullDto(Event event) {
        return toEventsFullDto(List.of(event)).get(0);
    }

    private Event getEventByIdAndInitiatorId(Long eventId, Long userId) {
        log.info("MainService: eventsId={}", eventId);
        return eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не существует"));
    }

    private Location getOrSaveLocation(LocationDto locationDto) {
        Location newLocation = locationMapper.toLocation(locationDto);
        return locationRepository.findByLatAndLon(newLocation.getLat(), newLocation.getLon())
                .orElseGet(() -> locationRepository.save(newLocation));
    }

    private Boolean needSort(EventSortType sort, EventSortType typeToCompare) {
        return sort != null && sort.equals(typeToCompare);
    }

    private void checkStartIsBeforeEnd(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new DataException("Дата начала не может быть позже даты окончания события");
        }
    }

    private void checkNewEventDate(LocalDateTime newEventDate, LocalDateTime minTimeBeforeEventStart) {
        if (newEventDate != null && newEventDate.isBefore(minTimeBeforeEventStart)) {
            throw new DataException("Слишком мало времени до начала события");
        }
    }

    private void checkIsNewLimitNotLessOld(Integer newLimit, Long eventParticipantLimit) {
        if (newLimit != 0 && eventParticipantLimit != 0 && (newLimit < eventParticipantLimit)) {
            throw new DataException("Лимит не может быть меньше количества участников: " + eventParticipantLimit);
        }
    }

    public void checkUserInBase(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("Пользователь с ID " + id + " не найден");
        }
    }

    @Override
    public void checkEventInBase(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Событие с ID " + eventId + " не существует");
        }
    }

}
