package org.orioz.memberportfolio.repositories;

import org.orioz.memberportfolio.models.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// ReactiveMongoRepository provides reactive CRUD operations for Member documents
public interface MemberRepository extends ReactiveMongoRepository<Member, String> {

    @Query("{ 'email': { $regex: '^?0$', $options: 'i' } }")
    Mono<Member> findByEmail(String email);

    // Find members whose roles list contains a specific role (e.g., "ADMIN")
    Flux<Member> findByRolesContaining(Member.Role role);

    /**
     * Finds a Flux of Members by their status, applying pagination (skip/limit) and sorting.
     * Note: This only returns the items for the current page, not the total count.
     */
    Flux<Member> findByStatus(Member.Status status, Pageable pageable);

    /**
     * Counts the total number of Members with a specific status.
     * This is needed to build the totalElements field for pagination.
     */
    Mono<Long> countByStatus(Member.Status status);

    @Query("{ 'lastName': { $regex: ?0, $options: 'i' } }")
    Flux<Member> findByLastName(String lastName);
    @Query("{ 'firstName': { $regex: ?0, $options: 'i' }, 'lastName': { $regex: ?1, $options: 'i' } }")
    Flux<Member> findByFirstNameAndLastName(String firstName, String lastName);
}