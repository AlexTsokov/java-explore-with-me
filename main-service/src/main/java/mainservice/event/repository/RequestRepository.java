package mainservice.event.repository;

import mainservice.event.dto.RequestStats;
import mainservice.event.enums.RequestStatus;
import mainservice.event.model.Request;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findAllByRequesterId(Long requesterId);

    Optional<Request> findByEventIdAndRequesterId(Long eventId, Long userId);

    List<Request> findAllByEventIdAndStatus(Long eventId, RequestStatus status);

    List<Request> findAllByEventId(Long eventId);

    List<Request> findAllByIdIn(List<Long> requestIds);

    @Query("SELECT new mainservice.event.dto.RequestStats(r.event.id, count(r.id)) " +
            "FROM Request r " +
            "WHERE r.event.id IN :eventsId " +
            "AND r.status = 'CONFIRMED' " +
            "GROUP BY r.event.id")
    List<RequestStats> getConfirmedRequests(@Param("eventsId") List<Long> eventsId);
}
