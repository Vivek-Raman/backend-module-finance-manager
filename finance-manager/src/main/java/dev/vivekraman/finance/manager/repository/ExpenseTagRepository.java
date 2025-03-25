package dev.vivekraman.finance.manager.repository;

import java.util.Set;
import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import dev.vivekraman.finance.manager.entity.ExpenseTag;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ExpenseTagRepository extends ReactiveCrudRepository<ExpenseTag, UUID> {
  Flux<ExpenseTag> findByApiKeyAndExpenseIdIn(String apiKey, Set<String> expenseIds);
  Flux<ExpenseTag> findByApiKeyAndTagIn(String apiKey, Set<String> tag);
  Mono<Void> deleteByApiKeyAndTagIn(String apiKey, Set<String> tag);

  Mono<Void> deleteByApiKeyAndId(String apiKey, UUID expenseId);
}
