package model.player;

public enum PlayerColor {
    RED, BLUE, GREEN, YELLOW;

    public String getDisplayName() {
        return switch (this) {
            case RED    -> "Merah";
            case BLUE   -> "Biru";
            case GREEN  -> "Hijau";
            case YELLOW -> "Kuning";
        };
    }

    public String getCssName() {
        return switch (this) {
            case YELLOW -> "yellow";
            case BLUE   -> "blue";
            case RED    -> "red";
            case GREEN  -> "green";
        };
    }
}
