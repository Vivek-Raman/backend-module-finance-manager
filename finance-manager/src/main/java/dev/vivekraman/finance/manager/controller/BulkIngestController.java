package dev.vivekraman.finance.manager.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClientResponseException.Unauthorized;

import dev.vivekraman.finance.manager.config.Constants;
import dev.vivekraman.finance.manager.service.api.IngestService;
import dev.vivekraman.finance.manager.service.api.ParserService;
import dev.vivekraman.monolith.annotation.MonolithController;
import dev.vivekraman.monolith.model.Response;
import dev.vivekraman.monolith.security.util.AuthUtils;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@MonolithController(moduleName = Constants.MODULE_NAME)
@RequiredArgsConstructor
public class BulkIngestController {
  private final IngestService ingestService;
  private final ParserService parserService;
  private final Scheduler scheduler;

  @PreAuthorize(Constants.PRE_AUTHORIZATION_SPEC)
  @PostMapping(path = "/bulk/splitwise", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Mono<Response<Boolean>> ingestSplitwiseExport(@RequestPart FilePart file) {
    return parserService.parseCSV(file)
      .flatMap(ingestService::ingestSplitwise)
      .map(Response::of).subscribeOn(scheduler);
  }
}
