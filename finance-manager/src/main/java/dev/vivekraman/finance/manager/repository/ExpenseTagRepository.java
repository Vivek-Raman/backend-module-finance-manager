package dev.vivekraman.finance.manager.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import dev.vivekraman.finance.manager.entity.ExpenseTag;
import reactor.core.publisher.Flux;

@Repository
public interface ExpenseTagRepository extends ReactiveCrudRepository<ExpenseTag, String> {
  Flux<ExpenseTag> findByApiKeyAndTag(String apiKey, String tag);
}
