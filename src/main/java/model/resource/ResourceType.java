package model.resource;

public enum ResourceType {
    WOOD,
    BRICK,
    WHEAT,
    ORE,
    BANANA;

    public String getDisplayName() {
        return switch (this) {
            case WOOD -> "Kayu";
            case BRICK -> "Batu Bata";
            case WHEAT -> "Gandum";
            case ORE -> "Bijih";
            case BANANA -> "Pisang";
        };
    }

    // public String getIcon() {
    //     return switch (this) {
    //         case WOOD   -> "🪵";
    //         case BRICK  -> "🧱";
    //         case WHEAT  -> "🌾";
    //         case ORE    -> "⛏️";
    //         case BANANA -> "🍌";
    //     };
    // }
}
