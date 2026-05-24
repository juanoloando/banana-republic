package persistence;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Data class yang merepresentasikan snapshot lengkap state permainan
 * Dipake sebagai struktur antara GameStateImpl (live) dan file JSON
 * Semua field adalah plain data (jadi tidak ada referensi ke objek game), jadi gampang di seriealize/deserialize
 */
public class SaveData {

    // Metadata
    public String version;
    public String timestamp;

    // konfigurasi map (posisi terrain, token, dll)
    public List<TileData> mapConfig;
    public List<HarborData> harbors;

    // posisi Nimon Ungu
    public int robberQ;
    public int robberR;

    // data pemain (nama, warna, bangunan, kartu, special card)
    public List<PlayerData> players;

    // state giliran
    public int currentPlayerIndex;
    public String currentPhase;
    public int roundNumber;

    // timer
    public int timerSecondsRemaining;

    // bank
    public Map<String, Integer> bank;

    // deck (hanya jumlah per kategori, isinya disembunyiin karena rahasia)
    public int deckKnightCount;
    public int deckProgressCount;
    public int deckVictoryPointCount;
    public int deckCustomCount;

    // special cards
    public String longestRoadHolder;  // null jika tidak ada
    public String largestArmyHolder;  // null jika tidak ada
    public int currentLongestRoadLength;
    public int currentLargestArmySize;


    // satu tile di map
    public static class TileData {
        public int q;
        public int r;
        public String terrainType;
        public int tokenNumber;

        public TileData() {}

        public TileData(int q, int r, String terrainType, int tokenNumber) {
            this.q = q;
            this.r = r;
            this.terrainType = terrainType;
            this.tokenNumber = tokenNumber;
        }
    }

    // satu pelabuhan di map
    public static class HarborData {
        public String harborType;
        public int intersectionIdA;
        public int intersectionIdB;

        public HarborData() {}

        public HarborData(String harborType, int intersectionIdA, int intersectionIdB) {
            this.harborType = harborType;
            this.intersectionIdA = intersectionIdA;
            this.intersectionIdB = intersectionIdB;
        }
    }

    // snapshot data satu pemain
    public static class PlayerData {
        public String name;
        public String color;
        public boolean isBot;
        public String botStrategyName; 

        // Resources di tangan
        public Map<String, Integer> resources;

        // Bangunan di papan
        public List<BuildingData> buildings;
        public List<Integer> pipePathIds;

        // Kartu di tangan 
        public int knightCardsInHand;
        public int progressCardsInHand;
        public int victoryPointCardsInHand;
        public int customCardsInHand;

        public int knightCardsPlayed;

        // Special cards yang dipegang
        public List<String> heldSpecialCards;

        public PlayerData() {
            this.resources = new java.util.HashMap<>();
            this.buildings = new ArrayList<>();
            this.pipePathIds = new ArrayList<>();
            this.heldSpecialCards = new ArrayList<>();
        }
    }

    // satu bangunan di papan
    public static class BuildingData {
        public int intersectionId;
        public String buildingType; 

        public BuildingData() {}

        public BuildingData(int intersectionId, String buildingType) {
            this.intersectionId = intersectionId;
            this.buildingType = buildingType;
        }
    }

    // factory method untuk buat SaveData kosong dengan metadata yang diisi
    public static SaveData createEmpty(String version) {
        SaveData data = new SaveData();
        data.version = version;
        data.timestamp = Instant.now().toString();
        data.mapConfig = new ArrayList<>();
        data.harbors = new ArrayList<>();
        data.players = new ArrayList<>();
        data.bank = new java.util.HashMap<>();
        return data;
    }
}
