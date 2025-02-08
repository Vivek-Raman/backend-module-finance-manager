package dev.vivekraman.finance.manager.service.impl;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import dev.vivekraman.finance.manager.config.Constants;
import dev.vivekraman.finance.manager.entity.Expense;
import dev.vivekraman.finance.manager.entity.IngestLog;
import dev.vivekraman.finance.manager.entity.User;
import dev.vivekraman.finance.manager.model.IngestMetadata;
import dev.vivekraman.finance.manager.repository.ExpenseRepository;
import dev.vivekraman.finance.manager.repository.IngestLogRepository;
import dev.vivekraman.finance.manager.service.api.IngestService;
import dev.vivekraman.finance.manager.service.api.UserService;
import dev.vivekraman.monolith.security.util.AuthUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestServiceImpl implements IngestService {
  private final UserService userService;
  private final ExpenseRepository expenseRepository;
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
      .flatMap(ingestMetadata -> filterEntries(ingestMetadata, entries))
      .flatMap(this::persistNewEntries)
      .map(e -> true);
  }

  private Mono<Map<String, IngestLog>> fetchIngestParams(String apiKey) {
    return ingestLogRepository.findByApiKeyAndIdIn(apiKey,
      Constants.INGEST_LAST_SEEN_BALANCE,
      Constants.INGEST_LAST_PROCESSED_DATE)
      .collectList()
      .map(ingestLogs -> ingestLogs.stream()
        .collect(Collectors.toMap(IngestLog::getKey, Function.identity())));
  }

  private Mono<IngestMetadata> filterEntries(IngestMetadata ingestMetadata, List<Map<String, String>> entries) {
    float lastSeenBalance = Float.parseFloat(
      ingestMetadata.getIngestParams().get(Constants.INGEST_LAST_SEEN_BALANCE).getValue());
    LocalDate lastProcessedDate = LocalDate.parse(
      ingestMetadata.getIngestParams().get(Constants.INGEST_LAST_PROCESSED_DATE).getValue());
    String username = ingestMetadata.getUser().getFullName();
    // TODO: handle empty params

    List<Map<String, String>> oldEntries = new LinkedList<>();
    List<Map<String, String>> newEntries = new LinkedList<>();
    double balance = 0d;
    for (int i = 0; i < entries.size(); ++i) {
      Map<String, String> row = entries.get(i);
      LocalDate date = LocalDate.parse(row.get("Date"));
      if (!date.isAfter(lastProcessedDate)) {
        oldEntries.add(row);
        balance += Float.parseFloat(row.get(username));
      } else if (date.isBefore(LocalDate.now())) {
        newEntries.add(row);
      }
    }


    if (balance != lastSeenBalance) {
      log.warn("Balance mismatch!");
      return reconcileUpdatedEntries(ingestMetadata, oldEntries);
    }

    ingestMetadata.setNewEntries(newEntries);
    return Mono.just(ingestMetadata);
  }

  private Mono<IngestMetadata> reconcileUpdatedEntries(IngestMetadata ingestMetadata, List<Map<String, String>> oldEntries) {
    // TODO: push updates on old records into finance_expense and notify
    // TODO: push backdated new records into finance_expense and notify
    return Mono.just(ingestMetadata);
  }

  private Mono<IngestMetadata> persistNewEntries(IngestMetadata ingestMetadata) {
    List<Expense> toPersist = ingestMetadata.getNewEntries().stream()
      .map(row -> parseRow(row, ingestMetadata.getUser()))
      .toList();
    // return expenseRepository.saveAll(toPersist).collectList() // TODO: persist
    return Flux.fromIterable(toPersist).collectList()
      .map(addedRecords -> {
        ingestMetadata.getResponse().setRecordsAdded(addedRecords.size());
        return ingestMetadata;
      });
  }

  private Expense parseRow(Map<String, String> row, User user) {
    return Expense.builder()
      .apiKey(user.getApiKey())
      .summary(row.get("Description"))
      .amount(Float.parseFloat(row.get(user.getFullName())))
      .date(LocalDate.parse(row.get("Date")).atTime(23, 59, 59))
      .build();
  }
}
