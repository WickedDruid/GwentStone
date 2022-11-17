package fileio;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;

public class board {
    private ArrayList<CardInput>[] playedCards = new ArrayList[4];
    public board() {
        for(int i = 0; i < 4; i++)
            playedCards[i] = new ArrayList<CardInput>();
    }
    public void boardAdd(int playerIdx, CardInput card) {
        String type = card.getType(card);
        if(playerIdx == 1) {
            if(type.contains("front"))
                playedCards[2].add(card);
            if(type.contains("back"))
                playedCards[3].add(card);
        } else {
            if(type.contains("front"))
                playedCards[1].add(card);
            if(type.contains("back"))
                playedCards[0].add(card);
        }
    }
    public String checkAvailabilty(int playerIdx, CardInput card) {
        String type = card.getType(card);
        if(type.contains("environment"))
            return "Cannot place environment card on table.";
        if(playerIdx == 1) {
            if(type.contains("front") && playedCards[2].size() > 4)
                return "Cannot place card on table since row is full.";
            if(type.contains("back") && playedCards[3].size() > 4)
                return "Cannot place card on table since row is full.";
        } else {
            if(type.contains("front") && playedCards[1].size() > 4)
                return "Cannot place card on table since row is full.";
            if(type.contains("back") && playedCards[0].size() > 4)
                return "Cannot place card on table since row is full.";
        }
        return null;
    }

    public ArrayList<CardInput>[] getPlayedCards() {
        return playedCards;
    }

    public void setPlayedCards(ArrayList<CardInput>[] playedCards) {
        this.playedCards = playedCards;
    }
}
