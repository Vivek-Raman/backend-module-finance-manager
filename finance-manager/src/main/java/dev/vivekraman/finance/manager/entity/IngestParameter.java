package dev.vivekraman.finance.manager.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "finance_ingest_parameter")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class IngestParameter {
  @Id private UUID id;

  private String apiKey;
  private String groupName;
  private Double lastSeenBalance;
  private OffsetDateTime lastProcessedDate;
}
