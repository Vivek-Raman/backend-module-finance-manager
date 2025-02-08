package dev.vivekraman.finance.manager.service.impl;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import dev.vivekraman.finance.manager.config.Constants;
import dev.vivekraman.finance.manager.entity.IngestLog;
import dev.vivekraman.finance.manager.model.IngestMetadata;
import dev.vivekraman.finance.manager.repository.IngestLogRepository;
import dev.vivekraman.finance.manager.service.api.IngestService;
import dev.vivekraman.finance.manager.service.api.UserService;
import dev.vivekraman.monolith.security.util.AuthUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestServiceImpl implements IngestService {
  private final UserService userService;
  private final IngestLogRepository ingestLogRepository;

  @Override
  public Mono<Boolean> ingestSplitwise(List<Map<String, String>> entries) {
    // omit last entry (total balances; not an expense)
    entries.removeLast();

    // TODO: push new records into finance_expense and notify
    // TODO: update ingest params

    return AuthUtils.fetchApiKey()
      .flatMap(apiKey -> Mono.zip(
        userService.fetchUser(apiKey),
        fetchIngestParams(apiKey),
        IngestMetadata::create))
      .map(ingestMetadata -> {
        ingestMetadata.setEntries(entries);
        filterEntries(ingestMetadata);
        return ingestMetadata;
      })
      .map(e -> true);
  }

  private Mono<Map<String, String>> fetchIngestParams(String apiKey) {
    return ingestLogRepository.findByApiKeyAndIdIn(apiKey,
      Constants.INGEST_LAST_SEEN_BALANCE,
      Constants.INGEST_LAST_PROCESSED_DATE)
      .collectList()
      .map(ingestLogs -> ingestLogs.stream()
        .collect(Collectors.toMap(IngestLog::getKey, IngestLog::getValue)));
  }

  private void filterEntries(IngestMetadata ingestMetadata) {
    float lastSeenBalance = Float.parseFloat(
      ingestMetadata.getIngestParams().get(Constants.INGEST_LAST_SEEN_BALANCE));
    LocalDate lastProcessedDate = LocalDate.parse(
      ingestMetadata.getIngestParams().get(Constants.INGEST_LAST_PROCESSED_DATE));

    // TODO: discard unchanged stats
    // TODO: implement hash/checksum for records until last processed date
    // calculate balance until last processed date

    String username = ingestMetadata.getUser().getFullName();

    List<Map<String, String>> oldRows = new LinkedList<>();
    List<Map<String, String>> filteredRows = new LinkedList<>();
    double balance = 0d;
    for (int i = 0; i < ingestMetadata.getEntries().size(); ++i) {
      Map<String, String> row = ingestMetadata.getEntries().get(i);
      if (!LocalDate.parse(row.get("Date")).isAfter(lastProcessedDate)) {
        oldRows.add(row);
        balance += Float.parseFloat(row.get(username));
      } else {
        filteredRows.add(row);
      }
    }

    if (balance != lastSeenBalance) {
      // TODO: push updates on old records into finance_expense and notify
      log.warn("Balance mismatch!");
    }
  }
}
