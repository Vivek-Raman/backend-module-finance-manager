package dev.vivekraman.finance.manager.service.impl;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import dev.vivekraman.finance.manager.entity.Expense;
import dev.vivekraman.finance.manager.entity.ExpenseTag;
import dev.vivekraman.finance.manager.model.IngestResponseDTO;
import dev.vivekraman.finance.manager.model.enums.ExpenseTags;
import dev.vivekraman.finance.manager.repository.ExpenseRepository;
import dev.vivekraman.finance.manager.service.api.ExpenseTagService;
import dev.vivekraman.finance.manager.service.api.NotionIngestService;
import dev.vivekraman.monolith.security.util.AuthUtils;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class NotionIngestServiceImpl implements NotionIngestService {
  private final ExpenseRepository expenseRepository;
  private final ExpenseTagService expenseTagService;

  @Override
  public Mono<IngestResponseDTO> ingestNotion(List<Map<String, String>> entries) {
    return AuthUtils.fetchApiKey()
      .map(apiKey -> entries.stream()
        .map(entry -> Expense.builder()
          .apiKey(apiKey)
          .summary(entry.get("Summary"))
          // .amount(Double.parseDouble(entry.get("Amount")))
          .date(OffsetDateTime.parse(entry.get("Date"), DateTimeFormatter.ofPattern("MMMM dd, yyyy hh:mm ()")))
          .build())
        .toList())
      // TODO: fix parsing and implement
      // .flatMapMany(expenseRepository::saveAll)
      // .collectList()
      // .flatMapMany(expenses -> expenseTagService.tag(expenses, Set.of(ExpenseTags.NOTION.name())))
      // .collectList()
      .map(expenses -> IngestResponseDTO.builder()
        .recordsAdded(expenses.size())
        .build());
  }
}
