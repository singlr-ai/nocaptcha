/*
 * Copyright (c) 2024 Singular™
 * SPDX-License-Identifier: MIT
 */

package ai.singlr.api;

import ai.singlr.api.service.NoCaptchaService;
import ai.singlr.core.Profile;
import ai.singlr.core.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.helidon.config.Config;
import io.helidon.http.media.jackson.JacksonSupport;
import io.helidon.http.media.multipart.MultiPartSupport;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.accesslog.AccessLogFeature;
import io.helidon.webserver.cors.CorsSupport;
import io.helidon.webserver.http.HttpRouting;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Runs the web server and registers the API end points.
 */
public class ApiServer {

  private static final String API_VERSION_V1 = "/v1";

  private static final Logger LOGGER = Logger.getLogger(ApiServer.class.getName());

  private Profile profile;

  /**
   * Starts the web server.
   */
  public void start() {
    var objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    Utils.init(objectMapper);
    this.profile = Utils.profile();

    if (Profile.dev == profile) {
      Map<String, String> configMap = Config.global().asMap().get();
      final var printConfig = new StringBuilder();
      configMap.keySet().stream().sorted().forEach(
          key -> printConfig.append(key).append(" = ").append(configMap.get(key)).append("\n")
      );
      LOGGER.info("🔧 Configuration:\n" + printConfig);
    }

    startApiServer(objectMapper);
  }

  private void startApiServer(ObjectMapper objectMapper) {
    WebServer server = WebServer.builder()
        .config(Config.global().get("server"))
        .mediaContext(it -> it
            .mediaSupportsDiscoverServices(false)
            .addMediaSupport(MultiPartSupport.create(Config.global()))
            .addMediaSupport(JacksonSupport.create(objectMapper))
        )
        .routing(this::setupApiRoutes)
        .addFeature(AccessLogFeature.builder().sockets(Set.of("@default")).build())
        .putSocket("observe", socket -> socket
            .port(8085)
            .routing(routing -> routing
                .get("/health/ready", (req, res) -> res.send("UP"))
                .get("/health/live", (req, res) -> res.send("UP"))
                .get()
            )
        )
        .build()
        .start();

    LOGGER.info("✅ Singular nocaptcha is up at http://localhost:" + server.port());
  }

  private void setupApiRoutes(HttpRouting.Builder routing) {
    var config = Config.global();
    var corsConfig = config.get("restrictive-cors");
    var corsSupport = CorsSupport.builder()
        .allowMethods(corsConfig.get("allow-methods").asList(String.class).get().toArray(new String[0]))
        .allowOrigins(corsConfig.get("allow-origins").asList(String.class).get().toArray(new String[0]))
        .allowCredentials(true)
        .build();

    routing.register(
        String.format("%s/nocaptcha", API_VERSION_V1),
        corsSupport,
        new NoCaptchaService()
    );
  }
}
