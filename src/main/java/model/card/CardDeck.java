package model.card;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

/**
 * pake ArrayDeque so cards can be drawn from the top atau ditaro in any position
 * added to the top/bottom of the deck
 * players gabisa beli card if the deck is empty
 */
public class CardDeck {

    private final Deque<ExperimentCard> cards;

    public CardDeck(List<ExperimentCard> initialCards) {
        this.cards = new ArrayDeque<>(initialCards);
    }

    public void shuffle() {
        List<ExperimentCard> list = new java.util.ArrayList<>(cards);
        Collections.shuffle(list);
        cards.clear();
        cards.addAll(list);
    }

    public Optional<ExperimentCard> draw() {
        return cards.isEmpty() ? Optional.empty() : Optional.of(cards.pop());
    }

    public int size() {
        return cards.size();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public void injectCard(ExperimentCard card) {
        assert card != null : "Cannot inject a null card into the deck";
        cards.addLast(card);
        shuffle();
    }

    public void injectCards(List<? extends ExperimentCard> newCards) {
        assert newCards != null && !newCards.isEmpty()
                : "Cannot inject empty or null card list";
        newCards.forEach(cards::addLast);
        shuffle();
    }
}
