/*
 * Copyright (c) 2024 Singular™
 * SPDX-License-Identifier: MIT
 */

package ai.singlr.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StringUtilsTest {

  @Test
  public void stringNullTest() {
    Exception thrown = assertThrows(NullPointerException.class, () -> {
      StringUtils.requireNonBlank(null, "null test");
    });

    assertEquals("null test", thrown.getMessage());
  }

  @Test
  public void stringBlankTest() {
    Exception thrown = assertThrows(IllegalArgumentException.class, () -> {
      StringUtils.requireNonBlank("   ", "blank test");
    });

    assertEquals("blank test", thrown.getMessage());
  }

  @Test
  public void stringNonBlankTest() {
    assertDoesNotThrow(() -> {
      StringUtils.requireNonBlank(" abc  ", "does not matter");
    });
  }

  @Test
  public void invalidNameTest() {
    assertTrue(StringUtils.isInvalidName("~raj"));
    assertTrue(StringUtils.isInvalidName("`crazy"));
    assertTrue(StringUtils.isInvalidName("what<now"));
    assertTrue(StringUtils.isInvalidName(">now"));
    assertTrue(StringUtils.isInvalidName("sayWhat?"));
    assertTrue(StringUtils.isInvalidName("this/that"));
    assertTrue(StringUtils.isInvalidName(":t1"));
    assertTrue(StringUtils.isInvalidName("t2;"));
    assertTrue(StringUtils.isInvalidName("{t3"));
    assertTrue(StringUtils.isInvalidName("t4}"));
    assertTrue(StringUtils.isInvalidName("t+5"));
    assertTrue(StringUtils.isInvalidName("=t6"));
    assertTrue(StringUtils.isInvalidName("t&7"));
    assertTrue(StringUtils.isInvalidName("yay!"));
    assertTrue(StringUtils.isInvalidName("s@m"));
    assertTrue(StringUtils.isInvalidName("#fintech"));
    assertTrue(StringUtils.isInvalidName("t8%"));
    assertTrue(StringUtils.isInvalidName("[t9"));
    assertTrue(StringUtils.isInvalidName("t10]"));
    assertTrue(StringUtils.isInvalidName("(t11"));
    assertTrue(StringUtils.isInvalidName("t)12"));
    assertTrue(StringUtils.isInvalidName("t13|"));
    assertTrue(StringUtils.isInvalidName("t\\14"));
    assertTrue(StringUtils.isInvalidName("t15."));
    assertTrue(StringUtils.isInvalidName("t,16"));
    assertTrue(StringUtils.isInvalidName("^t17"));
    assertTrue(StringUtils.isInvalidName("^t18$"));
    assertTrue(StringUtils.isInvalidName("t19'"));
    assertTrue(StringUtils.isInvalidName("\"t20"));
    assertTrue(StringUtils.isInvalidName("@t21@"));
    assertTrue(StringUtils.isInvalidName("t 22"));


    assertFalse(StringUtils.isInvalidName("raj_kiran"));
    assertFalse(StringUtils.isInvalidName("rajkiran1001"));
    assertFalse(StringUtils.isInvalidName("ud-chan"));
    assertFalse(StringUtils.isInvalidName("SUMESH"));
    assertFalse(StringUtils.isInvalidName("Marlène"));
  }
}
