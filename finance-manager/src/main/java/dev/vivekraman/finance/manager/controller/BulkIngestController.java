package dev.vivekraman.finance.manager.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;

import dev.vivekraman.finance.manager.config.Constants;
import dev.vivekraman.monolith.annotation.MonolithController;
import dev.vivekraman.monolith.model.Response;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@MonolithController(moduleName = Constants.MODULE_NAME)
@RequiredArgsConstructor
public class BulkIngestController {
  private final Scheduler scheduler;

  @PreAuthorize("hasAuthority('finance-manager')")
  @PostMapping("/bulk/parse")
  public Mono<Response<Boolean>> ingestSplitwiseExport() {
    return Mono.just(true)
      .map(Response::of).subscribeOn(scheduler);
  }
}
