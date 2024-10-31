/*
 * Copyright (c) 2024 Singular™
 * SPDX-License-Identifier: MIT
 */

package ai.singlr.api.service;

import ai.singlr.core.ErrorCode;
import ai.singlr.core.StringUtils;
import ai.singlr.core.Utils;
import ai.singlr.core.result.Result;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.helidon.http.HeaderName;
import io.helidon.http.HeaderNames;
import io.helidon.http.HeaderValues;
import io.helidon.http.media.jackson.JacksonRuntimeException;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the base service class that provides common functionality.
 */
public abstract class BaseService implements HttpService {

  private static final List<HeaderName> IP_HEADER_NAMES = List.of(
      HeaderNames.X_FORWARDED_FOR,
      HeaderNames.create("Proxy-Client-IP"),
      HeaderNames.create("WL-Proxy-Client-IP"),
      HeaderNames.create("HTTP_CLIENT_IP"),
      HeaderNames.create("HTTP_X_FORWARDED_FOR")
  );

  final Logger logger;

  public BaseService(Logger logger) {
    this.logger = logger;
  }

  void traceRequest(String remoteIp, String reqType, String message) {
    logger.info(
        String.format(
            "[%s] [%s] %s",
            remoteIp,
            reqType,
            message
        )
    );
  }

  String bestGuessRemoteIp(ServerRequest req) {
    var remoteHeader = IP_HEADER_NAMES.stream()
        .filter(headerName -> req.headers().contains(headerName))
        .map(headerName -> req.headers().get(headerName))
        .filter(ip -> {
          var fullValues = ip.values();
          return fullValues != null && !fullValues.isEmpty();
        })
        .findFirst();

    if (remoteHeader.isPresent()) {
      return remoteHeader.get().values();
    }

    return req.remotePeer().host();
  }

  void processResult(Result<?> result, ServerResponse resp, String logMessagePrefix) {
    if (result.isSuccess()) {
      resp
          .status(result.code())
          .header(HeaderNames.CONTENT_TYPE, HeaderValues.CONTENT_TYPE_JSON.values())
          .send(result.value());

    } else {
      processErrors(result, resp, logMessagePrefix);
    }
  }

  void processErrors(Result<?> result, ServerResponse resp, String logMessagePrefix) {
    var cause = result.cause();
    if (cause != null) {
      if (cause.getCause() instanceof JacksonException) {
        logger.fine(
            String.format("[%s] %s",
                logMessagePrefix,
                cause.getMessage()));
        sendInvalidError(resp, "A valid JSON must be specified");

      } else if (cause.getCause() instanceof IllegalArgumentException ex) {
        logger.fine(
            String.format("[%s] %s",
                logMessagePrefix,
                ex.getMessage()));
        sendInvalidError(resp, ex.getMessage());

      } else {
        // We expect this to be a bad error. So let's log it as such.
        // TODO: Add a request ID to track the flow and help support issues.
        logger.log(
            Level.SEVERE,
            String.format("[%s] Unable to process the request", logMessagePrefix),
            cause
        );
        sendInternalError(resp);
      }
    } else {
      if (ErrorCode.INVALID == result.errorCode()) {
        sendInvalidError(resp, result.errorMessage());
        return;
      }

      if (ErrorCode.NOT_FOUND == result.errorCode()) {
        sendNotFoundError(resp, result.errorMessage());
        return;
      }

      if (ErrorCode.NOT_AUTHORIZED == result.errorCode()) {
        sendNotAuthorizedError(resp, result.errorMessage());
        return;
      }

      // We expect this to be a bad error. So let's log it as such.
      // TODO: Add a request ID to track the flow and help support issues.
      logger.log(
          Level.SEVERE,
          String.format("[%s] Unable to process the request. [%s]", logMessagePrefix, result.errorMessage())
      );
      sendInternalError(resp);
    }
  }

  protected static <D> Optional<D> decodeAs(ServerRequest req, ServerResponse resp, Class<D> clazz) {
    try {
      D value = req.content().as(clazz);
      return Optional.of(value);

    } catch (JacksonRuntimeException | NullPointerException ex) {
      sendInvalidError(resp, "A valid JSON should be specified");
      return Optional.empty();
    }
  }

  protected static <D> Optional<D> decodeAs(JsonNode json, ServerResponse resp, Class<D> clazz) {
    try {
      D value = Utils.mapper().convertValue(json, clazz);
      return Optional.of(value);

    } catch (JacksonRuntimeException | NullPointerException | IllegalArgumentException ex) {
      sendInvalidError(resp, "A valid JSON should be specified");
      return Optional.empty();
    }
  }

  protected static <D> Optional<D> decodeAs(String json, ServerResponse resp, Class<D> clazz) {
    try {
      D value = Utils.mapper().readValue(json, clazz);
      return Optional.of(value);

    } catch (JacksonRuntimeException | NullPointerException | JsonProcessingException ex) {
      sendInvalidError(resp, "A valid JSON should be specified");
      return Optional.empty();
    }
  }

  protected static void send201(ServerResponse resp) {
    sendSuccess(resp, 201);
  }

  protected static void send202(ServerResponse resp) {
    sendSuccess(resp, 202);
  }

  protected static void sendSuccess(ServerResponse resp, int code) {
    resp
        .status(code)
        .header(HeaderNames.CONTENT_TYPE, HeaderValues.CONTENT_TYPE_JSON.values())
        .send(Result.success(true));
  }

  protected static void sendInvalidError(ServerResponse resp, String message) {
    sendError(resp, message, ErrorCode.INVALID);
  }

  protected static void sendNotFoundError(ServerResponse resp, String message) {
    sendError(resp, message, ErrorCode.NOT_FOUND);
  }

  static void sendInternalError(ServerResponse resp) {
    sendError(resp, "Sorry, something went wrong. Please try later.", ErrorCode.INTERNAL);
  }

  static void sendNotAuthorizedError(ServerResponse resp) {
    sendError(resp, "UnAuthorized", ErrorCode.NOT_AUTHORIZED);
  }

  static void sendNotAuthorizedError(ServerResponse resp, String message) {
    sendError(resp, message, ErrorCode.NOT_AUTHORIZED);
  }

  static void sendError(ServerResponse resp, String message, ErrorCode errorCode) {
    resp
        .status(errorCode.httpCode())
        .header(HeaderNames.CONTENT_TYPE, HeaderValues.CONTENT_TYPE_JSON.values())
        .send(new Result<>(errorCode, message, null));
  }

  protected Optional<UUID> tryParseId(ServerResponse resp, String id, String errorMessage) {
    if (StringUtils.isBlank(id)) {
      sendInvalidError(resp, errorMessage);
      return Optional.empty();
    }

    try {
      return Optional.of(UUID.fromString(id));

    } catch (IllegalArgumentException ex) {
      sendInvalidError(resp, errorMessage);
      return Optional.empty();
    }
  }

  protected Optional<String> tryParseHeader(ServerRequest req, String headerName) {
    return req.headers().value(HeaderNames.create(headerName));
  }
}
