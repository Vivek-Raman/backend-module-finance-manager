package dev.vivekraman.finance.manager.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@Configuration
@EnableR2dbcRepositories(basePackages = "dev.vivekraman.finance.manager.repository")
public class FinanceManagerConfig {
  @Bean
  public GroupedOpenApi financeManagerApiGroup() {
    return GroupedOpenApi.builder()
        .group(Constants.MODULE_NAME)
        .packagesToScan("dev.vivekraman.finance.manager.controller")
        .build();
  }
}
