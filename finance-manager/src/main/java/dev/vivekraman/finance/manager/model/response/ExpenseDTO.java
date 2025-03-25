package dev.vivekraman.finance.manager.model.response;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExpenseDTO {
  private String id;
  private String summary;
  private Double amount;
  private OffsetDateTime date;
  @Builder.Default
  private List<String> tags = new ArrayList<>();
}
