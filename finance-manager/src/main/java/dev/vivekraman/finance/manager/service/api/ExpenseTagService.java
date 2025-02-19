package dev.vivekraman.finance.manager.service.api;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import dev.vivekraman.finance.manager.entity.Expense;
import dev.vivekraman.finance.manager.model.response.ExpenseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ExpenseTagService {
  Flux<ExpenseDTO> findTagsForExpenses(String apiKey, Collection<Expense> expenses);
  Flux<ExpenseDTO> findExpensesByTagIn(String apiKey, Set<String> tags);
  Mono<Long> deleteExpensesByTagIn(String apiKey, Set<String> tags);
  Flux<ExpenseDTO> tag(List<Expense> expenses, Set<String> tags);
}
