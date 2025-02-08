package dev.vivekraman.finance.manager.service.api;

import java.util.List;
import java.util.Map;

import org.springframework.http.codec.multipart.FilePart;

import dev.vivekraman.finance.manager.entity.Expense;
import dev.vivekraman.finance.manager.entity.User;
import reactor.core.publisher.Mono;

public interface ParserService {
  Mono<List<Map<String, String>>> parseCSV(FilePart file);
}
