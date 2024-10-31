/*
 * Copyright (c) 2024 Singular™
 * SPDX-License-Identifier: MIT
 */

package ai.singlr.api;

import ai.singlr.core.Profile;
import io.helidon.common.media.type.MediaTypes;
import io.helidon.config.Config;
import io.helidon.http.HeaderNames;
import io.helidon.webclient.api.WebClient;
import org.junit.jupiter.api.BeforeAll;
import java.util.Map;

public abstract class BaseServiceTest {

  static WebClient webClient;

  @BeforeAll
  public static void startTheServer() {
    int port = Config.global().get("server.port").asInt().orElse(50080);
    Main.overrideConfigSource(v -> Map.of(
        "wan.id", "localhost",
        "wan.origin", "localhost:" + port
    ));
    Main.main(Profile.ci.name());

    webClient = WebClient.builder()
        .baseUri("http://localhost:" + port)
        .addHeader(HeaderNames.CONTENT_TYPE, MediaTypes.APPLICATION_JSON.text())
        .build();
  }
}
