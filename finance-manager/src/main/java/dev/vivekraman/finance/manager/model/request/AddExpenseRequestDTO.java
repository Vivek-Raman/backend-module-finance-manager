package dev.vivekraman.finance.manager.model.request;

import java.time.LocalDateTime;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddExpenseRequestDTO {
  private String summary;
  private Double amount;
  private LocalDateTime date;
  private Set<String> tags;
}
