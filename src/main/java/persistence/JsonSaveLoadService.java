package persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import engine.GameStateImpl;
import model.board.Board;
import model.board.BoardPath;
import model.board.Harbor;
import model.board.HexTile;
import model.board.Intersection;
import model.building.Laboratory;
import model.card.CardCategory;
import model.player.HumanPlayer;
import model.player.Player;
import model.player.PlayerInventory;
import model.player.SpecialCard;
import model.resource.ResourceType;
import plugin.BotPlayer;


// Implementasi SaveLoadService yg pake format JSON buat nyimpen dan load data permainannya
public class JsonSaveLoadService implements SaveLoadService {

    private static final Logger LOGGER = Logger.getLogger(JsonSaveLoadService.class.getName());
    private static final String SAVE_VERSION = "1.0.0";
    private static final String FILE_EXTENSION = ".json";

    @Override
    public String getFileExtension() {
        return FILE_EXTENSION;
    }

    @Override
    public boolean isValidSave(File file) {
        if (file == null || !file.exists() || !file.isFile()) return false;
        if (!file.getName().endsWith(FILE_EXTENSION)) return false;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String firstLine = reader.readLine();
            return firstLine != null && firstLine.trim().startsWith("{");
        } catch (IOException e) {
            return false;
        }
    }

    // fitur save
    @Override
    public void save(GameStateImpl state, File file) throws IOException {
        assert state != null : "GameStatete tidak boleh null saat disave";
        assert file != null : "Save file tidak boleh null";

        SaveData data = buildSaveData(state);
        String json = serializeToJson(data);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(json);
        }
        LOGGER.info("Game disimpan pada: " + file.getAbsolutePath());
    }

    // GameStateImpl diubah menjadi SaveData 
    private SaveData buildSaveData(GameStateImpl state) {
        SaveData data = SaveData.createEmpty(SAVE_VERSION);

        // Map
        for (HexTile tile : state.getBoard().getAllTiles()) {
            data.mapConfig.add(new SaveData.TileData(
                tile.getCoordinate().getQ(),
                tile.getCoordinate().getR(),
                tile.getTerrainType().name(),
                tile.getTokenNumber()
            ));
        }
        for (Harbor harbor : state.getBoard().getAllHarbors()) {
            data.harbors.add(new SaveData.HarborData(
                harbor.getType().name(),
                harbor.getIntersectionIdA(),
                harbor.getIntersectionIdB()
            ));
        }

        // Posisi robber
        data.robberQ = state.getRobberPosition().getQ();
        data.robberR = state.getRobberPosition().getR();

        // State gilirannya
        data.currentPlayerIndex = state.getCurrentPlayerIndex();
        data.currentPhase = state.getCurrentPhase().name();
        data.roundNumber = state.getRoundNumber();
        data.timerSecondsRemaining = 0; // timer diset ulang saat load

        // Bank
        for (ResourceType type : ResourceType.values()) {
            data.bank.put(type.name(), state.getBank().getAvailable(type));
        }

        // Deck
        data.deckKnightCount = countCardsByCategory(state, CardCategory.KNIGHT);
        data.deckProgressCount = countCardsByCategory(state, CardCategory.PROGRESS);
        data.deckVictoryPointCount = countCardsByCategory(state, CardCategory.VICTORY_POINT);
        data.deckCustomCount = countCardsByCategory(state, CardCategory.CUSTOM);

        // Specal cards
        state.getSpecialCardHolder(SpecialCard.LONGEST_ROAD)
            .ifPresent(p -> data.longestRoadHolder = p.getName());
        state.getSpecialCardHolder(SpecialCard.LARGEST_ARMY)
            .ifPresent(p -> data.largestArmyHolder = p.getName());
        data.currentLongestRoadLength = state.getLongestRoadLength();
        data.currentLargestArmySize = state.getLargestArmySize();

        // Data Pemain
        for (Player player : state.getAllPlayers()) {
            data.players.add(buildPlayerData(player, state.getBoard()));
        }

        return data;
    }

    private int countCardsByCategory(GameStateImpl state, CardCategory category) {
        return state.getDeck().size(); 
    }

    private SaveData.PlayerData buildPlayerData(Player player, Board board) {
        SaveData.PlayerData pd = new SaveData.PlayerData();
        pd.name = player.getName();
        pd.color = player.getColor().name();
        pd.isBot = player instanceof BotPlayer;
        if (pd.isBot) {
            pd.botStrategyName = ((BotPlayer) player).getStrategy().getStrategyName();
        }

        // Resources
        for (ResourceType type : ResourceType.values()) {
            pd.resources.put(type.name(), player.getResourceCount(type));
        }

        // Bangunan di papan
        for (Intersection intersection : board.getAllIntersections()) {
            intersection.getBuilding()
                .filter(b -> b.getOwnerName().equals(player.getName()))
                .ifPresent(b -> pd.buildings.add(new SaveData.BuildingData(
                    intersection.getId(),
                    b instanceof Laboratory ? "LABORATORY" : "SETTLEMENT"
                )));
        }

        // Pipa di papan
        for (BoardPath path : board.getAllPaths()) {
            path.getPipe()
                .filter(p -> p.getOwnerName().equals(player.getName()))
                .ifPresent(p -> pd.pipePathIds.add(path.getId()));
        }

        // Kartu di tangan (hanya kategori dan jumlahnya, detail kartu ga disimpen karena sifatnya rahasia)
        if (player instanceof HumanPlayer) {
            PlayerInventory inv = ((HumanPlayer) player).getInventory();
            pd.knightCardsInHand = inv.getCardsByCategory(CardCategory.KNIGHT).size();
            pd.progressCardsInHand = inv.getCardsByCategory(CardCategory.PROGRESS).size();
            pd.victoryPointCardsInHand = inv.getCardsByCategory(CardCategory.VICTORY_POINT).size();
            pd.customCardsInHand = inv.getCardsByCategory(CardCategory.CUSTOM).size();
            pd.knightCardsPlayed = inv.getKnightCardsPlayed();
        }

        // Special cards
        if (player instanceof HumanPlayer) {
            for (SpecialCard sc : ((HumanPlayer) player).getHeldSpecialCards()) {
                pd.heldSpecialCards.add(sc.name());
            }
        }

        return pd;
    }

    // Load
    @Override
    public SaveData load(File file) throws IOException, SaveCorruptedException {
        assert file != null : "File to load tidak boleh null";
        assert file.exists() : "Save file tidak ada: " + file.getAbsolutePath();

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }

        try {
            SaveData data = parseFromJson(sb.toString());
            validateSaveData(data);
            LOGGER.info("Game diload dari: " + file.getAbsolutePath());
            return data;
        } catch (SaveCorruptedException e) {
            throw e;
        } catch (Exception e) {
            throw new SaveCorruptedException("Gagal memuat save file: " + e.getMessage(), e);
        }
    }

    // Validasi data yang sudah diparse untuk memastikan semua field penting ada
    private void validateSaveData(SaveData data) throws SaveCorruptedException {
        if (data.version == null || data.version.isBlank()) {
            throw new SaveCorruptedException("Field Save version tidak ditemukan atau kosong");
        }
        if (data.players == null || data.players.size() < 3 || data.players.size() > 4) {
            throw new SaveCorruptedException(
                "Jumlah pemain tidak valid: " + (data.players == null ? "null" : data.players.size())
            );
        }
        if (data.mapConfig == null || data.mapConfig.isEmpty()) {
            throw new SaveCorruptedException("Save file tidak memiliki konfigurasi peta");
        }
        if (data.currentPlayerIndex < 0 || data.currentPlayerIndex >= data.players.size()) {
            throw new SaveCorruptedException(
                "Invalid currentPlayerIndex: " + data.currentPlayerIndex
            );
        }
    }

    // JSON Serialization (data game diubah jadi string JSON)
    // Pake StringBuilder dengan indent manual untuk keterbacaan.
    private String serializeToJson(SaveData data) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        appendField(sb, "version", data.version, 1);
        appendField(sb, "timestamp", data.timestamp, 1);
        appendField(sb, "currentPlayerIndex", data.currentPlayerIndex, 1);
        appendField(sb, "currentPhase", data.currentPhase, 1);
        appendField(sb, "roundNumber", data.roundNumber, 1);
        appendField(sb, "timerSecondsRemaining", data.timerSecondsRemaining, 1);
        appendField(sb, "robberQ", data.robberQ, 1);
        appendField(sb, "robberR", data.robberR, 1);
        appendField(sb, "longestRoadHolder", data.longestRoadHolder, 1);
        appendField(sb, "largestArmyHolder", data.largestArmyHolder, 1);
        appendField(sb, "currentLongestRoadLength", data.currentLongestRoadLength, 1);
        appendField(sb, "currentLargestArmySize", data.currentLargestArmySize, 1);
        appendField(sb, "deckKnightCount", data.deckKnightCount, 1);
        appendField(sb, "deckProgressCount", data.deckProgressCount, 1);
        appendField(sb, "deckVictoryPointCount", data.deckVictoryPointCount, 1);
        appendField(sb, "deckCustomCount", data.deckCustomCount, 1);

        // Bank
        sb.append(indent(1)).append("\"bank\": {\n");
        List<String> bankEntries = new ArrayList<>(data.bank.keySet());
        for (int i = 0; i < bankEntries.size(); i++) {
            String key = bankEntries.get(i);
            sb.append(indent(2)).append('"').append(key).append("\": ").append(data.bank.get(key));
            if (i < bankEntries.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append(indent(1)).append("},\n");

        // Map config
        sb.append(indent(1)).append("\"mapConfig\": [\n");
        for (int i = 0; i < data.mapConfig.size(); i++) {
            SaveData.TileData tile = data.mapConfig.get(i);
            sb.append(indent(2)).append("{");
            sb.append("\"q\":").append(tile.q).append(",");
            sb.append("\"r\":").append(tile.r).append(",");
            sb.append("\"terrainType\":\"").append(tile.terrainType).append("\",");
            sb.append("\"tokenNumber\":").append(tile.tokenNumber);
            sb.append("}");
            if (i < data.mapConfig.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append(indent(1)).append("],\n");

        // Harbors
        sb.append(indent(1)).append("\"harbors\": [\n");
        for (int i = 0; i < data.harbors.size(); i++) {
            SaveData.HarborData h = data.harbors.get(i);
            sb.append(indent(2)).append("{");
            sb.append("\"harborType\":\"").append(h.harborType).append("\",");
            sb.append("\"intersectionIdA\":").append(h.intersectionIdA).append(",");
            sb.append("\"intersectionIdB\":").append(h.intersectionIdB);
            sb.append("}");
            if (i < data.harbors.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append(indent(1)).append("],\n");

        // Players
        sb.append(indent(1)).append("\"players\": [\n");
        for (int i = 0; i < data.players.size(); i++) {
            serializePlayerData(sb, data.players.get(i), 2);
            if (i < data.players.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append(indent(1)).append("]\n");

        sb.append("}");
        return sb.toString();
    }

    private void serializePlayerData(StringBuilder sb, SaveData.PlayerData pd, int depth) {
        sb.append(indent(depth)).append("{\n");
        appendField(sb, "name", pd.name, depth + 1);
        appendField(sb, "color", pd.color, depth + 1);
        appendField(sb, "isBot", pd.isBot, depth + 1);
        appendField(sb, "botStrategyName", pd.botStrategyName, depth + 1);
        appendField(sb, "knightCardsInHand", pd.knightCardsInHand, depth + 1);
        appendField(sb, "progressCardsInHand", pd.progressCardsInHand, depth + 1);
        appendField(sb, "victoryPointCardsInHand", pd.victoryPointCardsInHand, depth + 1);
        appendField(sb, "customCardsInHand", pd.customCardsInHand, depth + 1);
        appendField(sb, "knightCardsPlayed", pd.knightCardsPlayed, depth + 1);

        // resources
        sb.append(indent(depth + 1)).append("\"resources\": {");
        List<String> rKeys = new ArrayList<>(pd.resources.keySet());
        for (int i = 0; i < rKeys.size(); i++) {
            String k = rKeys.get(i);
            sb.append("\"").append(k).append("\":").append(pd.resources.get(k));
            if (i < rKeys.size() - 1) sb.append(",");
        }
        sb.append("},\n");

        // buildings
        sb.append(indent(depth + 1)).append("\"buildings\": [");
        for (int i = 0; i < pd.buildings.size(); i++) {
            SaveData.BuildingData bd = pd.buildings.get(i);
            sb.append("{\"intersectionId\":").append(bd.intersectionId)
              .append(",\"buildingType\":\"").append(bd.buildingType).append("\"}");
            if (i < pd.buildings.size() - 1) sb.append(",");
        }
        sb.append("],\n");

        // pipes
        sb.append(indent(depth + 1)).append("\"pipePathIds\": [");
        for (int i = 0; i < pd.pipePathIds.size(); i++) {
            sb.append(pd.pipePathIds.get(i));
            if (i < pd.pipePathIds.size() - 1) sb.append(",");
        }
        sb.append("],\n");

        // held special cards
        sb.append(indent(depth + 1)).append("\"heldSpecialCards\": [");
        for (int i = 0; i < pd.heldSpecialCards.size(); i++) {
            sb.append("\"").append(pd.heldSpecialCards.get(i)).append("\"");
            if (i < pd.heldSpecialCards.size() - 1) sb.append(",");
        }
        sb.append("]\n");

        sb.append(indent(depth)).append("}");
    }

    // JSON Deserialization (string JSON diubah jadi SaveData)
    private SaveData parseFromJson(String json) throws SaveCorruptedException {
        try {
            SaveData data = new SaveData();
            data.version = extractString(json, "version");
            data.timestamp = extractString(json, "timestamp");
            data.currentPlayerIndex = extractInt(json, "currentPlayerIndex");
            data.currentPhase = extractString(json, "currentPhase");
            data.roundNumber = extractInt(json, "roundNumber");
            data.timerSecondsRemaining = extractInt(json, "timerSecondsRemaining");
            data.robberQ = extractInt(json, "robberQ");
            data.robberR = extractInt(json, "robberR");
            data.longestRoadHolder = extractStringNullable(json, "longestRoadHolder");
            data.largestArmyHolder = extractStringNullable(json, "largestArmyHolder");
            data.currentLongestRoadLength = extractInt(json, "currentLongestRoadLength");
            data.currentLargestArmySize = extractInt(json, "currentLargestArmySize");
            data.deckKnightCount = extractInt(json, "deckKnightCount");
            data.deckProgressCount = extractInt(json, "deckProgressCount");
            data.deckVictoryPointCount = extractInt(json, "deckVictoryPointCount");
            data.deckCustomCount = extractInt(json, "deckCustomCount");

            data.bank = parseBank(json);
            data.mapConfig = parseTiles(json);
            data.harbors = parseHarbors(json);
            data.players = parsePlayers(json);

            return data;
        } catch (SaveCorruptedException e) {
            throw e;
        } catch (Exception e) {
            throw new SaveCorruptedException("JSON parse error: " + e.getMessage(), e);
        }
    }

    // ambil nilai dari JSON dengan key tertentu, kembalikan string tanpa tanda ""
    private String extractString(String json, String key) throws SaveCorruptedException {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx == -1) throw new SaveCorruptedException("Missing key: " + key);
        int colonIdx = json.indexOf(':', idx);
        int startQuote = json.indexOf('"', colonIdx + 1);
        int endQuote = json.indexOf('"', startQuote + 1);
        if (startQuote == -1 || endQuote == -1) {
            throw new SaveCorruptedException("Invalid string value for key: " + key);
        }
        return json.substring(startQuote + 1, endQuote);
    }

    private String extractStringNullable(String json, String key) {
        try {
            String val = extractString(json, key);
            return val.equals("null") ? null : val;
        } catch (SaveCorruptedException e) {
            return null;
        }
    }

    // ambil nilai int dari JSON dengan key tertentu
    private int extractInt(String json, String key) throws SaveCorruptedException {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx == -1) throw new SaveCorruptedException("Missing key: " + key);
        int colonIdx = json.indexOf(':', idx);
        int start = colonIdx + 1;
        while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '\n')) {
            start++;
        }
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) {
            end++;
        }
        try {
            return Integer.parseInt(json.substring(start, end).trim());
        } catch (NumberFormatException e) {
            throw new SaveCorruptedException("Invalid int for key: " + key);
        }
    }

    private java.util.Map<String, Integer> parseBank(String json) throws SaveCorruptedException {
        java.util.Map<String, Integer> bank = new java.util.HashMap<>();
        int bankStart = json.indexOf("\"bank\"");
        if (bankStart == -1) throw new SaveCorruptedException("Missing bank section");
        int objStart = json.indexOf('{', bankStart);
        int objEnd = json.indexOf('}', objStart);
        String bankSection = json.substring(objStart + 1, objEnd);
        for (ResourceType type : ResourceType.values()) {
            String key = "\"" + type.name() + "\"";
            int idx = bankSection.indexOf(key);
            if (idx == -1) continue;
            int colonIdx = bankSection.indexOf(':', idx);
            int start = colonIdx + 1;
            while (start < bankSection.length() && bankSection.charAt(start) == ' ') start++;
            int end = start;
            while (end < bankSection.length() && Character.isDigit(bankSection.charAt(end))) end++;
            bank.put(type.name(), Integer.parseInt(bankSection.substring(start, end).trim()));
        }
        return bank;
    }

    private List<SaveData.TileData> parseTiles(String json) throws SaveCorruptedException {
        List<SaveData.TileData> tiles = new ArrayList<>();
        int arrayStart = json.indexOf("\"mapConfig\"");
        if (arrayStart == -1) throw new SaveCorruptedException("Missing mapConfig");
        int bracketOpen = json.indexOf('[', arrayStart);
        int bracketClose = json.indexOf(']', bracketOpen);
        String section = json.substring(bracketOpen + 1, bracketClose);
        int pos = 0;
        while (true) {
            int objStart = section.indexOf('{', pos);
            if (objStart == -1) break;
            int objEnd = section.indexOf('}', objStart);
            String obj = section.substring(objStart + 1, objEnd);
            SaveData.TileData tile = new SaveData.TileData();
            tile.q = parseInlineInt(obj, "q");
            tile.r = parseInlineInt(obj, "r");
            tile.terrainType = parseInlineString(obj, "terrainType");
            tile.tokenNumber = parseInlineInt(obj, "tokenNumber");
            tiles.add(tile);
            pos = objEnd + 1;
        }
        return tiles;
    }

    private List<SaveData.HarborData> parseHarbors(String json) {
        List<SaveData.HarborData> harbors = new ArrayList<>();
        int arrayStart = json.indexOf("\"harbors\"");
        if (arrayStart == -1) return harbors;
        int bracketOpen = json.indexOf('[', arrayStart);
        int bracketClose = json.indexOf(']', bracketOpen);
        String section = json.substring(bracketOpen + 1, bracketClose);
        int pos = 0;
        while (true) {
            int objStart = section.indexOf('{', pos);
            if (objStart == -1) break;
            int objEnd = section.indexOf('}', objStart);
            String obj = section.substring(objStart + 1, objEnd);
            try {
                SaveData.HarborData h = new SaveData.HarborData();
                h.harborType = parseInlineString(obj, "harborType");
                h.intersectionIdA = parseInlineInt(obj, "intersectionIdA");
                h.intersectionIdB = parseInlineInt(obj, "intersectionIdB");
                harbors.add(h);
            } catch (Exception ignored) {}
            pos = objEnd + 1;
        }
        return harbors;
    }

    private List<SaveData.PlayerData> parsePlayers(String json) throws SaveCorruptedException {
        List<SaveData.PlayerData> players = new ArrayList<>();
        int arrayStart = json.indexOf("\"players\"");
        if (arrayStart == -1) throw new SaveCorruptedException("Missing players section");
        int bracketOpen = json.indexOf('[', arrayStart);
        // parse player objects level-by-level
        int pos = bracketOpen + 1;
        int depth = 0;
        int objStart = -1;
        for (int i = pos; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') {
                if (depth == 0) objStart = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && objStart != -1) {
                    String obj = json.substring(objStart + 1, i);
                    players.add(parsePlayerObject(obj));
                    objStart = -1;
                }
            } else if (c == ']' && depth == 0) {
                break;
            }
        }
        return players;
    }

    private SaveData.PlayerData parsePlayerObject(String obj) {
        SaveData.PlayerData pd = new SaveData.PlayerData();
        try { pd.name = parseInlineString(obj, "name"); } catch (Exception e) { pd.name = "Unknown"; }
        try { pd.color = parseInlineString(obj, "color"); } catch (Exception e) { pd.color = "RED"; }
        try { pd.isBot = obj.contains("\"isBot\":true"); } catch (Exception ignored) {}
        try { pd.botStrategyName = parseInlineStringNullable(obj, "botStrategyName"); } catch (Exception ignored) {}
        try { pd.knightCardsInHand = parseInlineInt(obj, "knightCardsInHand"); } catch (Exception ignored) {}
        try { pd.progressCardsInHand = parseInlineInt(obj, "progressCardsInHand"); } catch (Exception ignored) {}
        try { pd.victoryPointCardsInHand = parseInlineInt(obj, "victoryPointCardsInHand"); } catch (Exception ignored) {}
        try { pd.customCardsInHand = parseInlineInt(obj, "customCardsInHand"); } catch (Exception ignored) {}
        try { pd.knightCardsPlayed = parseInlineInt(obj, "knightCardsPlayed"); } catch (Exception ignored) {}
        try { pd.resources = parseResourceMap(obj); } catch (Exception ignored) {}
        try { pd.buildings = parseBuildingList(obj); } catch (Exception ignored) {}
        try { pd.pipePathIds = parsePipeIds(obj); } catch (Exception ignored) {}
        try { pd.heldSpecialCards = parseStringList(obj, "heldSpecialCards"); } catch (Exception ignored) {}
        return pd;
    }

    private java.util.Map<String, Integer> parseResourceMap(String obj) {
        java.util.Map<String, Integer> resources = new java.util.HashMap<>();
        int rStart = obj.indexOf("\"resources\"");
        if (rStart == -1) return resources;
        int braceOpen = obj.indexOf('{', rStart);
        int braceClose = obj.indexOf('}', braceOpen);
        String section = obj.substring(braceOpen + 1, braceClose);
        for (ResourceType type : ResourceType.values()) {
            try {
                int val = parseInlineInt(section, type.name());
                resources.put(type.name(), val);
            } catch (Exception ignored) {}
        }
        return resources;
    }

    private List<SaveData.BuildingData> parseBuildingList(String obj) {
        List<SaveData.BuildingData> buildings = new ArrayList<>();
        int arrStart = obj.indexOf("\"buildings\"");
        if (arrStart == -1) return buildings;
        int bracketOpen = obj.indexOf('[', arrStart);
        int bracketClose = obj.indexOf(']', bracketOpen);
        String section = obj.substring(bracketOpen + 1, bracketClose);
        int pos = 0;
        while (true) {
            int objStart = section.indexOf('{', pos);
            if (objStart == -1) break;
            int objEnd = section.indexOf('}', objStart);
            String bdObj = section.substring(objStart + 1, objEnd);
            try {
                int iid = parseInlineInt(bdObj, "intersectionId");
                String bt = parseInlineString(bdObj, "buildingType");
                buildings.add(new SaveData.BuildingData(iid, bt));
            } catch (Exception ignored) {}
            pos = objEnd + 1;
        }
        return buildings;
    }

    private List<Integer> parsePipeIds(String obj) {
        List<Integer> ids = new ArrayList<>();
        int arrStart = obj.indexOf("\"pipePathIds\"");
        if (arrStart == -1) return ids;
        int bracketOpen = obj.indexOf('[', arrStart);
        int bracketClose = obj.indexOf(']', bracketOpen);
        String section = obj.substring(bracketOpen + 1, bracketClose).trim();
        if (section.isEmpty()) return ids;
        for (String part : section.split(",")) {
            try { ids.add(Integer.parseInt(part.trim())); } catch (Exception ignored) {}
        }
        return ids;
    }

    private List<String> parseStringList(String obj, String key) {
        List<String> items = new ArrayList<>();
        int arrStart = obj.indexOf("\"" + key + "\"");
        if (arrStart == -1) return items;
        int bracketOpen = obj.indexOf('[', arrStart);
        int bracketClose = obj.indexOf(']', bracketOpen);
        String section = obj.substring(bracketOpen + 1, bracketClose).trim();
        if (section.isEmpty()) return items;
        for (String part : section.split(",")) {
            String clean = part.trim().replace("\"", "");
            if (!clean.isEmpty()) items.add(clean);
        }
        return items;
    }

    // JSON Helper
    private int parseInlineInt(String obj, String key) throws SaveCorruptedException {
        String search = "\"" + key + "\"";
        int idx = obj.indexOf(search);
        if (idx == -1) throw new SaveCorruptedException("Key not found: " + key);
        int colonIdx = obj.indexOf(':', idx);
        int start = colonIdx + 1;
        while (start < obj.length() && obj.charAt(start) == ' ') start++;
        int end = start;
        while (end < obj.length() && (Character.isDigit(obj.charAt(end)) || obj.charAt(end) == '-')) end++;
        return Integer.parseInt(obj.substring(start, end).trim());
    }

    private String parseInlineString(String obj, String key) throws SaveCorruptedException {
        String search = "\"" + key + "\"";
        int idx = obj.indexOf(search);
        if (idx == -1) throw new SaveCorruptedException("Key not found: " + key);
        int colonIdx = obj.indexOf(':', idx);
        int startQ = obj.indexOf('"', colonIdx + 1);
        int endQ = obj.indexOf('"', startQ + 1);
        if (startQ == -1 || endQ == -1) throw new SaveCorruptedException("Invalid string for: " + key);
        return obj.substring(startQ + 1, endQ);
    }

    private String parseInlineStringNullable(String obj, String key) {
        try {
            String val = parseInlineString(obj, key);
            return "null".equals(val) ? null : val;
        } catch (Exception e) {
            return null;
        }
    }

    // Indent Helper
    private String indent(int depth) {
        return "  ".repeat(depth);
    }

    private void appendField(StringBuilder sb, String key, String value, int depth) {
        sb.append(indent(depth)).append('"').append(key).append("\": ");
        if (value == null) sb.append("null");
        else sb.append('"').append(value).append('"');
        sb.append(",\n");
    }

    private void appendField(StringBuilder sb, String key, int value, int depth) {
        sb.append(indent(depth)).append('"').append(key).append("\": ").append(value).append(",\n");
    }

    private void appendField(StringBuilder sb, String key, boolean value, int depth) {
        sb.append(indent(depth)).append('"').append(key).append("\": ").append(value).append(",\n");
    }
}
