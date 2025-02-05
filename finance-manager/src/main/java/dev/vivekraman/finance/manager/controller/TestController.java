package dev.vivekraman.finance.manager.controller;

import dev.vivekraman.finance.manager.config.Constants;
import dev.vivekraman.monolith.annotation.MonolithController;
import dev.vivekraman.monolith.model.Response;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@MonolithController(moduleName = Constants.MODULE_NAME)
@RequiredArgsConstructor
public class TestController {
  private final Scheduler scheduler;


  @PreAuthorize("hasAuthority('finance_manager')")
  @GetMapping
  public Mono<Response<Boolean>> test() {
    return Mono.just(Response.of(true))
        .subscribeOn(scheduler);
  }
}
