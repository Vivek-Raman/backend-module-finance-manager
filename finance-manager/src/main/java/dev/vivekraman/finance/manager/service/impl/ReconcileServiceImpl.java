package dev.vivekraman.finance.manager.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import dev.vivekraman.finance.manager.model.IngestMetadata;
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
  public Mono<IngestMetadata> performReconcile(
    IngestMetadata metadata, List<Map<String, String>> oldRecords) {

      // TODO: delete all records by apiKey and groupName.

    // TODO: push updates on old records into finance_expense and notify
    // TODO: push backdated new records into finance_expense and notify

    // return expenseTagService.findExpensesByTag(null, null)

    // return expenseRepository.findByApiKeySortByDateDesc(metadata.getUser().getApiKey(), PageRequest.of(0, 50))
    //   .map(expenses -> )
    //   .collectList().map(e -> metadata);

    return Mono.just(metadata);
  }
}
