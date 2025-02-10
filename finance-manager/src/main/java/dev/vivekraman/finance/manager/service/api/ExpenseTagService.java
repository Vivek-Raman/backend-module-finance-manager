package dev.vivekraman.finance.manager.service.api;

import java.util.List;
import java.util.Set;

import dev.vivekraman.finance.manager.entity.Expense;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ExpenseTagService {
  Flux<Expense> findExpensesByTagIn(String apiKey, Set<String> tags);
  Mono<Long> deleteExpensesByTagIn(String apiKey, Set<String> tags);
  Mono<Boolean> tag(List<Expense> expenses, Set<String> tags);
}
