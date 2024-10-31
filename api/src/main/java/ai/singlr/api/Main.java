/*
 * Copyright (c) 2024 Singular™
 * SPDX-License-Identifier: MIT
 */

package ai.singlr.api;

import static io.helidon.config.ConfigSources.file;

import ai.singlr.core.Profile;
import ai.singlr.core.StringUtils;
import ai.singlr.core.Utils;
import io.helidon.config.Config;
import io.helidon.config.ConfigSources;
import io.helidon.logging.common.LogConfig;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Fat jar's entry point into the application.
 */
public final class Main {

  private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

  private static Function<Void, Map<String, String>> configSupplier;

  private Main() {}

  /**
   * Server main entry point.
   *
   * @param args command line arguments.
   */
  public static void main(final String ...args) {
    LogConfig.configureRuntime();
    var profile = Utils.parseProfile(args);
    LOGGER.info(String.format("Using [%s] profile", profile));

    var profileConfigFile = "config-" + profile.name() + ".yaml";
    var defaultConfigFile = "config.yaml";
    Map<String, String> overrideConfig = configSupplier != null ? configSupplier.apply(null) : Map.of();

    var config = Config.builder()
        .disableEnvironmentVariablesSource()
        .sources(List.of(
            ConfigSources.create(Map.of(Utils.PROFILE, profile.name())),
            ConfigSources.create(overrideConfig),
            ConfigSources.create(envAndSecrets(profile)),
            file(profileConfigFile),
            file(defaultConfigFile))
        )
        .build();
    Config.global(config);
    new ApiServer().start();
  }

  public static void overrideConfigSource(Function<Void, Map<String, String>> supplier) {
    configSupplier = supplier;
  }

  private static Map<String, String> envAndSecrets(Profile profile) {
    Set<String> recognizedKeys = Set.of(
        "WAN_ID",
        "WAN_ORIGINS"
    );

    Map<String, String> finalConfig = new HashMap<>(recognizedKeys.size());

    for (var key : recognizedKeys) {
      String value = System.getenv(key);

      if (StringUtils.isBlank(value)) {
        LOGGER.warning(String.format("Missing env/secret [%s]", key));
        continue;
      }

      var tokens = key.split("_");
      var dotKey = new StringBuilder();
      for (var t = 0; t < tokens.length - 1; t++) {
        dotKey.append(tokens[t].toLowerCase());
        dotKey.append(".");
      }
      dotKey.append(tokens[tokens.length - 1].toLowerCase());

      finalConfig.put(dotKey.toString(), value);
    }

    return finalConfig;
  }
}
