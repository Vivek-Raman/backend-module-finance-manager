package dev.vivekraman.finance.manager.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.vivekraman.finance.manager.config.Constants;
import dev.vivekraman.finance.manager.entity.User;
import dev.vivekraman.finance.manager.model.request.RegisterUserRequestDTO;
import dev.vivekraman.finance.manager.model.response.UserDTO;
import dev.vivekraman.finance.manager.service.api.UserService;
import dev.vivekraman.monolith.annotation.MonolithController;
import dev.vivekraman.monolith.model.Response;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@MonolithController(moduleName = Constants.MODULE_NAME)
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;
  private final ObjectMapper objectMapper;
  private final Scheduler scheduler;

  @PreAuthorize(Constants.PRE_AUTHORIZATION_SPEC)
  @PostMapping("/user")
  public Mono<Response<UserDTO>> registerUser(@RequestBody RegisterUserRequestDTO newUser) {
    return userService.addUser(newUser)
      .map(user -> Response.of(objectMapper.convertValue(user, UserDTO.class)))
      .defaultIfEmpty(new Response<>())
      .subscribeOn(scheduler);
  }

  @PreAuthorize(Constants.PRE_AUTHORIZATION_SPEC)
  @GetMapping("/user")
  public Mono<Response<UserDTO>> fetchCurrentUser() {
    return userService.fetchCurrentUser()
      .map(user -> Response.of(objectMapper.convertValue(user, UserDTO.class)))
      .defaultIfEmpty(new Response<>())
      .subscribeOn(scheduler);
  }
}
