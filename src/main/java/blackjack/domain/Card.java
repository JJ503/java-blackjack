package blackjack.domain;

import java.util.Objects;

public class Card {

    private final CardSuit cardSuit;
    private final CardNumber cardNumber;

    public Card(CardSuit cardSuit, CardNumber cardNumber) {
        this.cardSuit = cardSuit;
        this.cardNumber = cardNumber;
    }

    public String getCardName() {
        return this.cardNumber.getValue() + this.cardSuit.getValue();
    }

    public boolean isAce() {
        return this.cardNumber.isAce();
    }

    public CardSuit getSuit() {
        return cardSuit;
    }

    public CardNumber getNumber() {
        return cardNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return cardSuit == card.cardSuit && cardNumber == card.cardNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardSuit, cardNumber);
    }
}
