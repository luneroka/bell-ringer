package com.bell_ringer.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

  private static final Logger logger = LoggerFactory.getLogger(FirebaseAuthenticationFilter.class);

  private final FirebaseAuth firebaseAuth;

  public FirebaseAuthenticationFilter(FirebaseAuth firebaseAuth) {
    this.firebaseAuth = firebaseAuth;
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {

    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    String token = resolveBearer(header);

    if (!StringUtils.hasText(token)) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      FirebaseToken decoded = firebaseAuth.verifyIdToken(token, true); // checks revocation
      Collection<SimpleGrantedAuthority> authorities = extractAuthorities(decoded.getClaims());
      FirebaseUserAuthentication authentication =
          new FirebaseUserAuthentication(decoded, authorities);

      SecurityContextHolder.getContext().setAuthentication(authentication);
    } catch (Exception ex) {
      logger.error("Firebase authentication failed for request {} {}", request.getMethod(), request.getRequestURI(), ex);
      // Optionally log at debug level; do not leak details to client
      SecurityContextHolder.clearContext();
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Firebase ID token");
      return;
    }

    filterChain.doFilter(request, response);
  }

  private static String resolveBearer(String header) {
    if (!StringUtils.hasText(header)) return null;
    if (!header.startsWith("Bearer ")) return null;
    return header.substring(7).trim();
  }

  @SuppressWarnings("unchecked")
  private static Collection<SimpleGrantedAuthority> extractAuthorities(Map<String, Object> claims) {
    // Expect roles inside a custom claim, e.g. { "roles": ["ADMIN", "USER"] }
    Object rolesObj = claims.get("roles");
    if (rolesObj instanceof List<?> list) {
      return list.stream()
          .filter(String.class::isInstance)
          .map(String.class::cast)
          .flatMap(r -> Stream.of("ROLE_" + r)) // Spring expects ROLE_*
          .map(SimpleGrantedAuthority::new)
          .toList();
    }
    return List.of();
  }

  /**
   * Lightweight Authentication object carrying the decoded FirebaseToken.
   */
  public static class FirebaseUserAuthentication extends AbstractAuthenticationToken {
    private final FirebaseToken token;

    public FirebaseUserAuthentication(FirebaseToken token, Collection<SimpleGrantedAuthority> authorities) {
      super(authorities);
      this.token = token;
      setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
      return ""; // credentials are not exposed
    }

    @Override
    public Object getPrincipal() {
      return token; // we’ll read UID/email from this
    }
  }
}