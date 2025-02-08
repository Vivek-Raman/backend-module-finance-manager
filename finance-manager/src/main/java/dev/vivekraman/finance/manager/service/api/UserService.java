package dev.vivekraman.finance.manager.service.api;

import dev.vivekraman.finance.manager.entity.User;
import reactor.core.publisher.Mono;

public interface UserService {
  Mono<User> fetchCurrentUser();
  Mono<User> fetchUser(String apiKey);
}
