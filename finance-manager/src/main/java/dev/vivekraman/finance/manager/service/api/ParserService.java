package dev.vivekraman.finance.manager.service.api;

import java.util.List;
import java.util.Map;

import org.springframework.http.codec.multipart.FilePart;

import reactor.core.publisher.Mono;

public interface ParserService {
  Mono<List<Map<String, String>>> parseCSV(FilePart file);
}
