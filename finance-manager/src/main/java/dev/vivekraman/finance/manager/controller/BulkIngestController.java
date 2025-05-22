package dev.vivekraman.finance.manager.controller;

import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;

import dev.vivekraman.finance.manager.config.Constants;
import dev.vivekraman.finance.manager.model.IngestResponseDTO;
import dev.vivekraman.finance.manager.service.api.SplitwiseIngestService;
import dev.vivekraman.finance.manager.service.api.NotionIngestService;
import dev.vivekraman.finance.manager.service.api.ParserService;
import dev.vivekraman.monolith.annotation.MonolithController;
import dev.vivekraman.monolith.model.Response;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@MonolithController(moduleName = Constants.MODULE_NAME)
@RequiredArgsConstructor
public class BulkIngestController {
  private final SplitwiseIngestService splitwiseIngestService;
  private final NotionIngestService notionIngestService;
  private final ParserService parserService;
  private final Scheduler scheduler;

  @PreAuthorize(Constants.PRE_AUTHORIZATION_SPEC)
  @PostMapping(path = "/bulk/splitwise", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Mono<Response<IngestResponseDTO>> ingestSplitwiseExport(@RequestPart FilePart file) {
    return parserService.parseCSV(file)
      .flatMap(records -> splitwiseIngestService.ingestSplitwise(
        splitwiseIngestService.parseGroupName(file.filename()), records))
      .map(Response::of).subscribeOn(scheduler);
  }

  @PreAuthorize(Constants.PRE_AUTHORIZATION_SPEC)
  @PostMapping(path = "/bulk/notion", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Mono<Response<IngestResponseDTO>> ingestNotionExport(@RequestPart FilePart file) {
    return parserService.parseCSV(file)
      .flatMap(notionIngestService::ingestNotion)
      .map(Response::of).subscribeOn(scheduler);
  }
}
