/*
 * Copyright (c) 2024 Singular™
 * SPDX-License-Identifier: MIT
 */

package ai.singlr.core.result;

/**
 * Represents an arbitrary result of a validation operation.
 *
 * @param <V> the type of the value when the result represents a successful validation.
 */
public final class ValidationResult<V> {
  private final V value;
  private final String errorMessage;

  public ValidationResult(V value) {
    this.value = value;
    this.errorMessage = null;
  }

  public ValidationResult(String errorMessage) {
    this.value = null;
    this.errorMessage = errorMessage;
  }

  public V value() {
    return value;
  }

  public String errorMessage() {
    return errorMessage;
  }

  public boolean isValid() {
    return value != null;
  }

  public boolean isInvalid() {
    return errorMessage != null;
  }
}
