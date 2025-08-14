package com.bell_ringer.services;

import com.bell_ringer.models.User;
import com.bell_ringer.repositories.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

@Service
public class UserService {
  private final UserRepository userRepository;

  // Constructor
  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  // ============================================
  // FIREBASE AUTHENTICATION OPERATIONS
  // ============================================

  /**
   * Get current authenticated user from Firebase token context
   * Automatically syncs/creates user from Firebase token data
   */
  @Transactional
  public User getCurrentUserOrThrow() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) {
      throw new IllegalStateException("No authenticated user in context");
    }

    Object principal = auth.getPrincipal();
    if (!(principal instanceof FirebaseToken token)) {
      throw new IllegalStateException("Unexpected principal type");
    }

    // Upsert & return (keeps local profile fresh)
    return syncFromToken(token);
  }

  /**
   * Sync/create user from a verified Firebase ID token
   * Updates existing user or creates new one with Firebase data
   */
  @Transactional
  public User syncFromToken(FirebaseToken token) {
    String uid = token.getUid();
    String email = token.getEmail(); // may be null if provider didn’t supply one
    Boolean emailVerified = token.isEmailVerified();
    String displayName = token.getName();
    String photoUrl = token.getPicture();

    return upsertFromAuth(
        "firebase",
        uid,
        email,
        emailVerified,
        displayName,
        photoUrl);
  }

  /**
   * Core upsert logic for authentication provider users
   * Updates existing user or creates new one based on provider + UID
   */
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
          if (emailVerified != null)
            u.setEmailVerified(emailVerified);
          if (displayName != null)
            u.setDisplayName(displayName);
          if (photoUrl != null)
            u.setPhotoUrl(photoUrl);
          return u; // Hibernate dirty checking will persist changes
        })
        .orElseGet(() -> {
          User u = new User(provider, uid, normalizedEmail);
          if (emailVerified != null)
            u.setEmailVerified(emailVerified);
          u.setDisplayName(displayName);
          u.setPhotoUrl(photoUrl);
          return userRepository.save(u);
        });
  }

  // ============================================
  // USER QUERY OPERATIONS
  // ============================================

  /**
   * Get all users in the system
   */
  @Transactional(readOnly = true)
  public Iterable<User> getAllUsers() {
    return userRepository.findAll();
  }

  /**
   * Get user by unique ID
   */
  @Transactional(readOnly = true)
  public User getUserById(UUID id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
  }

  /**
   * Find user by email address
   */
  @Transactional(readOnly = true)
  public Optional<User> getUserByEmail(String email) {
    if (email == null)
      return Optional.empty();
    String normalized = email.trim().toLowerCase();
    return userRepository.findByEmail(normalized);
  }

  /**
   * Find user by authentication provider and UID
   */
  @Transactional(readOnly = true)
  public Optional<User> getByAuth(String authProvider, String authUid) {
    if (authProvider == null || authProvider.isBlank() || authUid == null || authUid.isBlank()) {
      return Optional.empty();
    }
    return userRepository.findByAuthProviderAndAuthUid(authProvider.trim().toLowerCase(), authUid.trim());
  }

  // ============================================
  // USER MODIFICATION OPERATIONS
  // ============================================

  /**
   * Update user profile information
   */
  @Transactional
  public User updateUser(UUID id, String email, Boolean emailVerified, String displayName, String photoUrl) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    if (email != null)
      user.setEmail(email.trim().toLowerCase());
    if (emailVerified != null)
      user.setEmailVerified(emailVerified);
    if (displayName != null)
      user.setDisplayName(displayName);
    if (photoUrl != null)
      user.setPhotoUrl(photoUrl);

    return userRepository.save(user);
  }

  /**
   * Delete user from system
   */
  @Transactional
  public void deleteUser(UUID id) {
    User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    userRepository.delete(user);
  }

  // ============================================
  // UTILITY HELPER METHODS
  // ============================================

  /**
   * Get current user's ID from Firebase token context
   */
  public UUID getCurrentUserId() {
    User user = getCurrentUserOrThrow();
    return user.getId();
  }

  /**
   * Check if user exists by Firebase UID
   */
  @Transactional(readOnly = true)
  public boolean existsByFirebaseUid(String firebaseUid) {
    return userRepository.findByAuthProviderAndAuthUid("firebase", firebaseUid).isPresent();
  }
}
