package dev.vivekraman.finance.manager.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import dev.vivekraman.finance.manager.entity.User;

@Repository
public interface UserRepository extends ReactiveCrudRepository<User, String> {
}
