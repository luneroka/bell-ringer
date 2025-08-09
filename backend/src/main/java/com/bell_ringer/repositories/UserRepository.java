package com.bell_ringer.repositories;

import com.bell_ringer.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
  Optional<User> findByEmail(String email);
  boolean existsByEmail(String email);
  Optional<User> findByAuthProviderAndAuthUid(String authProvider, String authUid);
  boolean existsByAuthProviderAndAuthUid(String authProvider, String authUid);
}
