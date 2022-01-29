/*
 * Copyright © 2022 Luis Michaelis
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package net.orbismc.plurality;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.orbismc.plurality.storage.Storage;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

@Plugin(
		id = "plurality",
		name = "Plurality",
		version = BuildConstants.VERSION,
		description = "A Velocity plugin that puts players into the same server they left previously."
)
public class Plurality {
	private Storage storage;

	@Inject
	private Logger logger;

	@Inject
	private ProxyServer proxy;

	@Inject
	@DataDirectory
	private Path configurationDirectory;

	/**
	 * Handler for proxy initialization. This is an event handler that is called when Velocity starts up.
	 *
	 * @param event The proxy initialization event data.
	 */
	@Subscribe
	public void onProxyInitialization(final @NotNull ProxyInitializeEvent event) {
		loadConfiguration();
		proxy.getEventManager().register(this, new PluralityListener(this));
	}

	@Subscribe
	public void onProxyShutdown(final @NotNull ProxyShutdownEvent event) {
		try {
			this.getStorage().term();
		} catch (Exception e) {
			throw new RuntimeException("Failed to properly shut down storage driver", e);
		}
	}

	public @NotNull Storage getStorage() {
		return this.storage;
	}

	public @NotNull ProxyServer getProxy() {
		return this.proxy;
	}

	public @NotNull Logger getLogger() {
		return this.logger;
	}

	public @NotNull Path getConfigurationDirectory() {
		return this.configurationDirectory;
	}

	private void loadConfiguration() {
		final var directory = this.configurationDirectory.toFile();
		if (!directory.exists() && !directory.mkdirs())
			throw new IllegalStateException("Failed to create configuration directory.");

		final var file = new File(directory, "config.yml");
		if (!file.exists()) PluralityConfig.saveDefaultConfiguration(file.toPath());

		ConfigurationNode config;
		try {
			config = YAMLConfigurationLoader.builder()
					.setSource(() -> new BufferedReader(new FileReader(file)))
					.build()
					.load();
		} catch (IOException e) {
			throw new IllegalStateException("Failed to load configuration file.");
		}

		try {
			final var storageNode = config.getNode("storage");
			final var storageDriver = storageNode.getNode("method").getString();
			if (storageDriver == null) throw new IllegalStateException("Invalid configuration file semantics");

			final var storage = Storage.getDriver(this, storageDriver);
			if (storage.isEmpty())
				throw new IllegalStateException("Unknown storage driver %s".formatted(storageDriver));
			this.storage = storage.get();
			this.storage.init(storageNode);
		} catch (Exception e) {
			throw new IllegalStateException("Invalid configuration file semantics", e);
		}
	}
}
