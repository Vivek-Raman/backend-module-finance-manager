package dev.vivekraman.finance.manager.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "dev.vivekraman.*")
public class BackendModuleFinanceManagerApplication {
	public static void main(String[] args) {
		SpringApplication.run(BackendModuleFinanceManagerApplication.class, args);
	}
}
