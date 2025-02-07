package dev.vivekraman.finance.manager.service.impl;

import java.util.Map;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;

import com.opencsv.CSVReader;

import dev.vivekraman.finance.manager.service.api.ParserService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class ParserServiceImpl implements ParserService {

  @Override
  public Mono<Map<String, String>> parseCSV(FilePart file) {
    return file.content();
  }

}
