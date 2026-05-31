package engine;

import model.board.Board;
import model.board.HexCoordinate;
import model.card.CardDeck;
import model.player.Player;
import model.player.SpecialCard;
import model.resource.Bank;

import java.util.List;
import java.util.Optional;

public interface GameState {

    List<Player> getAllPlayers();

    Bank getBank();

    Board getBoard();

    CardDeck getDeck();

    Player getActivePlayer();

    int getCurrentPlayerIndex();

    HexCoordinate getRobberPosition();

    Optional<Player> getSpecialCardHolder(SpecialCard card);

    TurnPhase getCurrentPhase();

    int getRoundNumber();

    boolean isSetupPhase();

    boolean isGameOver();

    Optional<Player> getWinner();
}
