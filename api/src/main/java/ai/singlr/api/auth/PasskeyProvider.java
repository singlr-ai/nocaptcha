/*
 * Copyright (c) 2024 Singular™
 * SPDX-License-Identifier: MIT
 */

package ai.singlr.api.auth;

import ai.singlr.core.DateTimeUtils;
import ai.singlr.core.Utils;
import ai.singlr.core.result.Result;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.AuthenticatorAttachment;
import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import com.yubico.webauthn.data.ResidentKeyRequirement;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.data.UserVerificationRequirement;
import com.yubico.webauthn.data.exception.Base64UrlException;
import com.yubico.webauthn.exception.RegistrationFailedException;
import io.helidon.config.Config;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Passkey helper class.
 */
public class PasskeyProvider implements CredentialRepository {

  private static final Logger LOGGER = Logger.getLogger(PasskeyProvider.class.getName());

  private final RelyingParty rp;
  private final long timeout;

  /**
   * Handles the webAuthN registration and assertion process.
   */
  public PasskeyProvider(Config wanConfig) {
    Set<String> origins;
    if (wanConfig.get("origins").exists()) {
      var rawOrigins = wanConfig.get("origins").asString().get();
      origins = Set.of(rawOrigins.split(","));
    } else {
      throw new IllegalArgumentException("'origins' must be specified");
    }

    String id;
    if (wanConfig.get("id").exists()) {
      id = wanConfig.get("id").asString().get();
    } else {
      throw new IllegalArgumentException("'id' must be specified");
    }

    String name;
    if (wanConfig.get("name").exists()) {
      name = wanConfig.get("name").asString().get();
    } else {
      throw new IllegalArgumentException("'name' must be specified");
    }

    if (wanConfig.get("timeout").exists()) {
      timeout = wanConfig.get("timeout").asLong().get();
    } else {
      timeout = 100000;
    }

    RelyingPartyIdentity rpIdentity = RelyingPartyIdentity.builder()
        .id(id)
        .name(name)
        .build();

    rp = RelyingParty.builder()
        .identity(rpIdentity)
        .credentialRepository(this)
        .origins(origins)
        .build();
  }

  /**
   * Start registration process for CAPTCHA purposes.
   *
   * @return the newly minted {@link PublicKeyCredentialCreationOptions}.
   */
  public PublicKeyCredentialCreationOptions startCaptcha(String id)
      throws Base64UrlException {
    var userHandle = Base64.getUrlEncoder().encodeToString(
        DateTimeUtils.newId().toString().getBytes(StandardCharsets.UTF_8)
    );
    var identity = UserIdentity.builder()
        .name(id)
        .displayName(id)
        .id(ByteArray.fromBase64Url(userHandle))
        .build();

    var options = StartRegistrationOptions.builder()
        .user(identity)
        .authenticatorSelection(AuthenticatorSelectionCriteria
            .builder()
            .authenticatorAttachment(AuthenticatorAttachment.PLATFORM)
            .userVerification(UserVerificationRequirement.REQUIRED)
            .residentKey(ResidentKeyRequirement.REQUIRED)
            .build()
        )
        .timeout(timeout)
        .build();

    return rp.startRegistration(options);
  }

  /**
   * Finish registration process.
   *
   * @return the registration result.
   * @throws IOException if there is an error parsing the public key credential.
   * @throws RegistrationFailedException if the registration fails.
   */
  public Result<RegistrationResult> completeCaptcha(
      String pubKeyCredJsonFromClient,
      PublicKeyCredentialCreationOptions pubKeyCredFromServer)
      throws IOException, RegistrationFailedException {
    PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> pkc =
        PublicKeyCredential.parseRegistrationResponseJson(pubKeyCredJsonFromClient);

    // Handle android quirks.
    var clientOrigin = pkc.getResponse().getClientData().getOrigin();
    if (clientOrigin.startsWith("android:apk-key-hash:")) {
      // Recreate the clientJson with the regular origin.
      try {
        var collectedData = pkc.getResponse().getClientData();
        setFieldValue(collectedData, "origin", rp.getOrigins().iterator().next());

      } catch (NoSuchFieldException | IllegalAccessException ex) {
        LOGGER.log(Level.SEVERE, "Failed to set origin", ex);
      }
    }

    FinishRegistrationOptions options = FinishRegistrationOptions.builder()
        .request(pubKeyCredFromServer)
        .response(pkc)
        .build();
    RegistrationResult result = rp.finishRegistration(options);
    return Result.success(result);
  }

  @Override
  public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String id) {
    // TODO: Introduce persistence if needed, although the concept here is disposable passkeys.
    return Set.of();
  }

  @Override
  public Optional<ByteArray> getUserHandleForUsername(String id) {
    // TODO: Introduce persistence if needed, although the concept here is disposable passkeys.
    return Optional.empty();
  }

  @Override
  public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
    // TODO: Introduce persistence if needed, although the concept here is disposable passkeys.
    return Optional.empty();
  }

  @Override
  public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
    // TODO: Introduce persistence if needed, although the concept here is disposable passkeys.
    return Optional.empty();
  }

  @Override
  public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
    // TODO: Introduce persistence if needed, although the concept here is disposable passkeys.
    return Set.of();
  }

  private static void setFieldValue(Object object, String fieldName, Object valueTobeSet)
      throws NoSuchFieldException, IllegalAccessException {
    Field field = object.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(object, valueTobeSet);
  }
}
