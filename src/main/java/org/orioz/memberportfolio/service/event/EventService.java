package org.orioz.memberportfolio.service.event;

import lombok.extern.slf4j.Slf4j;
import org.orioz.memberportfolio.models.Event;
import org.orioz.memberportfolio.repositories.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Service
public class EventService {

    private final EventRepository eventRepository;

    @Autowired
    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Mono<Event> createEvent(Event event) {
        log.info("Creating new event: {}", event.getTitle());
        return eventRepository.save(event)
                .doOnSuccess(e -> log.info("Event created with id: {}", e.getId()))
                .doOnError(err -> log.error("Error creating event: {}", err.getMessage()));
    }

    public Flux<Event> getUpcomingEvents() {
        log.info("Fetching upcoming events");
        return eventRepository.findByDatetimeAfterOrderByDatetimeAsc(LocalDateTime.now())
                .doOnError(err -> log.error("Error fetching upcoming events: {}", err.getMessage()));
    }

    public Mono<Event> getEventById(String id) {
        log.info("Fetching event by id: {}", id);
        return eventRepository.findById(id)
                .doOnError(err -> log.error("Error fetching event by id {}: {}", id, err.getMessage()));
    }

    public Mono<Event> updateEvent(String id, Event updatedEvent) {
        log.info("Updating event with id: {}", id);
        return eventRepository.findById(id)
                .flatMap(existingEvent -> {
                    existingEvent.setTitle(updatedEvent.getTitle());
                    existingEvent.setDescription(updatedEvent.getDescription());
                    existingEvent.setDatetime(updatedEvent.getDatetime());
                    existingEvent.setTimezone(updatedEvent.getTimezone());
                    existingEvent.setPlace(updatedEvent.getPlace());
                    return eventRepository.save(existingEvent)
                            .doOnSuccess(e -> log.info("Event updated: {}", e.getId()))
                            .doOnError(err -> log.error("Error updating event {}: {}", id, err.getMessage()));
                });
    }

    public Mono<Void> deleteEvent(String id) {
        log.info("Deleting event with id: {}", id);
        return eventRepository.deleteById(id)
                .doOnSuccess(v -> log.info("Event deleted: {}", id))
                .doOnError(err -> log.error("Error deleting event {}: {}", id, err.getMessage()));
    }
}
