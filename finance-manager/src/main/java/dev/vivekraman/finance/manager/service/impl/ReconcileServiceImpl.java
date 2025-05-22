package dev.vivekraman.finance.manager.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import dev.vivekraman.finance.manager.model.IngestSplitwiseMetadata;
import dev.vivekraman.finance.manager.model.enums.ExpenseTags;
import dev.vivekraman.finance.manager.service.api.ExpenseTagService;
import dev.vivekraman.finance.manager.service.api.ReconcileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReconcileServiceImpl implements ReconcileService {
  private final ExpenseTagService expenseTagService;

  @Override
  public Mono<IngestSplitwiseMetadata> performReconcile(
    IngestSplitwiseMetadata metadata, List<Map<String, String>> oldRecords) {
    // Mark all old records to be re-added.
    metadata.getNewEntries().addAll(oldRecords);

    // delete all records by apiKey and groupName.
    return expenseTagService.deleteExpensesByTagIn(metadata.getUser().getApiKey(),
      Set.of(ExpenseTags.SPLITWISE.name(), metadata.getParameters().getGroupName()))
      .map(recordsDeleted -> {
        metadata.getResponse().setRecordsDeleted(recordsDeleted);
        return metadata;
      });
  }
}
