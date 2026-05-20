package model.card;

import model.player.Player;
import model.resource.ResourceType;

import java.util.List;

/**
 * pemain menyebutkan 1 jenis sumber daya. Semua pemain lain yg punya sumber daya jenis tersebut menyerahkan semua kartu jenis itu
 * 3 kartu di deck
 */
public final class MonopolyCard extends ProgressCard {

    private ResourceType chosenResource;

    public MonopolyCard() {
        super(
            "Monopoli Nimon",
            "Sebutkan 1 jenis sumber daya. Semua pemain lain WAJIB menyerahkan semua kartu " +
            "sumber daya jenis tersebut kepada Anda. Pemain tanpa sumber daya itu tidak menyerahkan apapun."
        );
    }

    public void setChosenResource(ResourceType resource) {
        assert resource != null : "Chosen resource for Monopoly must not be null";
        this.chosenResource = resource;
    }

    public ResourceType getChosenResource() {
        return chosenResource;
    }

    @Override
    public void applyEffect(GameStateContext state, Player player) {
        assert chosenResource != null
            : "setChosenResource() harus dipanggil sebelum applyEffect() pada MonopolyCard";

        List<Player> allPlayers = state.getAllPlayers();
        for (Player other : allPlayers) {
            if (other.getName().equals(player.getName())) continue;
            int amount = other.getResourceCount(chosenResource);
            if (amount > 0) {
                other.removeResource(chosenResource, amount);
                player.addResource(chosenResource, amount);
            }
        }
    }
}
