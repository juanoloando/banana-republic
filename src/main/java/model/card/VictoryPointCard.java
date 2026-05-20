package model.card;

import model.player.Player;

/**
 * +1 Poin Prestasi, tersembunyi dari pemain lain sampai pemilik menang or game berakhir
 * 5 kartu di deck
 * klo kartu ini yang baru dibeli membuat total VP pemain >= 10, pemain lgsg mengungkapkan dan menang di giliran 
 */
public final class VictoryPointCard extends AbstractCard {

    public static final int VP_VALUE = 1;

    public VictoryPointCard() {
        super(
            "Kartu PP Rahasia",
            "Bernilai +1 Poin Prestasi. Tersembunyi sampai permainan berakhir atau Anda menang.",
            CardCategory.VICTORY_POINT
        );
    }

    @Override
    public void applyEffect(GameStateContext state, Player player) {
        // ga ada efek aktif, VP dihitung oleh VictoryManager dgn getCardsByCategory(VICTORY_POINT).size() * VP_VALUE
    }

    @Override
    public boolean isRevealedWhenPlayed() {
        return false;
    }

    public int getVictoryPoints() {
        return VP_VALUE;
    }
}
