package dev.vivekraman.finance.manager.entity;

import java.time.LocalDateTime;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "finance_expense")
public class Expense {
  @Id private String id;
  private String summary;
  private Float amount;
  private LocalDateTime date;
}
