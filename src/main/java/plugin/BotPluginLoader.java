package plugin;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;


public class BotPluginLoader extends PluginLoader<PlayerStrategy> {

    private static final Logger LOGGER = Logger.getLogger(BotPluginLoader.class.getName());

    @Override
    protected Class<PlayerStrategy> getPluginInterface() {
        return PlayerStrategy.class;
    }
    
    public List<PlayerStrategy> loadAllStrategies(File jarFile) throws PluginLoadException {
        List<PlayerStrategy> strategies = loadAllFromJar(jarFile);
        if (strategies.isEmpty()) {
            throw new PluginLoadException(
                "No PlayerStrategy implementation found in: " + jarFile.getName()
            );
        }
        strategies.forEach(s ->
            LOGGER.info("Bot strategy loaded: " + s.getStrategyName())
        );
        return strategies;
    }
}
