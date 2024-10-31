/*
 * Copyright (c) 2024 Singular™
 * SPDX-License-Identifier: MIT
 */

package ai.singlr.core.result;

import ai.singlr.core.ErrorCode;

/**
 * A builder that can be used to create a {@link Result}.
 *
 * @param <V> the type of the {@link Result}.
 */
public final class ResultBuilder<V> {

  private V value;
  private int code;
  private ErrorCode errorCode;
  private String appMessage;
  private Throwable cause;

  /**
   * Build a successful result with a value and the success code.
   *
   * @param value the value of the successful result.
   * @param code the success code.
   * @return the builder instance for fluent building.
   */
  public ResultBuilder<V> withSuccess(V value, int code) {
    this.value = value;
    this.code = code;
    return this;
  }

  public ResultBuilder<V> withErrorCode(ErrorCode errorCode) {
    this.errorCode = errorCode;
    return this;
  }

  public ResultBuilder<V> withAppMessage(String appMessage) {
    this.appMessage = appMessage;
    return this;
  }

  public ResultBuilder<V> withCause(Throwable cause) {
    this.cause = cause;
    return this;
  }

  /**
   * Creates a new {@link Result} based on what was set.
   *
   * @return a new {@link Result} based on what was set.
   */
  public Result<V> build() {
    if (errorCode != null) {
      return new Result<>(errorCode, appMessage, cause);
    }

    if (value != null) {
      return new Result<>(value, code);
    }

    throw new IllegalArgumentException("A success or a failure result must be built");
  }
}
