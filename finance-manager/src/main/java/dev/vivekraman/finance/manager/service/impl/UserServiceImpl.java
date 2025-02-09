package dev.vivekraman.finance.manager.service.impl;

import org.springframework.stereotype.Service;

import dev.vivekraman.finance.manager.entity.User;
import dev.vivekraman.finance.manager.model.request.RegisterUserRequestDTO;
import dev.vivekraman.finance.manager.repository.UserRepository;
import dev.vivekraman.finance.manager.service.api.UserService;
import dev.vivekraman.monolith.security.util.AuthUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;

  @Override
  public Mono<User> fetchCurrentUser() {
    return AuthUtils.fetchApiKey()
      .flatMap(this::fetchUser);
  }

  @Override
  public Mono<User> fetchUser(String apiKey) {
    return userRepository.findByApiKey(apiKey);
  }

  @Override
  public Mono<User> addUser(RegisterUserRequestDTO user) {
    return AuthUtils.fetchApiKey()
      .map(apiKey -> User.builder()
        .apiKey(apiKey)
        .fullName(user.getFullName())
        .primaryCurrency(user.getPrimaryCurrency())
        .build())
      .flatMap(userRepository::save);
  }
}
