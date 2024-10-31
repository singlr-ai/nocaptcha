/*
 * Copyright (c) 2024 Singular™
 * SPDX-License-Identifier: MIT
 */

package ai.singlr.api.request;

import ai.singlr.core.Utils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Simple record to handle passkey based CAPTCHA requests.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PasskeyCaptchaRequest(
    String id,
    JsonNode pubKeyCredOpts) {

  /**
   *  Returns the public key credential options as a string.
   */
  @JsonIgnore
  public String credsAsString() {
    try {
      return Utils.mapper().writeValueAsString(pubKeyCredOpts);
    } catch (JsonProcessingException ignored) {
      return null;
    }
  }
}
