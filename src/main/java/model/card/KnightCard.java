package model.card;

import model.player.Player;

/*
 * memindahkan nimon ungu tanpa memicu penalti buang kartu >7 untuk pemain lain. 
 * abis memindahkan, boleh curi  1 kartu acak
 * 14 kartu di deck
 * hrs diperlihatkan ke semua pemain saat dimainkan
 * (penting untuk penghitungan Pasukan Terbesar).
 */
public final class KnightCard extends AbstractCard {

    public KnightCard() {
        super(
            "Kartu Penjaga",
            "Pindahkan Nimon Ungu ke petak lain (tanpa penalti buang kartu). " +
            "Boleh mencuri 1 kartu sumber daya acak dari pemain yang punya bangunan di sekitar petak baru.",
            CardCategory.KNIGHT
        );
    }

    @Override
    public void applyEffect(GameStateContext state, Player player) {
        // knight card langsung menaikkan counter knight played di inventory, lanjut ke fase interaktif (pilih petak, pilih target curi)
        // blm ada logika yg bisa dijalankan tanpa input UI
    }

    @Override
    public boolean isRevealedWhenPlayed() {
        return true;
    }
}
