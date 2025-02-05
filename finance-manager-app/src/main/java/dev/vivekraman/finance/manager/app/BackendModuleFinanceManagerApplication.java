package dev.vivekraman.finance.manager.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;

@SpringBootApplication(
  scanBasePackages = "dev.vivekraman.*",
  exclude = {
    ReactiveSecurityAutoConfiguration.class,
    ReactiveUserDetailsServiceAutoConfiguration.class,
  })
public class BackendModuleFinanceManagerApplication {
	public static void main(String[] args) {
		SpringApplication.run(BackendModuleFinanceManagerApplication.class, args);
	}
}
