/*
 * Copyright (c) 2024 Singular™
 * SPDX-License-Identifier: MIT
 */

package ai.singlr.api;

import ai.singlr.api.request.PasskeyCaptchaRequest;
import ai.singlr.core.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NoCaptchaServiceTest extends BaseServiceTest {

  @Test
  @Order(1)
  public void noCaptchaStartTest() throws Exception {
    var passkeyReq = new PasskeyCaptchaRequest("test@example.com", Utils.newJson());
    var body = Utils.mapper().writeValueAsString(passkeyReq);
    try (var response = webClient.post()
        .path("/v1/nocaptcha/start").submit(body)) {
      assertEquals(201, response.status().code());
      var result = response.as(JsonNode.class);
      assertTrue(result.has(Constants.PUB_KEY_CRED_OPTS));
    }
  }
}
