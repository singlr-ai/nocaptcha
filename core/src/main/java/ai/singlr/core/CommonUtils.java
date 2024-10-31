/*
 * Copyright (c) 2024 Singular™
 * SPDX-License-Identifier: MIT
 */

package ai.singlr.core;

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.validator.routines.UrlValidator;

/**
 * Utility class that handles common functions.
 */
public class CommonUtils {

  private static final UrlValidator URL_VALIDATOR = new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS);

  /**
   * No need to instantiate this object.
   */
  private CommonUtils() {}

  /**
   * Loops through the available enum constants for the given enum class and provides a set of
   * valid values for quick validations.
   *
   * @param enumClass the enum class to probe.
   * @param <E> the type of enum class.
   * @return a set of valid enum values for quick validations.
   */
  public static <E extends Enum<E>> Set<String> acceptedValues(Class<E> enumClass) {
    var values = new HashSet<String>();
    for (var enumType : enumClass.getEnumConstants()) {
      String name = enumType.name();
      values.add(name);
    }

    return values;
  }

  /**
   * Checks to see if the given value represents a valid url address.
   *
   * @param value the value to check.
   * @return {@code true} if the url is valid. {@code false} otherwise.
   */
  public static boolean isValidUrl(String value) {
    return URL_VALIDATOR.isValid(value);
  }
}
