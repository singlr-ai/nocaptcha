/*
 * Copyright (c) 2024 Singular™
 * SPDX-License-Identifier: MIT
 */

package ai.singlr.core.dto;

import ai.singlr.core.DateTimeUtils;
import ai.singlr.core.StringUtils;
import ai.singlr.core.result.ValidationResult;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.apache.commons.validator.routines.EmailValidator;

/**
 * Represents a user (consumer account) data transfer object. This is not necessarily an entity.
 */
public record User(
    UUID id,
    String firstName,
    String lastName,
    String email,
    String mobileNumber,
    OffsetDateTime createdAt,
    OffsetDateTime lastModifiedAt,
    @JsonIgnore
    String pubKeyCredOptions
) {

  public String displayName() {
    return firstName + " " + lastName;
  }

  /**
   * Checks for issues with the user values and if everything is good, returns a
   * {@link ValidationResult} with a new {@link User} with validated and properly
   * assigned fields. If there are issues, returns {@link ValidationResult} with
   * specific validation errors.
   *
   * @param user the {@link User} to be validated.
   * @return a new {@link User} with validated and properly assigned fields.
   */
  public static ValidationResult<User> validate(User user) {
    // TODO: Should we validate all fields instead of being short circuited?
    if (user == null) {
      return new ValidationResult<>("User must be specified");
    }

    if (StringUtils.isBlank(user.firstName)) {
      return new ValidationResult<>("'firstName' must be specified");
    }

    if (user.firstName.length() > 70) {
      return new ValidationResult<>("'firstName' too long");
    }

    if (StringUtils.isBlank(user.lastName)) {
      return new ValidationResult<>("'lastName' must be specified");
    }

    if (user.lastName.length() > 70) {
      return new ValidationResult<>("'lastName' too long");
    }

    String email = null;
    if (!StringUtils.isBlank(user.email)) {
      email = user.email.trim().toLowerCase();
      if (!EmailValidator.getInstance().isValid(email)) {
        return new ValidationResult<>("Please enter a valid email address");
      }
    }

    if (user.createdAt == null) {
      return new ValidationResult<>("'createdAt' must be specified");
    }

    return new ValidationResult<>(
        new User(
            user.id,
            user.firstName.trim(),
            user.lastName.trim(),
            email,
            user.mobileNumber,
            user.createdAt,
            DateTimeUtils.now(),
            user.pubKeyCredOptions
        ));
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static Builder newBuilder(User user) {
    return new Builder(user);
  }

  /**
   * Builder used to create immutable {@link User} records (DTOs).
   */
  public static class Builder {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String mobileNumber;
    private OffsetDateTime createdAt;
    private OffsetDateTime lastModifiedAt;
    private String pubKeyCredOptions;

    private Builder() {}

    private Builder(User user) {
      this.id = user.id;
      this.firstName = user.firstName;
      this.lastName = user.lastName;
      this.email = user.email;
      this.mobileNumber = user.mobileNumber;
      this.createdAt = user.createdAt;
      this.lastModifiedAt = user.lastModifiedAt;
      this.pubKeyCredOptions = user.pubKeyCredOptions;
    }

    public Builder withId(UUID id) {
      this.id = id;
      return this;
    }

    public Builder withFirstName(String firstName) {
      this.firstName = firstName;
      return this;
    }

    public Builder withLastName(String lastName) {
      this.lastName = lastName;
      return this;
    }

    public Builder withEmail(String email) {
      this.email = email;
      return this;
    }

    public Builder withMobileNumber(String mobileNumber) {
      this.mobileNumber = mobileNumber;
      return this;
    }

    public Builder withPubKeyCredOptions(String pubKeyCredOptions) {
      this.pubKeyCredOptions = pubKeyCredOptions;
      return this;
    }

    /**
     * Creates a user that has NOT been validated yet but adds a few basic defaults.
     *
     *  @apiNote Never save this instance directly. Always use {@link #validate(User)}.
     *
     * @return a user instance that has NOT been fully validated and prepared.
     */
    public User build() {
      if (createdAt == null) {
        createdAt = DateTimeUtils.now();
      }

      return new User(
          id,
          firstName,
          lastName,
          email,
          mobileNumber,
          createdAt,
          lastModifiedAt,
          pubKeyCredOptions
      );
    }
  }
}
