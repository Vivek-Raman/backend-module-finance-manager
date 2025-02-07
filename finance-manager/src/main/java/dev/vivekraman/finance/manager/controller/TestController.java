package dev.vivekraman.finance.manager.controller;

import dev.vivekraman.finance.manager.config.Constants;
import dev.vivekraman.finance.manager.service.api.ParserService;
import dev.vivekraman.monolith.annotation.MonolithController;
import dev.vivekraman.monolith.model.Response;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@MonolithController(moduleName = Constants.MODULE_NAME)
@RequiredArgsConstructor
public class TestController {
  private final ParserService parserService;
  private final Scheduler scheduler;


  @PreAuthorize("hasAuthority('finance_manager')")
  @GetMapping
  public Mono<Response<Boolean>> test() {
    return Mono.just(Response.of(true))
        .subscribeOn(scheduler);
  }

  @PreAuthorize("hasAuthority('finance_manager')")
  @PostMapping
  public Mono<Response<Map<String, String>>> fileTest(@RequestPart("file") Mono<FilePart> file) {
    return file.flatMap(parserService::parseCSV)
      .map(Response::of)
      .subscribeOn(scheduler);
  }
}
