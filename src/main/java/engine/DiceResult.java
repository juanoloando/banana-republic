package engine;

public final class DiceResult {

    private final int die1;
    private final int die2;

    public DiceResult(int die1, int die2) {
        assert die1 >= 1 && die1 <= 6 : "Die1 out of range: " + die1;
        assert die2 >= 1 && die2 <= 6 : "Die2 out of range: " + die2;
        this.die1 = die1;
        this.die2 = die2;
    }

    public int getDie1() {
        return die1;
    }

    public int getDie2() {
        return die2;
    }

    public int getTotal() {
        return die1 + die2;
    }

    public boolean isRobberActivated() {
        return getTotal() == 7;
    }

    @Override
    public String toString() {
        return "DiceResult{die1=" + die1 + ", die2=" + die2 + ", total=" + getTotal() + "}";
    }
}
