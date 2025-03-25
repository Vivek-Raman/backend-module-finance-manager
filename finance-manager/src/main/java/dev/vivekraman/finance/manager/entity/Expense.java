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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "finance_expense")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Expense {
  @Id private UUID id;
  private String apiKey;
  private String summary;
  private Double amount;
  private OffsetDateTime date;
}
