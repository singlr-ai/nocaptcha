/*
 * Copyright (c) 2024 Singular™
 * SPDX-License-Identifier: MIT
 */

package ai.singlr.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.helidon.config.Config;
import java.util.Arrays;

/**
 * Common utility methods.
 */
public class Utils {

  public static final String NOCAPTCHA_PROFILE = "NOCAPTCHA_PROFILE";
  public static final String PROFILE = "profile";

  private Utils() {}

  private static ObjectNode emptyJson;
  private static ObjectMapper theMapper;
  private static Profile profile;

  /**
   * Initialize the utility class.
   */
  public static void init(ObjectMapper mapper) {
    theMapper = mapper;
    emptyJson = mapper.createObjectNode();
  }

  public static JsonNode emptyJson() {
    return emptyJson;
  }

  public static ObjectMapper mapper() {
    return theMapper;
  }

  public static ObjectNode newJson() {
    return theMapper.createObjectNode();
  }

  public static ArrayNode newJsonArray() {
    return theMapper.createArrayNode();
  }

  /**
   * Returns the current profile.
   */
  public static Profile profile() {
    if (profile != null) {
      return profile;
    }
    profile = Profile.valueOf(Config.global().get(PROFILE).asString().get().toLowerCase());
    return profile;
  }

  /**
   * Parses the profile from the command line arguments or the environment variable.
   */
  public static Profile parseProfile(String ...args) {
    Profile profile = Profile.dev;

    if (args != null && args.length > 0) {
      try {
        profile = Profile.valueOf(args[0].toLowerCase());

      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(
            "Please set NOCAPTCHA_PROFILE env to one of " + Arrays.toString(Profile.values()) + " values.");
      }
    } else {
      String profileRaw = System.getenv(NOCAPTCHA_PROFILE);
      if (!StringUtils.isBlank(profileRaw)) {
        try {
          profile = Profile.valueOf(profileRaw.toLowerCase());

        } catch (IllegalArgumentException e) {
          throw new IllegalArgumentException(
              "Please set NOCAPTCHA_PROFILE env to one of " + Arrays.toString(Profile.values()) + " values.");
        }
      }
    }

    return profile;
  }
}
