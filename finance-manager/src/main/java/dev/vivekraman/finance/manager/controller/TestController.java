package dev.vivekraman.finance.manager.controller;

import java.util.Map;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.vivekraman.finance.manager.config.Constants;
import dev.vivekraman.finance.manager.entity.Expense;
import dev.vivekraman.finance.manager.model.ExpenseDTO;
import dev.vivekraman.finance.manager.repository.ExpenseRepository;
import dev.vivekraman.finance.manager.service.api.ParserService;
import dev.vivekraman.monolith.annotation.MonolithController;
import dev.vivekraman.monolith.model.Response;
import dev.vivekraman.monolith.model.ResponseList;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@MonolithController(moduleName = Constants.MODULE_NAME)
@RequiredArgsConstructor
public class TestController {
  private final ExpenseRepository expenseRepository;
  private final ObjectMapper objectMapper;
  private final ParserService parserService;
  private final Scheduler scheduler;

  @PreAuthorize("hasAuthority('finance_manager')")
  @PostMapping("/expenses")
  public Mono<Response<ExpenseDTO>> addExpense(@RequestBody ExpenseDTO expenseDTO) {
    Expense toAdd = objectMapper.convertValue(expenseDTO, Expense.class);
    return expenseRepository.save(toAdd)
      .map(e -> Response.of(objectMapper.convertValue(e, ExpenseDTO.class)))
      .subscribeOn(scheduler);
  }

  @PreAuthorize("hasAuthority('finance_manager')")
  @GetMapping("/expenses")
  public Mono<ResponseList<ExpenseDTO>> fetchExpenses() {
    return expenseRepository.findAll()
      .map(e -> objectMapper.convertValue(e, ExpenseDTO.class))
      .collectList().map(list -> {
        ResponseList<ExpenseDTO> response = ResponseList.of(list);
        response.setData(list);
        return response;
      }).subscribeOn(scheduler);
  }

  @PreAuthorize("hasAuthority('finance_manager')")
  @PostMapping
  public Mono<Response<Map<String, String>>> fileTest(@RequestPart("file") Mono<FilePart> file) {
    return file.flatMap(parserService::parseCSV)
      .map(Response::of)
      .subscribeOn(scheduler);
  }
}
