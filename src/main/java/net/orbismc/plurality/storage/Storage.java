package net.orbismc.plurality.storage;

import com.velocitypowered.api.proxy.Player;
import net.orbismc.plurality.Plurality;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;

/**
 * Represents a storage driver for <i>Plurality</i>.
 */
public abstract class Storage {
	protected final Plurality plugin;

	protected Storage(Plurality plugin) {
		this.plugin = plugin;
	}

	/**
	 * Get the storage driver with the given name.
	 *
	 * @param name The name of the driver
	 * @return An instance of that driver
	 */
	public static Optional<Storage> getDriver(final @NotNull Plurality plugin, final @NotNull String name) {
		return switch (name) {
			case "mysql" -> Optional.of(new MySQLStorage(plugin));
			case "file" -> Optional.of(new FileStorage(plugin));
			default -> Optional.empty();
		};
	}

	/**
	 * Initializes the driver with the proper configuration.
	 *
	 * @param config The configuration data.
	 */
	public void init(final @NotNull ConfigurationNode config) throws Exception {
	}

	/**
	 * Terminates the driver, saving any not yet persisted state.
	 */
	public void term() throws Exception {
	}

	/**
	 * Gets the last server that the given player was connected to.
	 *
	 * @param player The player to look up.
	 * @return The name of the server the player was last connected to.
	 */
	public abstract Optional<String> getLastServer(final @NotNull Player player);

	/**
	 * Sets the last server the player was connected to.
	 *
	 * @param player The player to set the server for
	 * @param server The name of the server the player was connected to.
	 */
	public abstract void setLastServer(final @NotNull Player player, final @NotNull String server);
}
