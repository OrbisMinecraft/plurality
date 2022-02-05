/*
 * Copyright © 2022 Luis Michaelis
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package net.orbismc.plurality;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Configuration data for <i>Plurality</i>-
 */
public class PluralityConfig {
	/**
	 * Loads the configuration from a file.
	 *
	 * @param file The configuration file to load from.
	 * @return The configuration
	 */
	@Contract("_ -> new")
	public static @NotNull ConfigurationNode load(final @NotNull File file) {
		if (!file.exists()) saveDefaultConfiguration(file);

		ConfigurationNode config;
		try {
			config = YAMLConfigurationLoader.builder()
					.setSource(() -> new BufferedReader(new FileReader(file, StandardCharsets.UTF_8)))
					.build()
					.load();
		} catch (IOException e) {
			throw new IllegalStateException("Failed to load configuration file.");
		}

		return config;
	}

	/**
	 * Copies the default configuration from the Jar into the given file.
	 *
	 * @param into The file to copy into.
	 */
	public static void saveDefaultConfiguration(final @NotNull File into) {
		if (!into.getParentFile().exists() && !into.getParentFile().mkdirs())
			throw new IllegalStateException("Failed to create configuration directory.");

		try {
			final var bundled = Plurality.class.getClassLoader().getResourceAsStream("config.yml");
			if (bundled == null) throw new IllegalStateException("Couldn't find bundled default configuration file");

			Files.copy(bundled, into.toPath());
			bundled.close();
		} catch (IOException e) {
			throw new IllegalStateException("Failed to copy the default configuration file to the proper location. " +
					"Are your filesystem permissions set up properly?");
		}
	}
}
