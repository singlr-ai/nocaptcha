/*
 * Copyright (c) 2024 Singular™
 * SPDX-License-Identifier: MIT
 */

package ai.singlr.api.service;

import ai.singlr.api.Constants;
import ai.singlr.api.auth.PasskeyProvider;
import ai.singlr.api.request.PasskeyCaptchaRequest;
import ai.singlr.core.StringUtils;
import ai.singlr.core.Utils;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import io.helidon.config.Config;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides registration and authentication functionality.
 */
public class NoCaptchaService extends BaseService {

  private static final Logger LOGGER = Logger.getLogger(NoCaptchaService.class.getName());

  private final PasskeyProvider passkeyProvider;

  // TODO: Clean-up this map after a certain time.
  //       This is a naive way of handling temporary time-based, passkeys
  private final Map<String, PublicKeyCredentialCreationOptions> passkeyCaptchaMap;
  
  /**
   * Create a new instance of the service.
   */
  public NoCaptchaService() {
    super(LOGGER);
    this.passkeyProvider = new PasskeyProvider(Config.global().get("wan"));
    this.passkeyCaptchaMap = new HashMap<>(100);
  }

  @Override
  public void routing(HttpRules rules) {
    rules.post("/start", this::passkeyStartHandler);
    rules.put("/complete", this::passkeyCompleteHandler);
  }

  private void passkeyStartHandler(ServerRequest req, ServerResponse resp) {
    var reqOpt = decodeAs(req, resp, PasskeyCaptchaRequest.class);
    if (reqOpt.isEmpty()) {
      return;
    }

    if (StringUtils.isBlank(reqOpt.get().id())) {
      sendInvalidError(resp, "ID is required.");
      return;
    }

    var passkeyReq = reqOpt.get();

    String pubKeyCredOptions;
    try {
      var credentialCreationOptions = passkeyProvider.startCaptcha(passkeyReq.id());
      pubKeyCredOptions = credentialCreationOptions.toCredentialsCreateJson();
      passkeyCaptchaMap.put(credentialCreationOptions.getUser().getId().getBase64Url(), credentialCreationOptions);
      passkeyCaptchaMap.keySet().forEach(LOGGER::info);

    } catch (Exception ex) {
      LOGGER.log(Level.INFO, "Unable to create public key credential options", ex);
      sendInternalError(resp);
      return;
    }

    var json = Utils.newJson()
        .put(Constants.PUB_KEY_CRED_OPTS, pubKeyCredOptions);

    // TODO: Add policy headers
    resp.status(201).send(json);
  }

  private void passkeyCompleteHandler(ServerRequest req, ServerResponse resp) {
    var reqOpt = decodeAs(req, resp, PasskeyCaptchaRequest.class);
    if (reqOpt.isEmpty()) {
      return;
    }

    try {
      var pubKeyCredJsonFromServer = passkeyCaptchaMap.remove(reqOpt.get().id());
      if (pubKeyCredJsonFromServer == null) {
        sendInvalidError(resp, "Unable to recognize temporary passkey");
        return;
      }

      var registerResult = passkeyProvider.completeCaptcha(
          reqOpt.get().credsAsString(),
          pubKeyCredJsonFromServer
      );

      if (registerResult.isFailure()) {
        sendInvalidError(resp, registerResult.errorMessage());
        return;
      }

      var json = Utils.newJson();
      resp.status(202).send(json);

    } catch (Exception ex) {
      LOGGER.log(Level.SEVERE, "Unable to complete captcha", ex);
      sendInternalError(resp);
    }
  }
}
