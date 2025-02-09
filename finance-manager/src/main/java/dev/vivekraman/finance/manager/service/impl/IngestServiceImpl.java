package dev.vivekraman.finance.manager.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Service;

import dev.vivekraman.finance.manager.config.Constants;
import dev.vivekraman.finance.manager.entity.Expense;
import dev.vivekraman.finance.manager.entity.IngestParameter;
import dev.vivekraman.finance.manager.entity.User;
import dev.vivekraman.finance.manager.model.IngestMetadata;
import dev.vivekraman.finance.manager.model.IngestSplitwiseResponseDTO;
import dev.vivekraman.finance.manager.model.enums.ExpenseTags;
import dev.vivekraman.finance.manager.repository.ExpenseRepository;
import dev.vivekraman.finance.manager.repository.IngestParameterRepository;
import dev.vivekraman.finance.manager.service.api.ExpenseTagService;
import dev.vivekraman.finance.manager.service.api.IngestService;
import dev.vivekraman.finance.manager.service.api.ReconcileService;
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
  private final ReconcileService reconcileService;
  private final ExpenseTagService expenseTagService;
  private final ExpenseRepository expenseRepository;
  private final IngestParameterRepository ingestParameterRepository;

  @Override
  public Mono<IngestSplitwiseResponseDTO> ingestSplitwise(List<Map<String, String>> entries) {
    // omit last entry (total balances; not an expense)
    entries.removeLast();

    // TODO: push new records into finance_expense and notify
    // TODO: update ingest params

    return AuthUtils.fetchApiKey()
      .flatMap(apiKey -> Mono.zip(
        userService.fetchUser(apiKey),
        ingestParameterRepository.findByApiKey(apiKey)
          .defaultIfEmpty(new IngestParameter()),
        IngestMetadata::create))
      .flatMap(this::handleNullParameters)
      .flatMap(ingestMetadata -> filterEntries(ingestMetadata, entries))
      .flatMap(this::persistNewEntries)
      .flatMap(this::updateParameters)
      .map(IngestMetadata::getResponse)
      .defaultIfEmpty(new IngestSplitwiseResponseDTO());
  }

  private Mono<IngestMetadata> handleNullParameters(IngestMetadata ingestMetadata) {
    if (StringUtils.isBlank(ingestMetadata.getParameters().getApiKey())) {
      return ingestParameterRepository.save(
        IngestParameter.builder()
          .apiKey(ingestMetadata.getUser().getApiKey())
          .lastSeenBalance(0d)
          .lastProcessedDate(LocalDateTime.parse("1970-01-01T00:00:00"))
          .build())
        .map(params -> {
          ingestMetadata.setParameters(params);
          return ingestMetadata;
        });
    } else {
      return Mono.just(ingestMetadata);
    }
  }

  private Mono<IngestMetadata> filterEntries(IngestMetadata ingestMetadata, List<Map<String, String>> entries) {
    String username = ingestMetadata.getUser().getFullName();
    LocalDate lastProcessedDate = ingestMetadata.getParameters()
      .getLastProcessedDate().toLocalDate();

    List<Map<String, String>> oldEntries = new LinkedList<>();
    List<Map<String, String>> newEntries = new LinkedList<>();
    double oldBalance = 0f;
    double newBalance = 0f;
    ingestMetadata.setNewestDateInEntries(LocalDate.parse(entries.get(0).get("Date")));
    for (int i = 0; i < entries.size(); ++i) {
      Map<String, String> row = entries.get(i);
      LocalDate date = LocalDate.parse(row.get("Date"));
      double amount = Double.parseDouble(row.get(username));
      if (!date.isAfter(lastProcessedDate)) {
        oldEntries.add(row);
        oldBalance += amount;
      } else if (date.isBefore(LocalDate.now())) {
        newBalance += amount;
        newEntries.add(row);
        if (date.isAfter(ingestMetadata.getNewestDateInEntries())) {
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
      // return reconcileService.performReconcile(ingestMetadata, oldEntries);
    }

    return Mono.just(ingestMetadata);
  }

  private Mono<IngestMetadata> persistNewEntries(IngestMetadata ingestMetadata) {
    List<Expense> toPersist = ingestMetadata.getNewEntries().stream()
      .map(row -> tryCreateExpense(row, ingestMetadata.getUser()))
      .filter(Objects::nonNull)
      .toList();
    return expenseRepository.saveAll(toPersist).collectList()
      .flatMap(expenses -> expenseTagService.tag(expenses, Set.of(ExpenseTags.SPLITWISE.name()))
        .map(tags -> expenses))
      .map(addedRecords -> {
        ingestMetadata.getResponse().setRecordsAdded(addedRecords.size());
        return ingestMetadata;
      });
  }

  private Expense tryCreateExpense(Map<String, String> row, User user) {
    if ("Payment".equals(row.get("Category")) ||
        !"USD".equals(row.get("Currency"))) {
      return null;
    }

    double totalAmount = Double.parseDouble(row.get("Cost"));
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
      .date(LocalDate.parse(row.get("Date")).atStartOfDay())
      .build();
  }

  private Mono<IngestMetadata> updateParameters(IngestMetadata ingestMetadata) {
    if (ingestMetadata.getResponse().getRecordsAdded() <= 0) {
      log.warn("No records added, parameters are not updated.");
      return Mono.just(ingestMetadata);
    }

    IngestParameter toPersist = ingestMetadata.getParameters();
    toPersist.setLastSeenBalance(ingestMetadata.getNewBalance());
    toPersist.setLastProcessedDate(ingestMetadata.getNewestDateInEntries()
      .atStartOfDay());
    return ingestParameterRepository.save(toPersist)
      .map(params -> {
        ingestMetadata.setParameters(params);
        return ingestMetadata;
      });
  }
}
