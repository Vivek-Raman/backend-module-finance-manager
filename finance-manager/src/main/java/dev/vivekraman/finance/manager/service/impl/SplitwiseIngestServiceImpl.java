package dev.vivekraman.finance.manager.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import dev.vivekraman.finance.manager.entity.Expense;
import dev.vivekraman.finance.manager.entity.IngestParameter;
import dev.vivekraman.finance.manager.entity.User;
import dev.vivekraman.finance.manager.model.IngestSplitwiseMetadata;
import dev.vivekraman.finance.manager.model.IngestResponseDTO;
import dev.vivekraman.finance.manager.model.enums.ExpenseTags;
import dev.vivekraman.finance.manager.repository.ExpenseRepository;
import dev.vivekraman.finance.manager.repository.IngestParameterRepository;
import dev.vivekraman.finance.manager.service.api.ExpenseTagService;
import dev.vivekraman.finance.manager.service.api.SplitwiseIngestService;
import dev.vivekraman.finance.manager.service.api.ReconcileService;
import dev.vivekraman.finance.manager.service.api.UserService;
import dev.vivekraman.monolith.security.util.AuthUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class SplitwiseIngestServiceImpl implements SplitwiseIngestService {
  private final UserService userService;
  private final ReconcileService reconcileService;
  private final ExpenseTagService expenseTagService;
  private final ExpenseRepository expenseRepository;
  private final IngestParameterRepository ingestParameterRepository;

  private static final Pattern FILENAME_PATTERN = Pattern.compile(
    "(.*)_[\\d-]{10}_export.*\\.csv", Pattern.CASE_INSENSITIVE);

  @Override
  public String parseGroupName(String filename) {
    Matcher matcher = FILENAME_PATTERN.matcher(filename);
    matcher.find();
    return matcher.group(1);
  }

  @Override
  public Mono<IngestResponseDTO> ingestSplitwise(
    String groupName, List<Map<String, String>> entries) {
    return AuthUtils.fetchApiKey()
      .flatMap(apiKey -> Mono.zip(
        userService.fetchUser(apiKey),
        ingestParameterRepository.findByApiKeyAndGroupName(apiKey, groupName)
          .defaultIfEmpty(new IngestParameter()),
        IngestSplitwiseMetadata::create))
      .flatMap(metadata -> handleNullParameters(metadata, groupName))
      .flatMap(ingestMetadata -> filterEntries(ingestMetadata, entries))
      .flatMap(this::persistNewEntries)
      .flatMap(this::updateParameters)
      .map(IngestSplitwiseMetadata::getResponse)
      .defaultIfEmpty(new IngestResponseDTO());
  }

  private Mono<IngestSplitwiseMetadata> handleNullParameters(IngestSplitwiseMetadata ingestMetadata, String groupName) {
    if (StringUtils.isBlank(ingestMetadata.getParameters().getApiKey())) {
      return ingestParameterRepository.save(
        IngestParameter.builder()
          .apiKey(ingestMetadata.getUser().getApiKey())
          .groupName(groupName)
          .lastSeenBalance(0d)
          .lastProcessedDate(OffsetDateTime.MIN)
          .build())
        .map(params -> {
          ingestMetadata.setParameters(params);
          return ingestMetadata;
        });
    } else {
      return Mono.just(ingestMetadata);
    }
  }

  private Mono<IngestSplitwiseMetadata> filterEntries(IngestSplitwiseMetadata ingestMetadata, List<Map<String, String>> entries) {
    String username = ingestMetadata.getUser().getFullName();
    OffsetDateTime lastProcessedDate = ingestMetadata.getParameters().getLastProcessedDate();

    List<Map<String, String>> oldEntries = new LinkedList<>();
    List<Map<String, String>> newEntries = new LinkedList<>();
    double oldBalance = 0f;
    double newBalance = 0f;
    ingestMetadata.setNewestDateInEntries(
      LocalDate.parse(entries.get(0).get("Date"),DateTimeFormatter.ISO_LOCAL_DATE)
        .atStartOfDay()
        .atOffset(ZoneOffset.UTC));
    for (int i = 0; i < entries.size(); ++i) {
      Map<String, String> row = entries.get(i);
      // omit total balance entries
      if ("Total balance".equals(row.get("Description"))) {
        continue;
      }

      OffsetDateTime date =
        LocalDate.parse(row.get("Date"), DateTimeFormatter.ISO_LOCAL_DATE)
          .atStartOfDay()
          .atOffset(ZoneOffset.UTC);
      double amount = Double.parseDouble(row.get(username));
      if (!date.toInstant().isAfter(lastProcessedDate.toInstant())) {
        oldEntries.add(row);
        oldBalance += amount;
      } else if (date.toInstant().isBefore(Instant.now())) {
        newBalance += amount;
        newEntries.add(row);
        if (date.toInstant().isAfter(ingestMetadata.getNewestDateInEntries().toInstant())) {
          ingestMetadata.setNewestDateInEntries(date);
        }
      }
    }

    newBalance += oldBalance;
    ingestMetadata.setNewBalance(newBalance);
    ingestMetadata.setNewEntries(newEntries);

    if (BigDecimal.valueOf(ingestMetadata.getParameters().getLastSeenBalance()).setScale(2, RoundingMode.DOWN)
        .compareTo(BigDecimal.valueOf(oldBalance).setScale(2, RoundingMode.DOWN)) != 0) {
      log.warn("Balance mismatch!");
      return reconcileService.performReconcile(ingestMetadata, oldEntries);
    }

    return Mono.just(ingestMetadata);
  }

  private Mono<IngestSplitwiseMetadata> persistNewEntries(IngestSplitwiseMetadata ingestMetadata) {
    List<Expense> toPersist = ingestMetadata.getNewEntries().stream()
      .map(row -> tryCreateExpense(row, ingestMetadata.getUser()))
      .filter(Objects::nonNull)
      .toList();
    return expenseRepository.saveAll(toPersist).collectList()
      .flatMap(expenses -> expenseTagService.tag(expenses, Set.of(
          ExpenseTags.SPLITWISE.name(),
          ingestMetadata.getParameters().getGroupName())).collectList()
        .map(tags -> expenses))
      .map(addedRecords -> {
        ingestMetadata.getResponse().setRecordsAdded(addedRecords.size());
        return ingestMetadata;
      });
  }

  private Expense tryCreateExpense(Map<String, String> row, User user) {
    if ("Payment".equals(row.get("Category")) ||
        !user.getPrimaryCurrency().equals(row.get("Currency"))) {
      return null;
    }

    // FIXME: bug!
    double totalAmount = Double.parseDouble(row.get("Cost"));
    // double othersAmount
    double userAmount = Double.parseDouble(row.get(user.getFullName()));
    double amount = 0f;
    if (totalAmount == userAmount) {
      // User pays for other/s; user not involved
      return null;
    } else if (userAmount > 0f) {
      // User pays, splits with other/s
      amount = -(totalAmount - userAmount);
    } else if (userAmount < 0f) {
      // Other/s pay, user owes
      amount = userAmount;
    } else {
      // amount is zero; user not involved
      return null;
    }

    return Expense.builder()
      .apiKey(user.getApiKey())
      .summary(row.get("Description"))
      .amount(amount)
      .date(LocalDate.parse(row.get("Date"), DateTimeFormatter.ISO_LOCAL_DATE)
        .atStartOfDay()
        .atOffset(ZoneOffset.UTC))
      .build();
  }

  private Mono<IngestSplitwiseMetadata> updateParameters(IngestSplitwiseMetadata ingestMetadata) {
    if (ingestMetadata.getResponse().getRecordsAdded() <= 0) {
      log.warn("No records added, parameters are not updated.");
      return Mono.just(ingestMetadata);
    }

    IngestParameter toPersist = ingestMetadata.getParameters();
    toPersist.setLastSeenBalance(ingestMetadata.getNewBalance());
    toPersist.setLastProcessedDate(ingestMetadata.getNewestDateInEntries());
    return ingestParameterRepository.save(toPersist)
      .map(params -> {
        ingestMetadata.setParameters(params);
        return ingestMetadata;
      });
  }
}
