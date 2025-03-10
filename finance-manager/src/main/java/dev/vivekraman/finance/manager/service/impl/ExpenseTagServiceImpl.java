package dev.vivekraman.finance.manager.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import dev.vivekraman.finance.manager.entity.Expense;
import dev.vivekraman.finance.manager.entity.ExpenseTag;
import dev.vivekraman.finance.manager.model.response.ExpenseDTO;
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
  public Flux<ExpenseDTO> findTagsForExpenses(String apiKey, Collection<Expense> expenses) {
    Set<String> expenseIds = expenses.stream()
      .map(e -> e.getId().toString()).collect(Collectors.toSet());
    return expenseTagRepository.findByApiKeyAndExpenseIdIn(apiKey, expenseIds).collectMultimap(ExpenseTag::getExpenseId)
      .flatMapMany(expenseIdToTagsMap -> buildExpenseDTOs(expenseIdToTagsMap, expenses));
  }

  @Override
  public Flux<ExpenseDTO> findExpensesByTagIn(String apiKey, Set<String> tags) {
    return expenseTagRepository.findByApiKeyAndTagIn(apiKey, tags).collectMultimap(ExpenseTag::getExpenseId)
      .zipWhen(expenseIdToTagsMap -> expenseRepository.findByApiKeyAndIdIn(apiKey, expenseIdToTagsMap.keySet()).collectList())
      .flatMapMany(tuple -> buildExpenseDTOs(tuple.getT1(), tuple.getT2()));
  }

  @Override
  public Mono<Long> deleteExpensesByTagIn(String apiKey, Set<String> tags) {
    return expenseTagRepository.findByApiKeyAndTagIn(apiKey, tags).collectList()
      .map(expenseTags -> expenseTags.stream()
        .map(ExpenseTag::getExpenseId).collect(Collectors.toSet()))
      .flatMap(ids -> expenseRepository.deleteAllById(ids)
        .map(e -> ids.size()).defaultIfEmpty(ids.size()))
        .flatMap(recordsDeleted -> expenseTagRepository.deleteByApiKeyAndTagIn(apiKey, tags)
            .map(e -> (long) recordsDeleted).defaultIfEmpty((long) recordsDeleted));
  }

  @Override
  public Flux<ExpenseDTO> tag(List<Expense> expenses, Set<String> tags) {
    List<ExpenseTag> toAdd = new LinkedList<>();
    expenses.forEach(expense ->
      tags.forEach(tag -> toAdd.add(ExpenseTag.builder()
        .apiKey(expense.getApiKey())
        .expenseId(expense.getId().toString())
        .tag(tag).build())));
    return expenseTagRepository.saveAll(toAdd).collectMultimap(ExpenseTag::getExpenseId)
      .flatMapMany(expenseIdToTagsMap -> buildExpenseDTOs(expenseIdToTagsMap, expenses));
  }

  private Flux<ExpenseDTO> buildExpenseDTOs(Map<String, Collection<ExpenseTag>> expenseIdToTagsMap, Collection<Expense> expenses) {
    return Flux.fromStream(expenses.stream()
      .map(expense -> ExpenseDTO.builder()
        .id(expense.getId().toString())
        .summary(expense.getSummary())
        .amount(expense.getAmount())
        .date(expense.getDate())
        .tags(expenseIdToTagsMap.get(expense.getId().toString())
          .stream().map(ExpenseTag::getTag).toList())
        .build()));
  }

  @Override
  public Mono<Boolean> deleteExpense(String apiKey, String expenseId) {
    return expenseTagRepository.deleteByApiKeyAndId(apiKey, UUID.fromString(expenseId))
      .map(e -> true).defaultIfEmpty(true)
      .zipWith(expenseRepository.deleteByApiKeyAndId(apiKey, expenseId))
      .map(res -> true).defaultIfEmpty(true);
  }
}
