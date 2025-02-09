package dev.vivekraman.finance.manager.service.api;

import java.util.List;
import java.util.Set;

import dev.vivekraman.finance.manager.entity.Expense;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ExpenseTagService {
  Flux<Expense> findExpensesByTag(String apiKey, String tag);
  Mono<Boolean> tag(List<Expense> expenses, Set<String> tags);
}
