package run.service;

import model.ViewStats;
import model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {

    void createHit(EndpointHit endpointHit);

    List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);

}
