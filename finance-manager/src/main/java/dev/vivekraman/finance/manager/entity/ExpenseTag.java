package dev.vivekraman.finance.manager.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "finance_expense_tag")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseTag {
  @Id private String id;

  private String apiKey;

  /**
   *  foreign key on table {@code finance_expense}.
   *  See {@link Expense}
   */
  private String expenseId;
  private String tag;
}
