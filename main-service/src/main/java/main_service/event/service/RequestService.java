package main_service.event.service;

import main_service.event.dto.EventRequestStatusUpdateRequest;
import main_service.event.dto.EventRequestStatusUpdateResult;
import main_service.event.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {
    List<ParticipationRequestDto> getEventRequestsByRequester(Long userId);

    ParticipationRequestDto createEventRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelEventRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> getEventRequestsByEventOwner(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateEventRequestsByEventOwner(
            Long userId, Long eventId, EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest);

}
