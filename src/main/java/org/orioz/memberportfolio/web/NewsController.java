package org.orioz.memberportfolio.web;

import org.orioz.memberportfolio.models.News;
import org.orioz.memberportfolio.service.news.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/v1/api/news", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class NewsController {

    private final NewsService newsService;

    @Autowired
    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @PostMapping
    public Mono<ResponseEntity<News>> createNews(@RequestBody News news) {
        return newsService.createNews(news)
                .map(savedNews -> ResponseEntity.status(HttpStatus.CREATED).body(savedNews));
    }

    // Fetch latest 10 news items
    @GetMapping("/latest")
    public Flux<News> getLatestNews() {
        return newsService.getLatestNews();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<News>> getNewsById(@PathVariable String id) {
        return newsService.getNewsById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<News>> updateNews(@PathVariable String id, @RequestBody News news) {
        return newsService.updateNews(id, news)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteNews(@PathVariable String id) {
        return newsService.deleteNews(id)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }
}
