package run.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import model.EndpointHit;
import model.ViewStats;
import run.mapper.StatsMapper;
import run.repository.StatsRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;
    private final StatsMapper statsMapper;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void createHit(EndpointHit endpointHit) {
        LocalDateTime timeStamp = LocalDateTime.parse(endpointHit.getTimestamp(), dateTimeFormatter);
        statsRepository.save(statsMapper.toStats(endpointHit, timeStamp));
        log.info("StatsService: {}", endpointHit);
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        log.info("StatsService: start={}, end={}, uris={}, unique={}", start, end, uris, unique);
        if (uris == null || uris.isEmpty()) {
            return unique ?
                    statsRepository.getStatsForUniqueIp(start, end) :
                    statsRepository.getAllStats(start, end);
        } else {
            return unique ?
                    statsRepository.getStatsWithUrisForUniqueIp(start, end, uris) :
                    statsRepository.getStatsWithUris(start, end, uris);
        }
    }
}