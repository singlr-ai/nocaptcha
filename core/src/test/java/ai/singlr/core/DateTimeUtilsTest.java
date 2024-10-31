/*
 * Copyright (c) 2024 Singular™
 * SPDX-License-Identifier: MIT
 */

package ai.singlr.core;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DateTimeUtilsTest {
  @Test
  void testVersionAndVariant() {
    UUID uuid = DateTimeUtils.newId();

    // Check version (should be 7)
    assertEquals(7, uuid.version());

    // Check variant (should be 2, RFC-4122)
    assertEquals(2, uuid.variant());
  }

  @Test
  void testTimestampMonotonicity() {
    Instant start = Instant.now();
    List<UUID> uuids = new ArrayList<>();
    for (int i = 0; i < 1000; i++) {
      uuids.add(DateTimeUtils.newId());
    }
    Instant end = Instant.now();
    long durationMillis = Duration.between(start, end).toMillis();
    System.out.printf("Generated 1000 UUIDs in %d ms%n", durationMillis);

    // Verify ordering
    List<UUID> sorted = new ArrayList<>(uuids);
    Collections.sort(sorted);
    assertEquals(uuids, sorted, "UUIDs should be naturally ordered by generation time");
  }

  @Test
  void testTimestampAccuracy() {
    long beforeGeneration = Instant.now().toEpochMilli();
    UUID uuid = DateTimeUtils.newId();
    long afterGeneration = Instant.now().toEpochMilli();

    // Extract timestamp from UUID (first 48 bits)
    long uuidTimestamp = uuid.getMostSignificantBits() >>> 16;

    assertTrue(uuidTimestamp >= beforeGeneration,
        "UUID timestamp should not be before generation time");
    assertTrue(uuidTimestamp <= afterGeneration,
        "UUID timestamp should not be after generation time");
  }

  @Test
  void testSubsecondField() {
    UUID uuid = DateTimeUtils.newId();

    // Extract subsec field (12 bits after timestamp)
    long subsec = (uuid.getMostSignificantBits() >> 4) & 0xFFF;

    // Subsec should be 12 bits
    assertTrue(subsec < 4096, "Subsec field should be 12 bits");
  }

  @RepeatedTest(1000)
  void testUniqueness() {
    Set<UUID> uuids = new HashSet<>();
    for (int i = 0; i < 1000; i++) {
      UUID uuid = DateTimeUtils.newId();
      assertTrue(uuids.add(uuid),
          "Generated UUIDs should be unique");
    }
  }

  @Test
  void testBitStructure() {
    UUID uuid = DateTimeUtils.newId();
    long msb = uuid.getMostSignificantBits();
    long lsb = uuid.getLeastSignificantBits();

    // Check if top 48 bits of MSB contain reasonable timestamp
    long timestamp = msb >>> 16;
    long currentTime = Instant.now().toEpochMilli();
    assertTrue(Math.abs(timestamp - currentTime) < 1000,
        "Timestamp should be close to current time");

    // Check version bits (bits 12-15, should be 7)
    assertEquals(7, (msb >> 12) & 0xF);

    // Check variant bits (should be binary 10)
    assertEquals(0x8000_0000_0000_0000L & lsb, 0x8000_0000_0000_0000L);
    assertEquals(0x4000_0000_0000_0000L & lsb, 0);
  }

  @Test
  void testRandomnessDistribution() {
    // Generate large number of UUIDs and check random bits distribution
    int sampleSize = 10000;
    long[] randomBits = new long[sampleSize];

    for (int i = 0; i < sampleSize; i++) {
      UUID uuid = DateTimeUtils.newId();
      // Extract the 62 random bits
      randomBits[i] = uuid.getLeastSignificantBits() & 0x3FFF_FFFF_FFFF_FFFFL;
    }

    // Check that random bits are well distributed
    // (simple check: count zeros and ones in LSB)
    int zeros = 0;
    int ones = 0;
    for (long bits : randomBits) {
      if ((bits & 1) == 0) zeros++;
      else ones++;
    }

    // Chi-square test for randomness (simplified)
    double expected = sampleSize / 2.0;
    double chiSquare = Math.pow(zeros - expected, 2) / expected +
        Math.pow(ones - expected, 2) / expected;

    assertTrue(chiSquare < 3.841, // 95% confidence for 1 degree of freedom
        "Random bits distribution should pass chi-square test");
  }
}
