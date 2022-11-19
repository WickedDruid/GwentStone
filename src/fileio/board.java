package fileio;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;

public class board {
    private ArrayList<CardInput>[] playedCards = new ArrayList[4];
    private ArrayList<CardInput> frozenCards = new ArrayList<>();
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
    public void clearUsed() {
        for(var i : playedCards) {
            for(var j : i) {
                j.setUsed(false);
            }
        }
    }
    public void playFirestorm(int row) {
        for(var i : playedCards[row])
            i.setHealth(i.getHealth() - 1);
    }
    public void playWinterfell(int row) {
        for(var i : playedCards[row]) {
            frozenCards.add(i);
            i.setFrozen(true);
            i.setTimeFrozen(2);
        }
    }
    public void unfreeze() {
        for(int i = 0; i < frozenCards.size(); i++) {
            if(frozenCards.get(i).getTimeFrozen() == 0) {
                frozenCards.get(i).setFrozen(false);
                frozenCards.remove(i);
            } else
                frozenCards.get(i).setTimeFrozen(frozenCards.get(i).getTimeFrozen() - 1);
        }
    }
    public void playHeartHound(int row) {
        if(playedCards[row].size() < 1)
            return;
        CardInput card = playedCards[row].get(0);
        for(var i : playedCards[row])
            if(i.getHealth() > card.getHealth())
                card = i;
        if(row == 0) {
            boardAdd(1, card);
            playedCards[row].remove(card);
        } else if(row == 1) {
            boardAdd(1, card);
            playedCards[row].remove(card);
        } else if(row == 2) {
            boardAdd(2, card);
            playedCards[row].remove(card);
        } else {
            boardAdd(2, card);
            playedCards[row].remove(card);
        }
    }
    public void checkKilled() {
        for(int i = 0; i < 4; i++) {
            if(playedCards[i].size() > 0)
                for(int j = 0; j < playedCards[i].size(); j++) {
                    if (playedCards[i].get(j).getHealth() < 1) {
                        if(frozenCards.contains(j))
                            frozenCards.remove(j);
                        playedCards[i].remove(j);
                    }
                }
        }
    }
    public ObjectNode getPosition(int x, int y, ObjectMapper objectMapper) {
        if(playedCards[x].size() > y) {
            CardInput card = playedCards[x].get(y);
            return card.getJson(objectMapper, card);
        } else
            return null;
    }
    public boolean checkTank(int targetX, CardInput target) {
        if(targetX < 2) {
            if(!target.getType(target).contains("tank"))
                for(var j : playedCards[1]) {
                    if(j.getType(j).contains("tank"))
                        return false;
                }
        } else {
            if(!target.getType(target).contains("tank"))
                for(var j : playedCards[2]) {
                    if (j.getType(j).contains("tank"))
                        return false;
                }
        }
        return true;
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

    public ArrayList<CardInput> getFrozenCards() {
        return frozenCards;
    }

    public void setFrozenCards(ArrayList<CardInput> frozenCards) {
        this.frozenCards = frozenCards;
    }
}
