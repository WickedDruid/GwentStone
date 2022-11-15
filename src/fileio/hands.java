package fileio;

import java.util.ArrayList;

public class hands {
    ArrayList<CardInput> playerOneHand, playerTwoHand;

    public hands(Input inputData, int indexPlayerOne, int indexPlayerTwo) {
        playerOneHand = new ArrayList<CardInput>();
        playerTwoHand = new ArrayList<CardInput>();
        playerOneHand.add(inputData.getPlayerOneDecks().getDecks().get(indexPlayerOne).get(0));
        playerOneHand.add(inputData.getPlayerTwoDecks().getDecks().get(indexPlayerTwo).get(0));
    }
    public void addPlayerOneHand(CardInput card) {
        playerOneHand.add(card);
    }
    public void setPlayerOneHand(ArrayList<CardInput> playerOneHand) {
        this.playerOneHand = playerOneHand;
    }

    public void setPlayerTwoHand(ArrayList<CardInput> playerTwoHand) {
        this.playerTwoHand = playerTwoHand;
    }

    public ArrayList<CardInput> getPlayerOneHand() {
        return playerOneHand;
    }

    public ArrayList<CardInput> getPlayerTwoHand() {
        return playerTwoHand;
    }
}
