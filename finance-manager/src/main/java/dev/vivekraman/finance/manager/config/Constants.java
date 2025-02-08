package dev.vivekraman.finance.manager.config;

public interface Constants {
  String MODULE_NAME = "finance-manager";
  String PRE_AUTHORIZATION_SPEC = "hasAuthority('finance_manager')";
  String INGEST_LAST_SEEN_BALANCE = "last_seen_balance";
  String INGEST_LAST_PROCESSED_DATE = "last_processed_date";
}
