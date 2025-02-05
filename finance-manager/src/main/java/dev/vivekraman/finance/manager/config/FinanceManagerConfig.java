package dev.vivekraman.finance.manager.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FinanceManagerConfig {
  @Bean
  public GroupedOpenApi financeManagerApiGroup() {
    return GroupedOpenApi.builder()
        .group(Constants.MODULE_NAME)
        .packagesToScan("dev.vivekraman.finance.manager.controller")
        .build();
  }
}
