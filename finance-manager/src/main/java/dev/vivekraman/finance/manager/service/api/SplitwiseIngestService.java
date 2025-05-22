package dev.vivekraman.finance.manager.service.api;

import java.util.List;
import java.util.Map;

import dev.vivekraman.finance.manager.model.IngestResponseDTO;
import reactor.core.publisher.Mono;

public interface SplitwiseIngestService {
  String parseGroupName(String filename);

  Mono<IngestResponseDTO> ingestSplitwise(
    String groupName, List<Map<String, String>> entries);
}
