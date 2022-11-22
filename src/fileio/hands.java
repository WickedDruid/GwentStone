package fileio;

import java.util.ArrayList;

public class hands {
    ArrayList<CardInput> playerOneHand, playerTwoHand;

    public hands(Input inputData, int indexPlayerOne, int indexPlayerTwo) {
        //initializes the hand of each player and adds the first card of their deck
        playerOneHand = new ArrayList<CardInput>();
        playerTwoHand = new ArrayList<CardInput>();
        playerOneHand.add(inputData.getPlayerOneDecks().getDecks().get(indexPlayerOne).get(0));
        playerTwoHand.add(inputData.getPlayerTwoDecks().getDecks().get(indexPlayerTwo).get(0));
    }
    public void addPlayerOneHand(CardInput card) {
        //adds the card to the hand of the first player
        playerOneHand.add(card);
    }
    public void addPlayerTwoHand(CardInput card) {
        //adds the card to the hand of the second player
        playerTwoHand.add(card);
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
