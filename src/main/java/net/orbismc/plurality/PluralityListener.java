/*
 * Copyright © 2022 Luis Michaelis
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package net.orbismc.plurality;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * The main Velocity event listener class of <i>Plurality</i>.
 */
public class PluralityListener {
	private final Plurality plugin;
	private final HashMap<String, Boolean> dontResolveOldServer = new HashMap<>();

	public PluralityListener(final @NotNull Plurality plugin) {
		this.plugin = plugin;
	}

	@Subscribe
	public void onPreLogin(final @NotNull PreLoginEvent event) {
		final var virtualHost = event.getConnection().getVirtualHost();
		if (virtualHost.isPresent() && !virtualHost.get().getHostName().startsWith(plugin.rootHostName)) {
			dontResolveOldServer.put(event.getUsername(), true);
			return;
		}

		dontResolveOldServer.put(event.getUsername(), false);
	}

	@Subscribe
	public void onChooseServer(final @NotNull PlayerChooseInitialServerEvent event) {
		final var lastServer = plugin.getStorage().getLastServer(event.getPlayer());
		final var player = event.getPlayer();

		if (dontResolveOldServer.getOrDefault(player.getUsername(), false)) {
			return;
		}

		if (lastServer.isPresent()) {
			final var targetServer = plugin.getProxy().getServer(lastServer.get()).orElseGet(() -> {
				final var defaultServer = event.getInitialServer();

				if (defaultServer.isEmpty()) {
					plugin.getLogger().error("Could not find a server in which to put {}!", player.getUsername());
					return null;
				} else {
					plugin.getLogger()
							.warn("{} was previously connected to {} which does not exist any more. Moving them into {}.",
									player.getUsername(),
									lastServer.get(),
									defaultServer.get()
							);

					return defaultServer.get();
				}
			});

			if (targetServer != null) {
				plugin.getLogger()
						.info("Connecting {} to previously left server {}", player.getUsername(), lastServer.get());
			}

			event.setInitialServer(targetServer);
		} else {
			plugin.getLogger().info("{} joined for the first time.", player.getUsername());
		}
	}

	@Subscribe
	public void onConnected(final @NotNull ServerConnectedEvent event) {
		final var player = event.getPlayer();
		final var server = event.getServer();

		plugin.getStorage().setLastServer(player, server.getServerInfo().getName());
	}
}
