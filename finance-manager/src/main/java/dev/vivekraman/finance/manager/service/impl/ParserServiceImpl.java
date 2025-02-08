package dev.vivekraman.finance.manager.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import dev.vivekraman.finance.manager.service.api.ParserService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class ParserServiceImpl implements ParserService {
  @Override
  public Mono<List<Map<String, String>>> parseCSV(FilePart file) {
    return readFile(file)
      .map(this::parseFile)
      .defaultIfEmpty(new ArrayList<>());
  }

  private Mono<InputStream> readFile(FilePart file) {
    return file.content().map(DataBuffer::asInputStream).next();
  }

  private List<Map<String, String>> parseFile(InputStream contents) {
    // CSVReader csvReader;
    List<Map<String, String>> list = new LinkedList<>();
    try (CSVReader csvReader = new CSVReader(new InputStreamReader(contents))) {
      String[] headers = csvReader.readNext();
      if (headers.length <= 0) {
        CsvValidationException ex = new CsvValidationException("No header columns found");
        ex.setLine(headers);
        ex.setLineNumber(1);
        throw ex;
      }

      String[] buffer = csvReader.readNext();
      Map<String, String> row;
      while (Objects.nonNull(buffer)) {
        row = new LinkedHashMap<>(buffer.length);
        if (StringUtils.isEmpty(buffer[0])) {
          buffer = csvReader.readNext();
          continue;
        }

        for (int i = 0; i < buffer.length; ++i) {
          row.put(headers[i], buffer[i]);
        }

        list.add(row);
        buffer = csvReader.readNext();
      }
    } catch (CsvValidationException e) {
      log.error("ERROR parsing CSV at line {}: \"{}\", error ",
        e.getLineNumber(), e.getLine(), e.getMessage());
        list = new LinkedList<>();
    } catch (IOException e) {
      log.error("ERROR parsing CSV, error ", e);
    }
    return list;
  }
}
