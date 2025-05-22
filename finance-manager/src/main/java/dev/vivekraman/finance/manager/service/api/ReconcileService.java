package dev.vivekraman.finance.manager.service.api;

import java.util.List;
import java.util.Map;

import dev.vivekraman.finance.manager.model.IngestSplitwiseMetadata;
import reactor.core.publisher.Mono;

public interface ReconcileService {
  Mono<IngestSplitwiseMetadata> performReconcile(
    IngestSplitwiseMetadata metadata, List<Map<String, String>> oldRecords);
}
