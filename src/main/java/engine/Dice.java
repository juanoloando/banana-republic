package engine;

import java.util.Random;

public class Dice {

    private static final int SIDES = 6;
    private static final int MIN = 1;

    private final Random random;
    private Integer manualDie1;
    private Integer manualDie2;
    private int lastRollDie1;
    private int lastRollDie2;

    public Dice() {
        this.random = new Random();
        this.manualDie1 = null;
        this.manualDie2 = null;
        this.lastRollDie1 = 0;
        this.lastRollDie2 = 0;
    }

    public int[] roll() {
        int die1 = (manualDie1 != null) ? manualDie1 : (random.nextInt(SIDES) + MIN);
        int die2 = (manualDie2 != null) ? manualDie2 : (random.nextInt(SIDES) + MIN);

        assert die1 >= MIN && die1 <= SIDES : "Die 1 result out of range: " + die1;
        assert die2 >= MIN && die2 <= SIDES : "Die 2 result out of range: " + die2;

        lastRollDie1 = die1;
        lastRollDie2 = die2;
        clearManualOverride();
        return new int[]{die1, die2};
    }

    public void setManualResult(int die1, int die2) {
        assert die1 >= MIN && die1 <= SIDES
                : "Manual die1 must be 1-6, got: " + die1;
        assert die2 >= MIN && die2 <= SIDES
                : "Manual die2 must be 1-6, got: " + die2;
        this.manualDie1 = die1;
        this.manualDie2 = die2;
    }

    public void clearManualOverride() {
        this.manualDie1 = null;
        this.manualDie2 = null;
    }

    public boolean hasManualOverride() {
        return manualDie1 != null || manualDie2 != null;
    }

    public int getLastTotal() {
        return lastRollDie1 + lastRollDie2;
    }

    public int getLastDie1() {
        return lastRollDie1;
    }

    public int getLastDie2() {
        return lastRollDie2;
    }
}
