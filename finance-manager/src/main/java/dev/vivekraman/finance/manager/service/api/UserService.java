package dev.vivekraman.finance.manager.service.api;

import dev.vivekraman.finance.manager.entity.User;
import dev.vivekraman.finance.manager.model.request.RegisterUserRequestDTO;
import reactor.core.publisher.Mono;

public interface UserService {
  Mono<User> fetchCurrentUser();
  Mono<User> fetchUser(String apiKey);
  Mono<User> addUser(RegisterUserRequestDTO user);
}
