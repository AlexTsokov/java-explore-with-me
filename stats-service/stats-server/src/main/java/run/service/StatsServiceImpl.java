package run.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import model.EndpointHit;
import model.ViewStats;
import run.mapper.StatsMapper;
import run.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;
    private final StatsMapper statsMapper;

    @Override
    public void createHit(EndpointHit endpointHit) {
        statsRepository.save(statsMapper.toStats(endpointHit, endpointHit.getTimestamp()));
        log.info("StatsService: {}", endpointHit);
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        log.info("StatsService: start={}, end={}, uris={}, unique={}", start, end, uris, unique);
        if (start == null || end == null || start.isAfter(end)) {
            throw new IllegalArgumentException("Неверный формат даты");
        }
        if (uris == null || uris.isEmpty()) {
            return unique ?
                    statsRepository.getStatsForUniqueIp(start, end) :
                    statsRepository.getAllStats(start, end);
        } else {
            return unique ?
                    statsRepository.getStatsByUrisForUniqueIp(start, end, uris) :
                    statsRepository.getStatsByUris(start, end, uris);
        }
    }
}