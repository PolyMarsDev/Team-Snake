package me.polymarsdev.teamsnake.config;

import net.dv8tion.jda.internal.utils.Checks;
import org.cfg4j.provider.ConfigurationProvider;
import org.cfg4j.provider.ConfigurationProviderBuilder;
import org.cfg4j.source.ConfigurationSource;
import org.cfg4j.source.context.environment.ImmutableEnvironment;
import org.cfg4j.source.context.filesprovider.ConfigFilesProvider;
import org.cfg4j.source.files.FilesConfigurationSource;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collections;

public class Config {

    private final ConfigurationProvider provider;

    /**
     * Retrieve a configuration instance of a file.
     * If {@code autoCreate} is true and the file does not exist, the file will be created.
     *
     * @param file The file of the configuration
     * @return the configuration instance of the given file
     */
    public static Config getConfig(@NotNull File file) {
        Checks.notNull(file, "file");
        if (!file.exists()) {
            try {
                boolean created = file.createNewFile();
                if (!created) System.out.println("[ERROR] Could not auto-create configuration file.");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return new Config(file);
    }

    private Config(File configFile) {
        ConfigFilesProvider configFilesProvider = () -> Collections.singletonList(Paths.get(configFile.getPath()));
        ConfigurationSource source = new FilesConfigurationSource(configFilesProvider);
        provider = new ConfigurationProviderBuilder()
                .withEnvironment(new ImmutableEnvironment(System.getProperty("user.dir")))
                .withConfigurationSource(source).build();
    }

    public ConfigurationProvider getProvider() {
        return provider;
    }
}