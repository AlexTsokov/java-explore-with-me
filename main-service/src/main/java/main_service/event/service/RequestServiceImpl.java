package main_service.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main_service.event.dto.EventRequestStatusUpdateRequest;
import main_service.event.dto.EventRequestStatusUpdateResult;
import main_service.event.dto.ParticipationRequestDto;
import main_service.event.enums.EventState;
import main_service.event.enums.RequestStatus;
import main_service.event.enums.RequestStatusAction;
import main_service.event.mapper.RequestMapper;
import main_service.event.model.Event;
import main_service.event.model.Request;
import main_service.event.repository.RequestRepository;
import main_service.exception.DataException;
import main_service.exception.NotFoundException;
import main_service.user.model.User;
import main_service.user.service.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {

    private final UserService userService;
    private final EventService eventService;
    private final StatsService statsService;
    private final RequestRepository requestRepository;
    private final RequestMapper mapper;

    @Override
    public List<ParticipationRequestDto> getEventRequestsByRequester(Long userId) {
        log.info("MainService: userId={}", userId);
        userService.checkUserInBase(userId);
        return toParticipationRequestsDto(requestRepository.findAllByRequesterId(userId));
    }

    @Override
    public ParticipationRequestDto createEventRequest(Long userId, Long eventId) {
        log.info("MainService: userId={}, eventId={}", userId, eventId);
        User user = userService.getUserById(userId);
        Event event = eventService.getEventById(eventId);
        if (Objects.equals(event.getInitiator().getId(), userId)) {
            throw new DataException("Регистрация на свое событие невозможна.");
        }
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new DataException("Регистрация на неопубликованное событие невозможна.");
        }
        Optional<Request> oldRequest = requestRepository.findByEventIdAndRequesterId(eventId, userId);
        if (oldRequest.isPresent()) {
            throw new DataException("Вы уже зарегистрированны.");
        }
        checkIsNewLimitGreaterOld(
                statsService.getConfirmedRequests(List.of(event)).getOrDefault(eventId, 0L) + 1,
                event.getParticipantLimit()
        );
        Request newRequest = Request.builder()
                .event(event)
                .requester(user)
                .created(LocalDateTime.now())
                .build();
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            newRequest.setStatus(RequestStatus.CONFIRMED);
        } else {
            newRequest.setStatus(RequestStatus.PENDING);
        }
        return mapper.toParticipationRequestDto(requestRepository.save(newRequest));
    }

    @Override
    public ParticipationRequestDto cancelEventRequest(Long userId, Long requestId) {
        log.info("MainService: userId={}, requestId={}", userId, requestId);
        userService.checkUserInBase(userId);
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с ID " + requestId + " не существует"));

        checkUserIsOwner(request.getRequester().getId(), userId);
        request.setStatus(RequestStatus.CANCELED);
        return mapper.toParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> getEventRequestsByEventOwner(Long userId, Long eventId) {
        log.info("MainService - getEventRequestsByEventOwner: userId={}, eventId={}", userId, eventId);
        userService.checkUserInBase(userId);
        Event event = eventService.getEventById(eventId);
        checkUserIsOwner(event.getInitiator().getId(), userId);
        return toParticipationRequestsDto(requestRepository.findAllByEventId(eventId));
    }

    @Override
    public EventRequestStatusUpdateResult updateEventRequestsByEventOwner(
            Long userId, Long eventId, EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        log.info("MainService: userId={}, eventId={}, eventRequestStatusUpdateRequest={}",
                userId, eventId, eventRequestStatusUpdateRequest);
        userService.checkUserInBase(userId);
        Event event = eventService.getEventById(eventId);
        checkUserIsOwner(event.getInitiator().getId(), userId);
        if (!event.getRequestModeration() ||
                event.getParticipantLimit() == 0 ||
                eventRequestStatusUpdateRequest.getRequestIds().isEmpty()) {
            return new EventRequestStatusUpdateResult(List.of(), List.of());
        }
        List<Request> confirmedList = new ArrayList<>();
        List<Request> rejectedList = new ArrayList<>();
        List<Request> requests = requestRepository.findAllByIdIn(eventRequestStatusUpdateRequest.getRequestIds());

        if (requests.size() != eventRequestStatusUpdateRequest.getRequestIds().size()) {
            throw new NotFoundException("Some requests doesn't find");
        }
        if (!requests.stream()
                .map(Request::getStatus)
                .allMatch(RequestStatus.PENDING::equals)) {
            throw new DataException("Нелья изменить заброс, если стстус не PENDING");
        }
        if (eventRequestStatusUpdateRequest.getStatus().equals(RequestStatusAction.REJECTED)) {
            rejectedList.addAll(changeStatusAndSave(requests, RequestStatus.REJECTED));
        } else {
            Long newConfirmedRequests = statsService.getConfirmedRequests(List.of(event)).getOrDefault(eventId, 0L) +
                    eventRequestStatusUpdateRequest.getRequestIds().size();

            checkIsNewLimitGreaterOld(newConfirmedRequests, event.getParticipantLimit());

            confirmedList.addAll(changeStatusAndSave(requests, RequestStatus.CONFIRMED));

            if (newConfirmedRequests >= event.getParticipantLimit()) {
                rejectedList.addAll(changeStatusAndSave(
                        requestRepository.findAllByEventIdAndStatus(eventId, RequestStatus.PENDING),
                        RequestStatus.REJECTED)
                );
            }
        }
        return new EventRequestStatusUpdateResult(toParticipationRequestsDto(confirmedList),
                toParticipationRequestsDto(rejectedList));
    }

    private List<ParticipationRequestDto> toParticipationRequestsDto(List<Request> requests) {
        return requests.stream()
                .map(mapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    private List<Request> changeStatusAndSave(List<Request> requests, RequestStatus status) {
        requests.forEach(request -> request.setStatus(status));
        return requestRepository.saveAll(requests);
    }

    private void checkIsNewLimitGreaterOld(Long newLimit, Integer eventParticipantLimit) {
        if (eventParticipantLimit != 0 && (newLimit > eventParticipantLimit)) {
            throw new DataException("Достигнут лимит: " + eventParticipantLimit);
        }
    }

    private void checkUserIsOwner(Long id, Long userId) {
        if (!Objects.equals(id, userId)) {
            throw new DataException("Пользователь ID " + userId + " не является создателем события.");
        }
    }
}
