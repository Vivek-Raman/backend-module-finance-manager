package dev.vivekraman.finance.manager.entity;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "finance_expense_tag")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseTag {
  @Id private UUID id;
  private String apiKey;

  /**
   *  foreign key on table {@code finance_expense}.
   *  See {@link Expense}
   */
  private UUID expenseId;
  private String tag;
}
