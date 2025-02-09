package dev.vivekraman.finance.manager.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import dev.vivekraman.finance.manager.entity.IngestParameter;
import reactor.core.publisher.Mono;

@Repository
public interface IngestParameterRepository extends ReactiveCrudRepository<IngestParameter, String> {
  Mono<IngestParameter> findByApiKeyAndGroupName(String apiKey, String groupName);
}
