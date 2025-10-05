package org.orioz.memberportfolio.service.news;

import lombok.extern.slf4j.Slf4j;
import org.orioz.memberportfolio.auth.entitlement.EntitlementValidator;
import org.orioz.memberportfolio.config.NewsConfig;
import org.orioz.memberportfolio.dtos.auth.AdminVoidEntitlementCheckRequest;
import org.orioz.memberportfolio.dtos.auth.MemberEntitlementCheckBySubjectRequest;
import org.orioz.memberportfolio.models.News;
import org.orioz.memberportfolio.repositories.NewsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
public class NewsService {

    private final NewsRepository newsRepository;
    private final EntitlementValidator entitlementValidator;
    private final NewsConfig newsConfig;

    @Autowired
    public NewsService(NewsRepository newsRepository, EntitlementValidator entitlementValidator, NewsConfig newsConfig) {
        this.newsRepository = newsRepository;
        this.entitlementValidator = entitlementValidator;
        this.newsConfig = newsConfig;
    }

    public Mono<News> createNews(News news) {
        log.info("Creating new news item: {}", news.getTitle());
        news.setId(UUID.randomUUID().toString());
        return entitlementValidator.validate(new AdminVoidEntitlementCheckRequest())
                .then(newsRepository.save(news))
                .doOnSuccess(n -> log.info("News created with id: {}", n.getId()))
                .doOnError(err -> log.error("Error creating news: {}", err.getMessage()));
    }

    // Fetch only latest 10 news
    public Flux<News> getLatestNews() {
        log.info("Fetching latest 10 news items");
        return entitlementValidator.validate(new MemberEntitlementCheckBySubjectRequest())
                .thenMany(newsRepository.findAllByOrderByDateOfOccurrenceDesc(PageRequest.of(0, newsConfig.getRetrieveLimit())))
                .doOnError(err -> log.error("Error fetching latest news: {}", err.getMessage()));
    }

    public Mono<News> getNewsById(String id) {
        log.info("Fetching news by id: {}", id);
        return entitlementValidator.validate(new MemberEntitlementCheckBySubjectRequest())
                .then(newsRepository.findById(id))
                .doOnError(err -> log.error("Error fetching news by id {}: {}", id, err.getMessage()));
    }

    public Mono<News> updateNews(String id, News updatedNews) {
        log.info("Updating news with id: {}", id);
        return entitlementValidator.validate(new AdminVoidEntitlementCheckRequest())
                .then(newsRepository.findById(id))
                .flatMap(existingNews -> {
                    existingNews.setTitle(updatedNews.getTitle());
                    existingNews.setContent(updatedNews.getContent());
                    existingNews.setDateOfOccurrence(updatedNews.getDateOfOccurrence());
                    return newsRepository.save(existingNews)
                            .doOnSuccess(n -> log.info("News updated: {}", n.getId()))
                            .doOnError(err -> log.error("Error updating news {}: {}", id, err.getMessage()));
                });
    }

    public Mono<Void> deleteNews(String id) {
        log.info("Deleting news with id: {}", id);
        return entitlementValidator.validate(new AdminVoidEntitlementCheckRequest())
                .then(newsRepository.deleteById(id))
                .doOnSuccess(v -> log.info("News deleted: {}", id))
                .doOnError(err -> log.error("Error deleting news {}: {}", id, err.getMessage()));
    }
}
