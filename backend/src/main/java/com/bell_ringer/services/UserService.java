package com.bell_ringer.services;

import com.bell_ringer.models.User;
import com.bell_ringer.repositories.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  //  GET ALL USERS
  @Transactional(readOnly = true)
  public Iterable<User> getAllUsers() {
    return userRepository.findAll();
  }

  // CREATE USER
  @Transactional
  public User createUser(String email) {
    if (email == null || email.isBlank()) {
      throw new IllegalArgumentException("Email must not be blank");
    }
    String normalized = email.trim().toLowerCase();
    // Create a local user using a random auth UID; this keeps the method usable in dev/tests.
    String randomLocalUid = UUID.randomUUID().toString();
    return upsertFromAuth("local", randomLocalUid, normalized, false, null, null);
  }

  // GET USER BY ID
  @Transactional(readOnly = true)
  public User getUserById(UUID id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
  }

  // GET USER BY EMAIL
  @Transactional(readOnly = true)
  public Optional<User> getUserByEmail(String email) {
    if (email == null) return Optional.empty();
    String normalized = email.trim().toLowerCase();
    return userRepository.findByEmail(normalized);
  }

  // UPSERT FROM AUTH
  @Transactional
  public User upsertFromAuth(String authProvider,
                             String authUid,
                             String email,
                             Boolean emailVerified,
                             String displayName,
                             String photoUrl) {
    if (authProvider == null || authProvider.isBlank()) {
      throw new IllegalArgumentException("authProvider must not be blank");
    }
    if (authUid == null || authUid.isBlank()) {
      throw new IllegalArgumentException("authUid must not be blank");
    }

    String provider = authProvider.trim().toLowerCase();
    String uid = authUid.trim();
    String normalizedEmail = (email == null || email.isBlank()) ? null : email.trim().toLowerCase();

    return userRepository.findByAuthProviderAndAuthUid(provider, uid)
        .map(u -> {
          u.setEmail(normalizedEmail);
          if (emailVerified != null) u.setEmailVerified(emailVerified);
          if (displayName != null) u.setDisplayName(displayName);
          if (photoUrl != null) u.setPhotoUrl(photoUrl);
          return u; // Hibernate dirty checking will persist changes
        })
        .orElseGet(() -> {
          User u = new User(provider, uid, normalizedEmail);
          if (emailVerified != null) u.setEmailVerified(emailVerified);
          u.setDisplayName(displayName);
          u.setPhotoUrl(photoUrl);
          return userRepository.save(u);
        });
  }

  // GET BY AUTH
  @Transactional(readOnly = true)
  public Optional<User> getByAuth(String authProvider, String authUid) {
    if (authProvider == null || authProvider.isBlank() || authUid == null || authUid.isBlank()) {
      return Optional.empty();
    }
    return userRepository.findByAuthProviderAndAuthUid(authProvider.trim().toLowerCase(), authUid.trim());
  }

  // UPDATE USER
  @Transactional
  public User updateUser(UUID id, String email, Boolean emailVerified, String displayName, String photoUrl) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    if (email != null) user.setEmail(email.trim().toLowerCase());
    if (emailVerified != null) user.setEmailVerified(emailVerified);
    if (displayName != null) user.setDisplayName(displayName);
    if (photoUrl != null) user.setPhotoUrl(photoUrl);

    return userRepository.save(user);
  }

  // DELETE USER
  @Transactional
  public void deleteUser(UUID id) {
    User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    userRepository.delete(user);
  }
}
