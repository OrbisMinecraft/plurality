package net.orbismc.plurality;

import org.jetbrains.annotations.NotNull;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configuration data for <i>Plurality</i>-
 */
public class PluralityConfig {
	/**
	 * Copies the default configuration from the Jar into the given file.
	 *
	 * @param into The file to copy into.
	 */
	public static void saveDefaultConfiguration(final @NotNull Path into) {
		try {
			final var bundled = Plurality.class.getClassLoader().getResourceAsStream("config.yml");
			if (bundled == null) throw new IllegalStateException("Couldn't find bundled default configuration file");

			Files.copy(bundled, into);
			bundled.close();
		} catch (IOException e) {
			throw new IllegalStateException("Failed to copy the default configuration file to the proper location. " +
					"Are your filesystem permissions set up properly?");
		}
	}
}
