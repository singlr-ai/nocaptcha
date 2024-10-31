/*
 * Copyright (c) 2024 Singular™
 * SPDX-License-Identifier: MIT
 */

package ai.singlr.core.result;

import ai.singlr.core.ErrorCode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an arbitrary result of an operation.
 *
 * @param <V> the type of the value when the result represents a successful operation.
 */
public final class Result<V> {

  @JsonProperty()
  private final V value;

  @JsonProperty
  private final int code;

  @JsonProperty
  private final String errorMessage;

  @JsonProperty
  private final ErrorCode errorCode;  

  @JsonIgnore
  private final Throwable cause;

  /**
   * Instantiates a new successful result.
   *
   * @param value the value of the result.
   */
  public Result(V value, int code) {
    this.value = value;
    this.code = code;
    this.errorMessage = null;
    this.errorCode = null;
    this.cause = null;
  }

  /**
   * Instantiates a new successful result.
   *
   * @param errorCode the error code.
   * @param errorMessage a user-friendly error message that can be displayed.
   * @param cause the cause of the error.
   */
  public Result(ErrorCode errorCode, String errorMessage, Throwable cause) {
    this.value = null;
    this.code = -1;
    this.errorMessage = errorMessage;
    this.errorCode = errorCode;
    this.cause = cause;
  }

  public Result(ErrorCode errorCode, String errorMessage) {
    this(errorCode, errorMessage, null);
  }

  public V value() {
    return value;
  }

  public int code() {
    return code;
  }

  public String errorMessage() {
    return errorMessage;
  }

  public ErrorCode errorCode() {
    return errorCode;
  }

  public Throwable cause() {
    return cause;
  }

  @JsonIgnore
  public boolean isFailure() {
    return errorCode != null;
  }

  @JsonIgnore
  public boolean isSuccess() {
    return value != null;
  }

  public static <T> Result<T> invalid(String errorMessage) {
    return new Result<>(ErrorCode.INVALID, errorMessage);
  }

  public static <T> Result<T> failure(String errorMessage) {
    return new Result<>(ErrorCode.INTERNAL, errorMessage);
  }

  public static <T> Result<T> success(T value) {
    return new Result<>(value, 200);
  }
}
