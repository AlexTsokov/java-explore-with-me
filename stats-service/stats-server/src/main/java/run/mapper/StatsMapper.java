package run.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import model.EndpointHit;
import run.model.Stats;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface StatsMapper {
    @Mapping(target = "timestamp", expression = "java(timestamp)")
    Stats toStats(EndpointHit endpointHit, LocalDateTime timestamp);
}
