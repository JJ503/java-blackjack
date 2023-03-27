package blackjack.controller;

import blackjack.domain.BetAmount;
import blackjack.domain.BlackjackGame;
import blackjack.domain.participant.Dealer;
import blackjack.domain.participant.Participant;
import blackjack.domain.participant.Participants;
import blackjack.domain.participant.Player;
import blackjack.dto.ParticipantCardsDto;
import blackjack.dto.ParticipantCardsResultDto;
import blackjack.dto.ParticipantGameResultDto;
import blackjack.dto.PlayerNamesDto;
import blackjack.view.InputView;
import blackjack.view.OutputView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class BlackjackController {
    private final InputView inputView;
    private final OutputView outputView;

    public BlackjackController(InputView inputView, OutputView outputView) {
        this.inputView = inputView;
        this.outputView = outputView;
    }

    public void run() {
        try {
            Participants participants = getParticipants();
            List<BetAmount> betAmounts = getBetAmounts(participants);
            BlackjackGame blackjackGame = BlackjackGame.of(participants, betAmounts);
            startGame(blackjackGame, participants);
        } catch (IllegalArgumentException exception) {
            outputView.printException(exception.getMessage());
        }
    }

    private Participants getParticipants() {
        return retryWhenException(() -> {
            List<String> names = inputView.readNames();
            return Participants.from(names);
        });
    }

    private List<BetAmount> getBetAmounts(Participants participants) {
        List<BetAmount> betAmounts = new ArrayList<>();
        for (Player player : participants.getPlayers()) {
            betAmounts.add(getBetAmountEachPlayer(player));
        }
        return betAmounts;
    }

    private BetAmount getBetAmountEachPlayer(Player player) {
        return retryWhenException(() -> {
            int betAmount = inputView.readBetAmount(player.getName());
            return BetAmount.fromPlayer(betAmount);
        });
    }

    private void startGame(BlackjackGame blackjackGame, Participants participants) {
        blackjackGame.dealOutCard();
        printInitGame(participants);
        play(participants, blackjackGame);
        printResult(blackjackGame, participants);
    }

    private void play(Participants participants, BlackjackGame blackjackGame) {
        for (Player player : participants.getPlayers()) {
            playEachPlayer(player, blackjackGame);
        }
        Dealer dealer = participants.getDealer();
        playDealer(dealer, blackjackGame);
    }

    private void playEachPlayer(Player player, BlackjackGame blackjackGame) {
        GameCommand command = GameCommand.PLAY;
        while (player.canHit() && command.isPlay()) {
            command = getCommand(player);
            giveCard(player, blackjackGame, command);
            outputView.printPlayerCards(ParticipantCardsDto.from(player));
        }
    }

    private GameCommand getCommand(Player player) {
        return retryWhenException(() -> {
            String inputCommand = inputView.readIsContinue(player.getName());
            return GameCommand.from(inputCommand);
        });
    }

    private void giveCard(Player player, BlackjackGame blackjackGame, GameCommand command) {
        if (command.isPlay()) {
            blackjackGame.giveCard(player);
        }
    }

    private void playDealer(Dealer dealer, BlackjackGame blackjackGame) {
        while (dealer.isHit()) {
            outputView.printDealerState();
            blackjackGame.giveCard(dealer);
        }
    }

    private void printInitGame(Participants participants) {
        outputView.printInitCardsMessage(PlayerNamesDto.from(participants.getPlayers()));

        Dealer dealer = participants.getDealer();
        outputView.printDealerInitCards(ParticipantCardsDto.from(dealer));
        for (Player player : participants.getPlayers()) {
            outputView.printPlayerCards(ParticipantCardsDto.from(player));
        }
    }

    private void printResult(BlackjackGame blackjackGame, Participants participants) {
        printCardResult(participants);
        printGameResult(blackjackGame, participants);
    }

    private void printCardResult(Participants participants) {
        for (Participant participant : participants.getParticipants()) {
            outputView.printEachParticipantCardsResult(ParticipantCardsResultDto.of(participant));
        }
    }

    private void printGameResult(BlackjackGame blackjackGame, Participants participants) {
        outputView.printGameResultMessage();
        blackjackGame.calculateBetAmount();
        printDealerResult(blackjackGame, participants.getDealer());
        printPlayersResult(blackjackGame, participants.getPlayers());
    }

    private void printDealerResult(BlackjackGame blackjackGame, Dealer dealer) {
        outputView.printEachParticipantGameResult(
                ParticipantGameResultDto.of(dealer, blackjackGame.dealerResult()));
    }

    private void printPlayersResult(BlackjackGame blackjackGame, List<Player> players) {
        for (Player player : players) {
            outputView.printEachParticipantGameResult(
                    ParticipantGameResultDto.of(player, blackjackGame.resultEachPlayer(player)));
        }
    }

    private <T> T retryWhenException(Supplier<T> supplier) {
        T result;
        do {
            result = getSupplier(supplier);
        } while (result == null);

        return result;
    }

    private <T> T getSupplier(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (IllegalArgumentException exception) {
            outputView.printException(exception.getMessage());
            return null;
        }
    }
}
