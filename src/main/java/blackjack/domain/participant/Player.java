package blackjack.domain.participant;

public class Player extends Participant {
    private static final String INVALID_NAME = "딜러";

    private Player(Name name) {
        super(name);
    }

    public static Player from(String name) {
        validateName(name);
        return new Player(new Name(name));
    }

    private static void validateName(String name) {
        if (INVALID_NAME.equals(name)) {
            throw new IllegalArgumentException("딜러라는 이름은 사용할 수 없습니다.");
        }
    }

    @Override
    public void initBetAmount(int value) {
        betAmount.initialize(value);
    }

    public boolean canHit() {
        return this.getState()
                   .isHit() || this.getState()
                                   .isStay();
    }

    public void multipleBetAmount(double times) {
        betAmount.multiple(times);
    }
}
