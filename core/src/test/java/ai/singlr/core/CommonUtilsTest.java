/*
 * Copyright (c) 2024 Singular™
 * SPDX-License-Identifier: MIT
 */

package ai.singlr.core;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

import org.apache.commons.validator.routines.EmailValidator;
import org.junit.jupiter.api.Test;

public class CommonUtilsTest {

  @Test
  public void emailTest() {
    var badEmails = List.of(
        "plainaddress", // missing '@' and domain
        "john.doe@com", // missing top-level domain
        "john.doe@.com", // missing domain
        "john.doe@com.", // missing top-level domain
        "john.doe@com..", // double dots in top-level domain
        "john@ex#ample.com", // special character in domain
        "john@ex ample.com", // space in domain
        "john@example..com", // double dots in domain
        "john@123.456.789.0", // IP address in domain
        " valid@example.com "
    );

    var validator = EmailValidator.getInstance();
    for (String email : badEmails) {
      assertFalse(validator.isValid(email));
    }
  }
}
