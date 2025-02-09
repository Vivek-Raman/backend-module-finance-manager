package dev.vivekraman.finance.manager.service.api;

import java.util.List;
import java.util.Map;

import dev.vivekraman.finance.manager.model.IngestMetadata;
import reactor.core.publisher.Mono;

public interface ReconcileService {
  Mono<IngestMetadata> performReconcile(
    IngestMetadata metadata, List<Map<String, String>> oldRecords);
}
