package mainservice.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import mainservice.compilation.dto.CompilationDto;
import mainservice.compilation.dto.NewCompilationDto;
import mainservice.compilation.dto.UpdateCompilationRequest;
import mainservice.compilation.mapper.CompilationMapper;
import mainservice.compilation.model.Compilation;
import mainservice.compilation.repository.CompilationRepository;
import mainservice.event.dto.EventShortDto;
import mainservice.event.model.Event;
import mainservice.event.service.EventService;
import mainservice.exception.NotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationServiceImpl implements CompilationService {

    private final EventService eventService;
    private final CompilationRepository compilationRepository;
    private final CompilationMapper mapper;

    @Override
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        log.info("MainService: newCompilationDto={}", newCompilationDto);
        List<Event> events = new ArrayList<>();
        if (!newCompilationDto.getEvents().isEmpty()) {
            events = eventService.getEventsByIds(newCompilationDto.getEvents());
            checkEventsListsSize(events, newCompilationDto.getEvents());
        }
        Compilation compilation = compilationRepository.save(mapper.newDtoToCompilation(newCompilationDto, events));
        return getCompilationDtoById(compilation.getId());
    }

    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest) {
        log.info("MainService: compId={}, updateCompilationRequest={}",
                compId, updateCompilationRequest);
        Compilation compilation = getCompilationById(compId);
        if (updateCompilationRequest.getTitle() != null) {
            compilation.setTitle(updateCompilationRequest.getTitle());
        }
        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }
        if (updateCompilationRequest.getEvents() != null) {
            List<Event> events = eventService.getEventsByIds(updateCompilationRequest.getEvents());
            checkEventsListsSize(events, updateCompilationRequest.getEvents());
            compilation.setEvents(events);
        }
        compilationRepository.save(compilation);
        return getCompilationDtoById(compId);
    }

    @Override
    public void deleteCompilationById(Long compId) {
        log.info("MainService: compId={}", compId);
        getCompilationById(compId);
        compilationRepository.deleteById(compId);
    }

    @Override
    public List<CompilationDto> getAllCompilationDto(Boolean pinned, Pageable pageable) {
        log.info("MainService: pinned={}, pageable={}", pinned, pageable);
        List<Compilation> compilations;
        if (pinned == null) {
            compilations = compilationRepository.findAll(pageable).toList();
        } else {
            compilations = compilationRepository.findAllByPinned(pinned, pageable);
        }
        Set<Event> uniqueEvents = new HashSet<>();
        compilations.forEach(compilation -> uniqueEvents.addAll(compilation.getEvents()));

        Map<Long, EventShortDto> eventsShortDto = new HashMap<>();
        eventService.toEventsShortDto(new ArrayList<>(uniqueEvents))
                .forEach(event -> eventsShortDto.put(event.getId(), event));
        List<CompilationDto> result = new ArrayList<>();
        compilations.forEach(compilation -> {
            List<EventShortDto> compEventsShortDto = new ArrayList<>();
            compilation.getEvents()
                    .forEach(event -> compEventsShortDto.add(eventsShortDto.get(event.getId())));
            result.add(mapper.toCompilationDto(compilation, compEventsShortDto));
        });
        return result;
    }

    @Override
    public CompilationDto getCompilationDtoById(Long compId) {
        log.info("MainService: compId={}", compId);
        Compilation compilation = getCompilationById(compId);
        List<EventShortDto> eventsShortDto = eventService.toEventsShortDto(compilation.getEvents());
        return mapper.toCompilationDto(compilation, eventsShortDto);
    }

    private Compilation getCompilationById(Long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с ID " + compId + " не найдена"));
    }

    private void checkEventsListsSize(List<Event> events, List<Long> eventsIdToUpdate) {
        if (events.size() != eventsIdToUpdate.size()) {
            throw new NotFoundException("Событие для подборки не найдено");
        }
    }

}
