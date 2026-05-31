package model.card;

import model.player.Player;

/**
 * pemain lgsg menempatkan 2 pipa transportasi baru secara gratis 
 * 3 kartu di deck.
 */
public final class RoadBuildingCard extends ProgressCard {

    public RoadBuildingCard() {
        super(
            "Konstruksi Cepat",
            "Tempatkan 2 Pipa Transportasi baru secara gratis sesuai aturan normal pembangunan Pipa."
        );
    }

    @Override
    public void applyEffect(GameStateContext state, Player player) {
        // Tidak ada efek otomatis — engine handle interaksi pilih lokasi pipa.
    }
}
