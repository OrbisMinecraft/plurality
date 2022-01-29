/*
 * Copyright © 2022 Luis Michaelis
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package net.orbismc.plurality.storage;

import com.velocitypowered.api.proxy.Player;
import net.orbismc.plurality.Plurality;
import ninja.leaping.configurate.ConfigurationNode;
import org.jetbrains.annotations.NotNull;
import org.mariadb.jdbc.MariaDbPoolDataSource;

import java.sql.SQLException;
import java.util.Optional;

/**
 * MySQL storage driver for <i>Plurality</i>.
 */
public class MySQLStorage extends Storage {
	private MariaDbPoolDataSource pool;

	protected MySQLStorage(Plurality plugin) {
		super(plugin);
	}

	@Override
	public void init(@NotNull ConfigurationNode config) throws Exception {
		final var url = config.getNode("url").getString(null);
		if (url == null) throw new RuntimeException("No database URL provided");

		pool = new MariaDbPoolDataSource(url);
		pool.setMaxPoolSize(3);

		// try to connect to the database
		pool.getConnection().close();

		// make sure the required table exists
		try (final var conn = pool.getConnection()) {
			conn.createStatement().execute("CREATE TABLE IF NOT EXISTS plurality (player VARCHAR(64) PRIMARY KEY, server VARCHAR(64) NOT NULL);");
		}
	}

	@Override
	public void term() {
		pool.close();
	}

	@Override
	public Optional<String> getLastServer(@NotNull Player player) {
		try (final var conn = pool.getConnection()) {
			final var stmt = conn.prepareStatement("SELECT server FROM plurality WHERE player=?");
			stmt.setString(1, player.getUniqueId().toString());

			final var result = stmt.executeQuery();
			if (result.next()) return Optional.of(result.getString("server"));
			return Optional.empty();
		} catch (SQLException e) {
			plugin.getLogger().error("Failed to retrieve the last server of {} from the database", player.getUsername(), e);
			return Optional.empty();
		}
	}

	@Override
	public void setLastServer(@NotNull Player player, @NotNull String server) {
		try (final var conn = pool.getConnection()) {
			final var stmt = conn.prepareStatement("REPLACE INTO plurality (player, server) VALUES (?, ?)");
			stmt.setString(1, player.getUniqueId().toString());
			stmt.setString(2, server);
			stmt.executeUpdate();
		} catch (SQLException e) {
			plugin.getLogger().error("Failed to set the last server of {} in the database", player.getUsername(), e);
		}
	}
}
