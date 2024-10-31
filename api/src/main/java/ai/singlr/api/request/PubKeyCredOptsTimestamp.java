/*
 * Copyright (c) 2024 Singular™
 * SPDX-License-Identifier: MIT
 */

package ai.singlr.api.request;

import ai.singlr.core.DateTimeUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents the epochTimestamp used to kind of make password based authentication more tamper resistant.
 */
public record PubKeyCredOptsTimestamp(long epochTimestamp) {

  @JsonIgnore
  public boolean isExpired(int expirySeconds) {

    return DateTimeUtils.now().toEpochSecond() - epochTimestamp > expirySeconds;
  }
}
