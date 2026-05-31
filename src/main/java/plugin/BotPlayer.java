package plugin;

import engine.GameEngine;
import engine.GameState;
import model.player.HumanPlayer;
import model.player.PlayerColor;

/**
/**
 * BotPlayer implementasi dari HumanPlayer yang menggunakan PlyerStrategy buat ambil keputusan. Jadi, BotPlayer itu seperti pemain manusia biasa, tapi cara dia mikir buat ambil keputusan itu beda karena dia pake strategi yang sudah di setup
 */
public class BotPlayer extends HumanPlayer {

    private PlayerStrategy strategy;

    public BotPlayer(String name, PlayerColor color, PlayerStrategy strategy) {
        super(name, color);
        assert strategy != null : "BotPlayer tidak boleh dibuat dengan strategy null";
        this.strategy = strategy;
    }


    public void executeTurn(GameState state, GameEngine engine) {
        assert state != null : "GameState tidak boleh null saat menjalankan giliran bot";
        assert engine != null : "GameEngine tidak boleh null saat menjalankan giliran bot";
        strategy.takeTurn(state, this);
    }

    public void setStrategy(PlayerStrategy strategy) {
        assert strategy != null : "Strategy tidak boleh null";
        this.strategy = strategy;
    }

    public PlayerStrategy getStrategy() {
        return strategy;
    }

    public boolean isBot() {
        return true;
    }

    @Override
    public String toString() {
        return "BotPlayer{name='" + getName() + "', strategy='"
            + strategy.getStrategyName() + "', color=" + getColor() + "}";
    }
}
