package plugin;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import model.card.CardDeck;
import model.card.ExperimentCard;



// Plugin loader khusus ExperimentCard, jadi dia muat kartu baru dari JAR pas game lagi jalan (runtime)
public class CardPluginLoader extends PluginLoader<ExperimentCard> {

    private static final Logger LOGGER = Logger.getLogger(CardPluginLoader.class.getName());

    @Override
    protected Class<ExperimentCard> getPluginInterface() {
        return ExperimentCard.class;
    }


    public ExperimentCard loadAndInject(File jarFile, String className, CardDeck deck)
        throws PluginLoadException {
        assert deck != null : "CardDeck must not be null";

        ExperimentCard card = loadPlugin(jarFile, className);
        deck.injectCard(card);
        LOGGER.info("Card '" + card.getCardName() + "' injected into deck and shuffled.");
        return card;
    }


    public List<ExperimentCard> loadAllAndInject(File jarFile, CardDeck deck)
        throws PluginLoadException {
        assert deck != null : "CardDeck must not be null";

        List<ExperimentCard> cards = loadAllFromJar(jarFile);
        if (cards.isEmpty()) {
            LOGGER.warning("No ExperimentCard implementations found in: " + jarFile.getName());
            return cards;
        }
        deck.injectCards(cards);
        LOGGER.info(cards.size() + " card(s) injected from " + jarFile.getName());
        return cards;
    }
}
