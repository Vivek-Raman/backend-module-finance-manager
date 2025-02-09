package dev.vivekraman.finance.manager.service.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import dev.vivekraman.finance.manager.entity.Expense;
import dev.vivekraman.finance.manager.entity.ExpenseTag;
import dev.vivekraman.finance.manager.repository.ExpenseRepository;
import dev.vivekraman.finance.manager.repository.ExpenseTagRepository;
import dev.vivekraman.finance.manager.service.api.ExpenseTagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseTagServiceImpl implements ExpenseTagService {
  private final ExpenseRepository expenseRepository;
  private final ExpenseTagRepository expenseTagRepository;

  @Override
  public Flux<Expense> findExpensesByTag(String apiKey, String tag) {
    return expenseTagRepository.findByApiKeyAndTag(apiKey, tag).collectList()
      .map(expenseTags -> expenseTags.stream().map(ExpenseTag::getExpenseId).toList())
      .flatMapMany(ids -> expenseRepository.findAllById(ids));
  }

  @Override
  public Mono<Boolean> tag(List<Expense> expenses, Set<String> tags) {
    List<ExpenseTag> toAdd = new LinkedList<>();
    expenses.forEach(expense -> {
      tags.forEach(tag -> {
        toAdd.add(ExpenseTag.builder()
        .apiKey(expense.getApiKey())
        .expenseId(expense.getId().toString())
        .tag(tag).build());
      });
    });
    return expenseTagRepository.saveAll(toAdd).collectList()
      .map(e -> true);
  }
}
