package dev.vivekraman.finance.manager.service.api;

import java.util.List;
import java.util.Map;

import dev.vivekraman.finance.manager.model.IngestSplitwiseResponseDTO;
import reactor.core.publisher.Mono;

public interface IngestService {
  String parseGroupName(String filename);

  Mono<IngestSplitwiseResponseDTO> ingestSplitwise(
    String groupName, List<Map<String, String>> entries);
}
