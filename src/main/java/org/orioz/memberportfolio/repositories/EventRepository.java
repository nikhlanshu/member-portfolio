package org.orioz.memberportfolio.repositories;

import org.orioz.memberportfolio.models.Event;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

@Repository
public interface EventRepository extends ReactiveMongoRepository<Event, String> {
    Flux<Event> findByDatetimeAfterOrderByDatetimeAsc(LocalDateTime now);
}
