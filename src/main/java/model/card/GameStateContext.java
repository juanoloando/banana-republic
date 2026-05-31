package model.card;

import model.player.Player;
import model.resource.Bank;
import model.resource.ResourceType;

import java.util.List;

// representasi state game yang diperlukan untuk menerapkan efek kartu
public interface GameStateContext {

    List<Player> getAllPlayers();
    Bank getBank();
    Player getActivePlayer();

    void moveRobber(int tileIndex);
    void giveResourceToPlayer(Player player, ResourceType type, int amount);
}
