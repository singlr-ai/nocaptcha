/*
 * Copyright (c) 2024 Singular™
 * SPDX-License-Identifier: MIT
 */

package ai.singlr.core;

import java.util.Base64;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Utility class that handles common string functions.
 */
public class StringUtils {

  private static final Pattern INVALID_NAME =
      Pattern.compile("[~`<>?/:;{}+=&!@#%\\[\\]()|\\\\.,^$'\" ]+");

  private static final Base64.Encoder BASE64URL = Base64.getUrlEncoder().withoutPadding();

  private static final Base64.Decoder BASE64URL_DECODER = Base64.getUrlDecoder();

  /**
   * No need to instantiate this object.
   */
  private StringUtils() {}

  /**
   * Checks to ensure that the given name does not contain invalid characters.
   *
   * @param name the name to test.
   * @return {@code true} if the given name is a invalid. {@code false} otherwise.
   */
  public static boolean isInvalidName(String name) {
    if (name == null || name.isEmpty()) {
      return true;
    }

    var matcher = INVALID_NAME.matcher(name);
    return matcher.find();
  }

  /**
   * Checks that the specified string is not {@code null} and not blank (is empty or contains only
   * {@linkplain Character#isWhitespace(int) white space} codepoints. If the conditions are not met,
   * throws a customized {@link NullPointerException} or {@link IllegalArgumentException}.
   *
   * @apiNote ensure that the {@code errorMessage} argument is not blank. The method does
   *          not validate the message.
   *
   * @param value the string to check.
   * @param errorMessage the customized error message to throw when the conditions are not met.
   */
  public static String requireNonBlank(String value, String errorMessage) {
    Objects.requireNonNull(value, errorMessage);
    if (value.isBlank()) {
      throw new IllegalArgumentException(errorMessage);
    }

    return value;
  }

  public static boolean isEmpty(String value) {
    return value == null || value.isEmpty();
  }

  public static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  /**
   * Checks to see if the given value is part of the accepted list of enum values.
   *
   * @param value the value to check
   * @param acceptedValues the set of accepted enum values.
   * @return {@code true} if the given value is a valid enum. {@code false} otherwise.
   */
  public static boolean isValidEnum(String value, Set<String> acceptedValues) {
    if (StringUtils.isBlank(value)) {
      return false;
    }

    return acceptedValues.contains(value);
  }

  /**
   * Limits the character count to the specified limit.
   *
   * @param input the input string to check.
   * @param inclusiveLength the inclusive length of the string.
   * @return the input string if the length is within the limit. Otherwise, returns a substring of the input string.
   */
  public static String limitCharacters(String input, int inclusiveLength) {
    if (input != null && input.length() > inclusiveLength) {
      return input.substring(0, inclusiveLength);
    }
    return input;
  }

  public static String base64UrlEncode(byte[] bytes) {
    return BASE64URL.encodeToString(bytes);
  }
}
