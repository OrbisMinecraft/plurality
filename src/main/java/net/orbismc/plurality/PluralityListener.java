/*
 * Copyright © 2022 Luis Michaelis
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package net.orbismc.plurality;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * The main Velocity event listener class of <i>Plurality</i>.
 */
public class PluralityListener {
	private final Plurality plugin;

	public PluralityListener(final @NotNull Plurality plugin) {
		this.plugin = plugin;
	}

	@Subscribe
	public void onChooseServer(final @NotNull PlayerChooseInitialServerEvent event) {
		final var lastServer = plugin.getStorage().getLastServer(event.getPlayer());
		final var player = event.getPlayer();

		if (lastServer.isPresent()) {
			final var targetServer = plugin.getProxy().getServer(lastServer.get()).orElseGet(() -> {
				final var defaultServer = event.getInitialServer();

				if (defaultServer.isEmpty()) {
					plugin.getLogger().error("Could not find a server in which to put {}!", player.getUsername());
					return null;
				} else {
					plugin.getLogger().warn("{} was previously connected to {} which does not exist any more. Moving them into {}.",
							player.getUsername(),
							lastServer.get(),
							defaultServer.get()
					);

					return defaultServer.get();
				}
			});

			if (targetServer != null) {
				plugin.getLogger().info("Connecting {} to previously left server {}", player.getUsername(), lastServer.get());
			}

			event.setInitialServer(targetServer);
		} else {
			plugin.getLogger().info("{} joined for the first time.", player.getUsername());
		}
	}

	@Subscribe
	public void onDisconnect(final @NotNull DisconnectEvent event) {
		final var player = event.getPlayer();
		final var server = player.getCurrentServer();

		if (server.isEmpty()) {
			plugin.getLogger().error("Could not determine the last server {} joined!", player.getUsername());
			return;
		}

		plugin.getStorage().setLastServer(player, server.get().getServer().getServerInfo().getName());
	}
}
