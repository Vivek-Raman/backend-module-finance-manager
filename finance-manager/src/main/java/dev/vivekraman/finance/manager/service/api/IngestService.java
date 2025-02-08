package dev.vivekraman.finance.manager.service.api;

import java.util.List;
import java.util.Map;

import reactor.core.publisher.Mono;

public interface IngestService {
  Mono<Boolean> ingestSplitwise(List<Map<String, String>> entries);
}
