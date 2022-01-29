package net.orbismc.plurality.storage;

import com.google.common.reflect.TypeToken;
import com.velocitypowered.api.proxy.Player;
import net.orbismc.plurality.Plurality;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Optional;

/**
 * YAML file storage driver for <i>Plurality</i>.
 */
public class FileStorage extends Storage {
	private final HashMap<String, String> players = new HashMap<>();

	protected FileStorage(Plurality plugin) {
		super(plugin);
	}

	@Override
	public void init(final @NotNull ConfigurationNode config) throws IOException {
		final var playersFile = plugin.getConfigurationDirectory().resolve("players.yml").toFile();
		if (!playersFile.exists()) return;

		final var rawPlayers = YAMLConfigurationLoader.builder()
				.setSource(() -> new BufferedReader(new FileReader(playersFile)))
				.build()
				.load();

		for (final var entry : rawPlayers.getChildrenMap().entrySet()) {
			players.put((String) entry.getKey(), entry.getValue().getString());
		}
	}

	@Override
	public void term() throws Exception {
		final var playersFile = plugin.getConfigurationDirectory().resolve("players.yml").toFile();
		final var node = ConfigurationNode.root();
		node.setValue(this.players);

		YAMLConfigurationLoader.builder()
				.setSink(() -> new BufferedWriter(new FileWriter(playersFile)))
				.setFlowStyle(DumperOptions.FlowStyle.FLOW)
				.setIndent(4)
				.build()
				.save(node);
	}

	@Override
	public Optional<String> getLastServer(final @NotNull Player player) {
		return Optional.ofNullable(this.players.getOrDefault(player.getUniqueId().toString(), null));
	}

	@Override
	public void setLastServer(final @NotNull Player player, final @NotNull String server) {
		this.players.put(player.getUniqueId().toString(), server);
	}
}
