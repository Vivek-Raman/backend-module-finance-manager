package dev.vivekraman.finance.manager.repository;

import java.util.Date;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import dev.vivekraman.finance.manager.entity.Expense;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ExpenseRepository extends ReactiveCrudRepository<Expense, String> {
  Flux<Expense> findByApiKeyAndDateBetween(String apiKey, Date left, Date right);
  Flux<Expense> findByApiKeyOrderByDateDesc(String apiKey, Pageable pageRequest);
  Mono<Long> countByApiKey(String apiKey);
  Flux<Expense> findByApiKeyAndIdIn(String apiKey, Set<String> ids);
}
