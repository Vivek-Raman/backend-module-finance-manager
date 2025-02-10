package dev.vivekraman.finance.manager.repository;

import java.util.Set;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import dev.vivekraman.finance.manager.entity.ExpenseTag;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ExpenseTagRepository extends ReactiveCrudRepository<ExpenseTag, String> {
  Flux<ExpenseTag> findByApiKeyAndTagIn(String apiKey, Set<String> tag);
  Mono<Void> deleteByApiKeyAndTagIn(String apiKey, Set<String> tag);
}
