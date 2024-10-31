/*
 * Copyright (c) 2024 Singular™
 * SPDX-License-Identifier: MIT
 */

package ai.singlr.core.dto;

import ai.singlr.core.DateTimeUtils;
import ai.singlr.core.StringUtils;
import ai.singlr.core.result.ValidationResult;
import java.time.OffsetDateTime;

/**
 * Represents a passkey authenticator.
 */
public record Authenticator(
    String email,
    String handle,
    String credentialId,
    byte[] publicKey,
    Long counter,
    byte[] aaguid,
    OffsetDateTime createdAt,
    OffsetDateTime lastModifiedAt
) {

  /**
   * Validates the given {@link Authenticator} record for creation.
   */
  public static ValidationResult<Authenticator> validate(Authenticator authenticator) {
    if (authenticator == null) {
      return new ValidationResult<>("Authenticator must be specified");
    }

    if (StringUtils.isBlank(authenticator.email())) {
      return new ValidationResult<>("'email' must be specified");
    }

    if (StringUtils.isBlank(authenticator.handle())) {
      return new ValidationResult<>("'handle' must be specified");
    }

    if (StringUtils.isBlank(authenticator.credentialId())) {
      return new ValidationResult<>("'credentialId' must be specified");
    }

    if (authenticator.publicKey() == null) {
      return new ValidationResult<>("'publicKey' must be specified");
    }

    if (authenticator.counter() == null) {
      return new ValidationResult<>("'counter' must be specified");
    }

    if (authenticator.createdAt() == null) {
      return new ValidationResult<>("'createdAt' must be specified");
    }

    return new ValidationResult<>(new Authenticator(
        authenticator.email(),
        authenticator.handle(),
        authenticator.credentialId(),
        authenticator.publicKey(),
        authenticator.counter(),
        authenticator.aaguid(),
        authenticator.createdAt(),
        DateTimeUtils.now()
    ));
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  /**
   * Builder used to create immutable {@link Authenticator} records (DTOs).
   */
  public static class Builder {
    private String email;
    private String handle;
    private String credentialId;
    private byte[] publicKey;
    private Long counter;
    private byte[] aaguid;
    private OffsetDateTime createdAt;
    private OffsetDateTime lastModifiedAt;

    private Builder() {}

    public Builder withEmail(String email) {
      this.email = email;
      return this;
    }

    public Builder withHandle(String handle) {
      this.handle = handle;
      return this;
    }

    public Builder withCredentialId(String credentialId) {
      this.credentialId = credentialId;
      return this;
    }

    public Builder withPublicKey(byte[] publicKey) {
      this.publicKey = publicKey;
      return this;
    }

    public Builder withCounter(Long counter) {
      this.counter = counter;
      return this;
    }

    public Builder withAaguid(byte[] aaguid) {
      this.aaguid = aaguid;
      return this;
    }

    public Builder withCreatedAt(OffsetDateTime createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    public Builder withLastModifiedAt(OffsetDateTime lastModifiedAt) {
      this.lastModifiedAt = lastModifiedAt;
      return this;
    }

    /**
     * Builds a new {@link Authenticator} record.
     *
     * @return a new {@link Authenticator} record.
     */
    public Authenticator buildToCreate() {
      if (createdAt == null) {
        createdAt = DateTimeUtils.now();
      }

      return new Authenticator(
          email,
          handle,
          credentialId,
          publicKey,
          counter,
          aaguid,
          createdAt,
          lastModifiedAt
      );
    }

  }
}
