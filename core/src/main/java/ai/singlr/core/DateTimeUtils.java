/*
 * Copyright (c) 2024 Singular™
 * SPDX-License-Identifier: MIT
 */

package ai.singlr.core;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility class that handles date and time functions.
 */
public final class DateTimeUtils {

  private static final SecureRandom SECURE_RANDOM = new SecureRandom();
  private static final long MAX_SUBSEC = 4096L; // 2^12

  private static final ZoneId UTC = ZoneId.of("UTC");

  private static final DateTimeFormatter FORMATTER = DateTimeFormatter
      .ofPattern("MMM dd, yyyy hh:mm 'UTC'")
      .withZone(UTC);

  private static final DateTimeFormatter FORMATTER_SHORT = DateTimeFormatter
      .ofPattern("MMM dd, yyyy")
      .withZone(UTC);

  private static final DateTimeFormatter FORMATTER_LONG = DateTimeFormatter
      .ofPattern("EEEE, MMMM dd, yyyy")
      .withZone(UTC);

  // Keep track of last timestamp and counter for monotonicity
  private static final AtomicLong lastTimestamp = new AtomicLong(0);
  private static final AtomicLong counter = new AtomicLong(0);

  /**
   * Private constructor to prevent instantiation of utility class.
   *
   * @throws UnsupportedOperationException if instantiation is attempted
   */
  private DateTimeUtils() {
    throw new UnsupportedOperationException("Utility class should not be instantiated");
  }

  /**
   * Returns the current time in UTC.
   *
   * @return Current time as OffsetDateTime in UTC
   */
  public static OffsetDateTime now() {
    return OffsetDateTime.now(Clock.systemUTC());
  }

  /**
   * Formats a datetime in standard UTC format (MMM dd, yyyy hh:mm UTC).
   *
   * @param dateTime the datetime to format.
   *
   * @return the formatted datetime string.
   * @throws NullPointerException if dateTime is null.
   */
  public static String toDisplayUtc(OffsetDateTime dateTime) {
    if (dateTime == null) {
      throw new NullPointerException("DateTime cannot be null");
    }
    return FORMATTER.format(dateTime);
  }

  /**
   * Formats a datetime in short UTC format (MMM dd, yyyy).
   *
   * @param dateTime the datetime to format.
   *
   * @return the formatted datetime string.
   * @throws NullPointerException if dateTime is null.
   */
  public static String toDisplayShortUtc(OffsetDateTime dateTime) {
    if (dateTime == null) {
      throw new NullPointerException("DateTime cannot be null");
    }
    return FORMATTER_SHORT.format(dateTime);
  }

  /**
   * Formats a datetime in long UTC format (EEEE, MMMM dd, yyyy).
   *
   * @param dateTime the datetime to format.
   *
   * @return the formatted datetime string.
   * @throws NullPointerException if dateTime is null.
   */
  public static String toDisplayLongUtc(OffsetDateTime dateTime) {
    if (dateTime == null) {
      throw new NullPointerException("DateTime cannot be null");
    }
    return FORMATTER_LONG.format(dateTime);
  }

  /**
   * Generates a UUID v7 according to the draft specification.
   * Format: |48 bits unix timestamp|12 bits subsec|4 bits version|64 bits LSB|
   * This implementation ensures monotonicity within the same millisecond and
   * uses SecureRandom for the random bits.
   *
   * @return A new UUID v7 instance
   */
  public static UUID newId() {
    Instant now = Instant.now();
    long milliseconds = now.toEpochMilli();

    // Ensure timestamp monotonicity
    long timestamp = lastTimestamp.updateAndGet(last -> {
      if (milliseconds > last) {
        counter.set(0);
        return milliseconds;
      }
      return last;
    });

    // Calculate subsec value using counter for same-millisecond monotonicity
    long subsec = counter.getAndIncrement() % MAX_SUBSEC;

    // Build MSB:
    // Timestamp in most significant 48 bits
    long msb = (timestamp << 16) | (subsec & 0xFFF);

    // Clear version bits and set to version 7
    msb = (msb & 0xFFFF_FFFF_FFFF_0FFFL) | (0x7L << 12);

    // Build LSB with variant bits and random data
    long lsb = 0x8000_0000_0000_0000L
        | (SECURE_RANDOM.nextLong() & 0x3FFF_FFFF_FFFF_FFFFL); // Random bits

    return new UUID(msb, lsb);
  }
}
