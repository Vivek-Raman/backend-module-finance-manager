package dev.vivekraman.finance.manager.controller;

import java.util.List;

import dev.vivekraman.monolith.security.util.AuthUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.vivekraman.finance.manager.config.Constants;
import dev.vivekraman.finance.manager.entity.Expense;
import dev.vivekraman.finance.manager.model.ExpenseDTO;
import dev.vivekraman.finance.manager.model.request.AddExpenseRequestDTO;
import dev.vivekraman.finance.manager.repository.ExpenseRepository;
import dev.vivekraman.finance.manager.service.api.ExpenseTagService;
import dev.vivekraman.monolith.annotation.MonolithController;
import dev.vivekraman.monolith.model.Response;
import dev.vivekraman.monolith.model.ResponseList;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@MonolithController(moduleName = Constants.MODULE_NAME)
@RequiredArgsConstructor
public class ExpenseController {
  private final ExpenseRepository expenseRepository;
  private final ExpenseTagService expenseTagService;
  private final ObjectMapper objectMapper;
  private final Scheduler scheduler;

  @PreAuthorize(Constants.PRE_AUTHORIZATION_SPEC)
  @PostMapping("/expenses")
  public Mono<Response<ExpenseDTO>> addExpense(@RequestBody AddExpenseRequestDTO expenseDTO) {
    Expense toAdd = objectMapper.convertValue(expenseDTO, Expense.class);
    return expenseRepository.save(toAdd)
      .flatMap(expense -> expenseTagService.tag(List.of(expense), expenseDTO.getTags())
        .map(e -> expense))
      .map(e -> Response.of(objectMapper.convertValue(e, ExpenseDTO.class)))
      .subscribeOn(scheduler);
  }

  @PreAuthorize(Constants.PRE_AUTHORIZATION_SPEC)
  @GetMapping("/expenses")
  public Mono<ResponseList<ExpenseDTO>> fetchExpenses(int page, int size) {
    PageRequest pageRequest = PageRequest.of(page, size);
    return AuthUtils.fetchApiKey()
      .flatMapMany(apiKey -> expenseRepository.findByApiKeyOrderByDateDesc(apiKey, pageRequest))
      .map(e -> objectMapper.convertValue(e, ExpenseDTO.class))
      .collectList().map(list -> {
        ResponseList<ExpenseDTO> response = ResponseList.of(list);
        response.setData(list);
        return response;
      }).subscribeOn(scheduler);
  }
}
