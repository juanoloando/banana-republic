package model.player;

/**
 * 2 special achievement cards
 * Only one player bisa punya salah satu di satu waktu
 * each  worth +2 VP dan bisa ditransfer between players.
 */
public enum SpecialCard {

    LONGEST_ROAD("Jalan Terpanjang",
            "Pemain pertama dengan rangkaian Pipa berurutan minimal 5 ruas. Berpindah ke pemain lain jika mereka membangun lebih panjang."),

    LARGEST_ARMY("Pasukan Terbesar",
            "Pemain pertama yang memainkan 3 Kartu Penjaga. Berpindah ke pemain lain jika mereka memainkan lebih banyak.");

    private final String displayName;
    private final String description;

    SpecialCard(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName()  { return displayName; }
    public String getDescription()  { return description; }
    public int getVictoryPoints()   { return 2; }
}
