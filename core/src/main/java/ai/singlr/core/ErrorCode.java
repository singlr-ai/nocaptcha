/*
 * Copyright (c) 2024 Singular™
 * SPDX-License-Identifier: MIT
 */

package ai.singlr.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an error that holds a generic message, a custom code, and an associated HTTP code.
 */
public final class ErrorCode {

  private static final Map<String, ErrorCode> REGISTRY;

  public static final ErrorCode CONFLICT;
  public static final ErrorCode INVALID;
  public static final ErrorCode NOT_AUTHORIZED;
  public static final ErrorCode FORBIDDEN;
  public static final ErrorCode NOT_FOUND;
  public static final ErrorCode TOO_EARLY;
  public static final ErrorCode INTERNAL;

  static {
    REGISTRY = new HashMap<>();
    INVALID = register("SINGULAR_400", 400, "Potential malformed request");
    NOT_AUTHORIZED = register("SINGULAR_401", 401, "Unauthorized");
    FORBIDDEN = register("SINGULAR_403", 403, "Forbidden");
    NOT_FOUND = register("SINGULAR_404", 404, "Resource not found");
    CONFLICT = register("SINGULAR_409", 409, "Resource already exists");
    TOO_EARLY = register("SINGULAR_425", 425, "Request too early");
    INTERNAL = register("SINGULAR_500", 500, "Internal error");
  }

  private final String code;

  @JsonProperty
  private final int httpCode;

  @JsonProperty
  private final String httpMessage;

  private ErrorCode(String code, int httpCode, String httpMessage) {
    this.code = StringUtils.requireNonBlank(code, "'code' must be specified");
    this.httpMessage = StringUtils.requireNonBlank(httpMessage, "'httpMessage' must be specified");

    if (httpCode < 100 || httpCode > 510) {
      throw new IllegalArgumentException("A valid HTTP code must be specified");
    }
    this.httpCode = httpCode;
  }

  public String code() {
    return code;
  }

  public int httpCode() {
    return httpCode;
  }

  public String message() {
    return httpMessage;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    var that = (ErrorCode) obj;
    return httpCode == that.httpCode && code.equals(that.code);
  }

  @Override
  public int hashCode() {
    return code.hashCode();
  }

  /**
   * Registers a new {@link ErrorCode} after ensuring that the {@code code} has not been
   * previously used.
   *
   * @param code the arbitrary platform specific code.
   * @param httpCode the HTTP code that maps to this error.
   * @param httpMessage the generic HTTP message.
   * @return the registered {@link ErrorCode}.
   */
  public static ErrorCode register(String code, int httpCode, String httpMessage) {
    var error = new ErrorCode(code, httpCode, httpMessage);

    if (REGISTRY.containsKey(error.code)) {
      throw new RuntimeException("Error code has already been registered: " + code);
    }

    REGISTRY.put(error.code, error);

    return error;
  }
}
