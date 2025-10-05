package org.orioz.memberportfolio.repositories;

import org.orioz.memberportfolio.models.News;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface NewsRepository extends ReactiveMongoRepository<News, String> {

    // Use Pageable to fetch top N items
    Flux<News> findAllByOrderByDateOfOccurrenceDesc(Pageable pageable);
}
