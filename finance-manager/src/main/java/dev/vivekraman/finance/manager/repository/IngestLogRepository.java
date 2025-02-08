package dev.vivekraman.finance.manager.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import dev.vivekraman.finance.manager.entity.IngestLog;
import reactor.core.publisher.Flux;

@Repository
public interface IngestLogRepository extends ReactiveCrudRepository<IngestLog, String> {
  Flux<IngestLog> findByApiKeyAndIdIn(String apiKey, String... keys);
}
