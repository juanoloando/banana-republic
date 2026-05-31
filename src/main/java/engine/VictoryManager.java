package engine;

import model.board.Board;
import model.board.Intersection;
import model.building.Laboratory;
import model.building.Settlement;
import model.card.CardCategory;
import model.card.VictoryPointCard;
import model.player.Player;
import model.player.SpecialCard;

import java.util.Optional;

public class VictoryManager {

    private static final int VICTORY_THRESHOLD = 10;

    public int computeVictoryPoints(Player player, Board board) {
        assert player != null : "Player must not be null";
        assert board != null : "Board must not be null";

        int points = 0;

        for (Intersection intersection : board.getAllIntersections()) {
            Optional<model.building.Building> building = intersection.getBuilding();
            if (building.isPresent() && building.get().getOwnerName().equals(player.getName())) {
                points += building.get().getVictoryPoints();
            }
        }

        if (player instanceof model.player.HumanPlayer) {
            points += ((model.player.HumanPlayer) player).getInventory()
                    .getCardsByCategory(CardCategory.VICTORY_POINT).size()
                    * VictoryPointCard.VP_VALUE;
        }

        if (player instanceof model.player.HumanPlayer) {
            points += ((model.player.HumanPlayer) player).getSpecialCardVictoryPoints();
        }

        return points;
    }

    public Optional<Player> checkWinner(GameStateImpl state) {
        assert state != null : "GameState must not be null";

        Player activePlayer = state.getActivePlayer();
        int vp = computeVictoryPoints(activePlayer, state.getBoard());
        if (vp >= VICTORY_THRESHOLD) {
            return Optional.of(activePlayer);
        }
        return Optional.empty();
    }

    public boolean hasWon(Player player, Board board) {
        return computeVictoryPoints(player, board) >= VICTORY_THRESHOLD;
    }

    public int getVictoryThreshold() {
        return VICTORY_THRESHOLD;
    }
}
