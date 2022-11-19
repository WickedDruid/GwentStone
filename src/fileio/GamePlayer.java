package fileio;
import checker.Checker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import checker.CheckerConstants;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Random;

public class GamePlayer {
    public static ArrayNode gameStart(Input inputData, ArrayNode output, ObjectMapper objectMapper) {
        for(var index : inputData.getGames()) {
            board currentBoard = new board();
            int manaPlayerOne = 1, manaPlayerTwo = 1;
            int increment = 2;
            int handIdx;
            int xdef, ydef, xatt, yatt;
            CardInput card, target, playerOneHero, playerTwoHero;
            int belongsAtt, belongsDef;
            ObjectNode values;
            boolean gameEnded = false;
            playerOneHero = index.getStartGame().getPlayerOneHero();
            playerTwoHero = index.getStartGame().getPlayerTwoHero();
            playerOneHero.setHealth(30);
            playerTwoHero.setHealth(30);
            int turn = index.getStartGame().getStartingPlayer() + 2;
            int playerOneIdx = index.getStartGame().getPlayerOneDeckIdx();
            int playerTwoIdx = index.getStartGame().getPlayerTwoDeckIdx();
            Random rand = new Random(index.getStartGame().getShuffleSeed());
            Collections.shuffle(inputData.getPlayerOneDecks().getDecks().get(playerOneIdx), rand);
            rand = new Random(index.getStartGame().getShuffleSeed());
            Collections.shuffle(inputData.getPlayerTwoDecks().getDecks().get(playerTwoIdx), rand);
            inputData.getPlayerOneDecks().clearCards();
            inputData.getPlayerTwoDecks().clearCards();
            hands currentHands = new hands(inputData, playerOneIdx, playerTwoIdx);
            inputData.getPlayerOneDecks().getDecks().get(playerOneIdx).remove(0);
            inputData.getPlayerTwoDecks().getDecks().get(playerTwoIdx).remove(0);
            ObjectNode outputInterior = objectMapper.createObjectNode();
            for(var command : index.getActions()) {
                currentBoard.checkKilled();
                switch (command.getCommand()) {
                    case "getPlayerDeck":
                        outputInterior = objectMapper.createObjectNode();
                        ArrayNode deckJson = objectMapper.createArrayNode();
                        ArrayList<CardInput> currentDeck;
                        if (command.getPlayerIdx() == 1) {
                            currentDeck = inputData.getPlayerOneDecks().getDecks().get(playerOneIdx);
                        } else {
                            currentDeck = inputData.getPlayerTwoDecks().getDecks().get(playerTwoIdx);
                        }
                        for (var i : currentDeck) {
                            if(!i.isPlayed())
                                deckJson.add(i.getJson(objectMapper, i));
                        }
                        outputInterior.put("command", command.getCommand());
                        outputInterior.put("playerIdx", command.getPlayerIdx());
                        outputInterior.put("output", deckJson);
                        output.add(outputInterior);
                        break;
                    case "getPlayerHero":
                        outputInterior = objectMapper.createObjectNode();
                        outputInterior.put("command", command.getCommand());
                        outputInterior.put("playerIdx", command.getPlayerIdx());
                        if (command.getPlayerIdx() == 1) {
                            ObjectNode playerOneHeroJson = playerOneHero.getJson(objectMapper, playerOneHero);
                            outputInterior.put("output", playerOneHeroJson);
                        } else {
                            ObjectNode playerTwoHeroJson = playerTwoHero.getJson(objectMapper, playerTwoHero);
                            outputInterior.put("output", playerTwoHeroJson);
                        }
                        output.add(outputInterior);
                        break;
                    case "getPlayerTurn":
                        outputInterior = objectMapper.createObjectNode();
                        outputInterior.put("command", command.getCommand());
                        if (turn % 2 == 0)
                            outputInterior.put("output", 2);
                        else
                            outputInterior.put("output", 1);
                        output.add(outputInterior);
                        break;
                    case "getPlayerMana":
                        outputInterior = objectMapper.createObjectNode();
                        outputInterior.put("command", command.getCommand());
                        if(command.getPlayerIdx() == 2) {
                            outputInterior.put("output", manaPlayerTwo);
                        } else {
                            outputInterior.put("output", manaPlayerOne);
                        }
                        outputInterior.put("playerIdx", command.getPlayerIdx());
                        output.add(outputInterior);
                        break;
                    case "endPlayerTurn":
                        currentBoard.unfreeze();
                        if(turn % 2 != (index.getStartGame().getStartingPlayer() % 2)) {
                            currentBoard.clearUsed();
                            manaPlayerOne += increment;
                            manaPlayerTwo += increment;
                            if(increment < 10)
                                increment++;
                            ArrayList<CardInput> playerOneDeck;
                            playerOneDeck = inputData.getPlayerOneDecks().getDecks().get(playerOneIdx);
                            if(playerOneDeck.size() == 0)
                                break;
                            for(var k : inputData.getPlayerOneDecks().getDecks().get(playerOneIdx)) {
                                if (!k.isPlayed()) {
                                    currentHands.addPlayerOneHand(k);
                                    k.setPlayed(true);
                                    break;
                                }
                            }
                            ArrayList<CardInput> playerTwoDeck;
                            playerTwoDeck = inputData.getPlayerTwoDecks().getDecks().get(playerTwoIdx);
                            if(playerTwoDeck.size() == 0)
                                break;
                            for(var k : inputData.getPlayerTwoDecks().getDecks().get(playerTwoIdx)) {
                                if (!k.isPlayed()) {
                                    currentHands.addPlayerTwoHand(k);
                                    k.setPlayed(true);
                                    break;
                                }
                            }
                        }
                        turn++;
                        break;
                    case "placeCard":
                        handIdx = command.getHandIdx();
                        if(turn % 2 == 0) {
                            if (handIdx < currentHands.getPlayerTwoHand().size())
                                card = currentHands.getPlayerTwoHand().get(handIdx);
                            else {
                                System.out.println("error");
                                break;
                            }
                        } else {
                            if(handIdx < currentHands.getPlayerOneHand().size())
                                card = currentHands.getPlayerOneHand().get(handIdx);
                            else {
                                System.out.println("error");
                                break;
                            }
                        }
                        String available = null;
                        outputInterior = objectMapper.createObjectNode();
                        outputInterior.put("command", command.getCommand());
                        if(turn % 2 == 0) {
                            if(card.getMana() > manaPlayerTwo) {
                                available = "Not enough mana to place card on table.";
                            }
                            if(available == null)
                                available = currentBoard.checkAvailabilty(2, card);
                            if(available == null) {
                                manaPlayerTwo -= card.getMana();
                                currentBoard.boardAdd(2, card);
                                currentHands.playerTwoHand.remove(card);
                            } else {
                                outputInterior.put("error", available);
                                outputInterior.put("handIdx", handIdx);
                                output.add(outputInterior);
                            }
                        } else {
                            if(card.getMana() > manaPlayerOne) {
                                available = "Not enough mana to place card on table.";
                            }
                            if( available == null)
                                available = currentBoard.checkAvailabilty(1, card);
                            if(available == null) {
                                currentBoard.boardAdd(1, card);
                                manaPlayerOne -= card.getMana();
                                currentHands.playerOneHand.remove(card);
                            } else {
                                outputInterior.put("error", available);
                                outputInterior.put("handIdx", handIdx);
                                output.add(outputInterior);
                            }
                        }
                        break;
                    case "getCardsInHand":
                        outputInterior = objectMapper.createObjectNode();
                        outputInterior.put("command", command.getCommand());
                        outputInterior.put("playerIdx", command.getPlayerIdx());
                        ArrayNode handJson = objectMapper.createArrayNode();
                        ArrayList<CardInput> playerHand;
                        int playerIdx = command.getPlayerIdx();
                        if(playerIdx == 1) {
                            playerHand = currentHands.getPlayerOneHand();
                        } else {
                            playerHand = currentHands.getPlayerTwoHand();
                        }
                        for(var i : playerHand) {
                            handJson.add(i.getJson(objectMapper, i));
                        }
                        outputInterior.put("output", handJson);
                        output.add(outputInterior);
                        break;
                    case "getCardsOnTable":
                        ArrayList<CardInput>[] cards = currentBoard.getPlayedCards();
                        outputInterior = objectMapper.createObjectNode();
                        ArrayNode outputArray = objectMapper.createArrayNode();
                        ArrayNode exteriorArray = objectMapper.createArrayNode();
                        outputInterior.put("command", command.getCommand());
                        ArrayNode boardJson = objectMapper.createArrayNode();
                        for (int i = 0; i < 4; i++) {
                            for (var j : cards[i]) {
                                outputArray.add(j.getJson(objectMapper, j));
                            }
                            exteriorArray.add(outputArray);
                            outputArray = objectMapper.createArrayNode();
                        }
                        outputInterior.put("output", exteriorArray);
                        output.add(outputInterior);
                        break;
                    case "getCardAtPosition":
                        ObjectNode desiredCard = objectMapper.createObjectNode();
                        int x = command.getX();
                        int y = command.getY();
                        outputInterior = objectMapper.createObjectNode();
                        outputInterior.put("command", command.getCommand());
                        outputInterior.put("x", x);
                        outputInterior.put("y", y);
                        desiredCard = currentBoard.getPosition(x, y, objectMapper);
                        if(desiredCard == null)
                            outputInterior.put("output", "No card available at that position.");
                        else
                            outputInterior.put("output", desiredCard);
                        output.add(outputInterior);
                        break;
                    case "getEnvironmentCardsInHand":
                        outputInterior = objectMapper.createObjectNode();
                        ArrayNode cardsArray = objectMapper.createArrayNode();
                        outputInterior.put("command", command.getCommand());
                        outputInterior.put("playerIdx", command.getPlayerIdx());
                        ArrayList<CardInput> playerCards;
                        if(command.getPlayerIdx() == 2)
                            playerCards = currentHands.getPlayerTwoHand();
                        else
                            playerCards = currentHands.getPlayerOneHand();
                        for(var env : playerCards) {
                            if(env.getType(env).contains("environment"))
                                cardsArray.add(env.getJson(objectMapper, env));
                        }
                        outputInterior.put("output", cardsArray);
                        output.add(outputInterior);
                        break;
                    case "useEnvironmentCard":
                        handIdx = command.getHandIdx();
                        int row = command.getAffectedRow();
                        String error = "Cannot steal enemy card since the player's row is full.";
                        String error2 = "Cannot steal enemy card since the player's row is full.";
                        outputInterior = objectMapper.createObjectNode();
                        if(turn % 2 == 0) {
                            if (handIdx >= currentHands.playerTwoHand.size())
                                break;
                            card = currentHands.playerTwoHand.get(handIdx);

                            if(!card.getType(card).contains("environment")) {
                                outputInterior.put("affectedRow", row);
                                outputInterior.put("command", command.getCommand());
                                outputInterior.put("error", "Chosen card is not of type environment.");
                                outputInterior.put("handIdx", handIdx);
                                output.add(outputInterior);
                                break;
                            } else if(card.getMana() > manaPlayerTwo) {
                                outputInterior.put("affectedRow", row);
                                outputInterior.put("command", command.getCommand());
                                outputInterior.put("error", "Not enough mana to use environment card.");
                                outputInterior.put("handIdx", handIdx);
                                output.add(outputInterior);
                                break;
                            } else if(row == 0 || row == 1) {
                                outputInterior.put("affectedRow", row);
                                outputInterior.put("command", command.getCommand());
                                outputInterior.put("error", "Chosen row does not belong to the enemy.");
                                outputInterior.put("handIdx", handIdx);
                                output.add(outputInterior);
                                break;
                            } else {
                                switch (card.getName()) {
                                    case "Firestorm":
                                        currentBoard.playFirestorm(row);
                                        manaPlayerTwo -= card.getMana();
                                        currentHands.getPlayerTwoHand().remove(card);
                                        break;
                                    case "Winterfell":
                                        currentBoard.playWinterfell(row);
                                        manaPlayerTwo -= card.getMana();
                                        currentHands.getPlayerTwoHand().remove(card);
                                        break;
                                    case "Heart Hound":
                                        if(currentBoard.getPlayedCards()[0].size() > 4 && row == 3 ||
                                                currentBoard.getPlayedCards()[1].size() > 4 && row == 2) {
                                            outputInterior.put("affectedRow", row);
                                            outputInterior.put("command", command.getCommand());
                                            outputInterior.put("error", error);
                                            outputInterior.put("handIdx", handIdx);
                                            output.add(outputInterior);
                                            break;
                                        }
                                        manaPlayerTwo -= card.getMana();
                                        currentHands.getPlayerTwoHand().remove(card);
                                        currentBoard.playHeartHound(row);
                                        break;
                                }
                            }
                            break;
                        } else {
                            if(handIdx >= currentHands.playerOneHand.size())
                                break;
                            card = currentHands.playerOneHand.get(handIdx);
                            if(!card.getType(card).contains("environment")) {
                                outputInterior.put("affectedRow", row);
                                outputInterior.put("command", command.getCommand());
                                outputInterior.put("error", "Chosen card is not of type environment.");
                                outputInterior.put("handIdx", handIdx);
                                output.add(outputInterior);
                                break;
                            } else if(card.getMana() > manaPlayerOne) {
                                outputInterior.put("affectedRow", row);
                                outputInterior.put("command", command.getCommand());
                                outputInterior.put("error", "Not enough mana to use environment card.");
                                outputInterior.put("handIdx", handIdx);
                                output.add(outputInterior);
                                break;
                            } else if(row == 2 || row == 3) {
                                outputInterior.put("affectedRow", row);
                                outputInterior.put("command", command.getCommand());
                                outputInterior.put("error", "Chosen row does not belong to the enemy.");
                                outputInterior.put("handIdx", handIdx);
                                output.add(outputInterior);
                                break;
                            } else {
                                switch (card.getName()) {
                                    case "Firestorm":
                                        currentBoard.playFirestorm(row);
                                        manaPlayerOne -= card.getMana();
                                        currentHands.getPlayerOneHand().remove(card);
                                        break;
                                    case "Winterfell":
                                        currentBoard.playWinterfell(row);
                                        manaPlayerOne -= card.getMana();
                                        currentHands.getPlayerOneHand().remove(card);
                                        break;
                                    case "Heart Hound":
                                        if(currentBoard.getPlayedCards()[3].size() > 4 && row == 0 ||
                                                currentBoard.getPlayedCards()[2].size() > 4 && row == 1) {
                                            outputInterior.put("affectedRow", row);
                                            outputInterior.put("command", command.getCommand());
                                            outputInterior.put("error", error);
                                            outputInterior.put("handIdx", handIdx);
                                            output.add(outputInterior);
                                            break;
                                        }
                                        currentBoard.playHeartHound(row);
                                        manaPlayerOne -= card.getMana();
                                        currentHands.getPlayerOneHand().remove(card);
                                        break;
                                }
                            }
                        }
                        break;
                    case "getFrozenCardsOnTable":
                        outputInterior = objectMapper.createObjectNode();
                        outputInterior.put("command", command.getCommand());
                        ArrayNode frozenArray = objectMapper.createArrayNode();
                        for(var i : currentBoard.getFrozenCards())
                            frozenArray.add(i.getJson(objectMapper, i));
                        outputInterior.put("output", frozenArray);
                        output.add(outputInterior);
                        break;
                    case "cardUsesAttack":
                        xdef = command.getCardAttacked().getX();
                        ydef = command.getCardAttacked().getY();
                        xatt = command.getCardAttacker().getX();
                        yatt = command.getCardAttacker().getY();
                        if(currentBoard.getPlayedCards()[xatt].size() <= yatt)
                            break;
                        if(currentBoard.getPlayedCards()[xdef].size() <= ydef)
                            break;
                        card = currentBoard.getPlayedCards()[xatt].get(yatt);
                        target = currentBoard.getPlayedCards()[xdef].get(ydef);
                        outputInterior = objectMapper.createObjectNode();
                        values = objectMapper.createObjectNode();
                        outputInterior.put("command", command.getCommand());
                        values.put("x", xatt);
                        values.put("y", yatt);
                        outputInterior.put("cardAttacker", values);
                        values = objectMapper.createObjectNode();
                        values.put("x", command.getCardAttacked().getX());
                        values.put("y", command.getCardAttacked().getY());
                        outputInterior.put("cardAttacked", values);
                        if(turn % 2 == 0) {
                            if(command.getCardAttacked().getX() == 0 || command.getCardAttacked().getX() == 1) {
                                outputInterior.put("error",
                                        "Attacked card does not belong to the enemy.");
                                output.add(outputInterior);
                                break;
                            } else if(card.isUsed()) {
                                outputInterior.put("error",
                                        "Attacker card has already attacked this turn.");
                                output.add(outputInterior);
                                break;
                            } else if(currentBoard.getFrozenCards().contains(card)) {
                                outputInterior.put("error",
                                        "Attacker card is frozen.");
                                output.add(outputInterior);
                                break;
                            } else if(!currentBoard.checkTank(xdef, target)) {
                                outputInterior.put("error",
                                    "Attacked card is not of type 'Tank'.");
                                output.add(outputInterior);
                                break;
                            } else {
                                target.setHealth(target.getHealth() - card.getAttackDamage());
                                card.setUsed(true);
                            }
                        } else {
                            if(command.getCardAttacked().getX() == 2 || command.getCardAttacked().getX() == 3) {
                                outputInterior.put("error",
                                        "Attacked card does not belong to the enemy.");
                                output.add(outputInterior);
                                break;
                            } else if(card.isUsed()) {
                                outputInterior.put("error",
                                        "Attacker card has already attacked this turn.");
                                output.add(outputInterior);
                                break;
                            } else if(currentBoard.getFrozenCards().contains(card)) {
                                outputInterior.put("error",
                                        "Attacker card is frozen.");
                                output.add(outputInterior);
                                break;
                            } else if(!currentBoard.checkTank(xdef, target)) {
                                outputInterior.put("error",
                                        "Attacked card is not of type 'Tank'.");
                                output.add(outputInterior);
                                break;
                            } else {
                                target.setHealth(target.getHealth() - card.getAttackDamage());
                                card.setUsed(true);
                            }
                        }
                        break;
                    case "cardUsesAbility":
                        xdef = command.getCardAttacked().getX();
                        ydef = command.getCardAttacked().getY();
                        xatt = command.getCardAttacker().getX();
                        yatt = command.getCardAttacker().getY();
                        int extra;
                        if(currentBoard.getPlayedCards()[xatt].size() <= yatt)
                            break;
                        if(currentBoard.getPlayedCards()[xdef].size() <= ydef)
                            break;
                        card = currentBoard.getPlayedCards()[xatt].get(yatt);
                        target = currentBoard.getPlayedCards()[xdef].get(ydef);
                        outputInterior = objectMapper.createObjectNode();
                        values = objectMapper.createObjectNode();
                        outputInterior.put("command", command.getCommand());
                        values.put("x", xatt);
                        values.put("y", yatt);
                        outputInterior.put("cardAttacker", values);
                        values = objectMapper.createObjectNode();
                        values.put("x", command.getCardAttacked().getX());
                        values.put("y", command.getCardAttacked().getY());
                        outputInterior.put("cardAttacked", values);
                        if(xdef == 0 || xdef == 1)
                            belongsDef = 2;
                        else
                            belongsDef = 1;
                        if(xatt == 0 || xatt == 1)
                            belongsAtt = 2;
                        else
                            belongsAtt = 1;
                        if(card.isFrozen()) {
                            outputInterior.put("error",
                                    "Attacker card is frozen.");
                            output.add(outputInterior);
                            break;
                        } else if(card.isUsed()) {
                            outputInterior.put("error",
                                    "Attacker card has already attacked this turn.");
                            output.add(outputInterior);
                            break;
                        }else {
                            if(card.getName().equals("Disciple")) {
                                if (belongsDef != belongsAtt) {
                                outputInterior.put("error",
                                        "Attacked card does not belong to the current player.");
                                output.add(outputInterior);
                                break;
                                }
                            }
                            else {
                                if(belongsDef == belongsAtt) {
                                    outputInterior.put("error",
                                            "Attacked card does not belong to the enemy.");
                                    output.add(outputInterior);
                                    break;
                                }
                            }
                        }
                        if(!currentBoard.checkTank(xdef, target)) {
                            outputInterior.put("error",
                                    "Attacked card is not of type 'Tank'.");
                            output.add(outputInterior);
                            break;
                        }
                        card.setUsed(true);
                        switch (card.getName()) {
                            case "The Ripper":
                                target.setAttackDamage(target.getAttackDamage() - 2);
                                if(target.getAttackDamage() < 0)
                                    target.setAttackDamage(0);
                                break;
                            case "Miraj":
                                extra = card.getHealth();
                                card.setHealth(target.getHealth());
                                target.setHealth(extra);
                                break;
                            case "The Cursed One":
                                extra = target.getHealth();
                                target.setHealth(target.getAttackDamage());
                                target.setAttackDamage(extra);
                                break;
                            case "Disciple":
                                target.setHealth(target.getHealth() + 2);
                                break;
                        }
                        break;
                    case "useAttackHero":
                        outputInterior = objectMapper.createObjectNode();
                        xatt = command.getCardAttacker().getX();
                        yatt = command.getCardAttacker().getY();
                        if(currentBoard.getPlayedCards()[xatt].size() <= yatt)
                            break;
                        card = currentBoard.getPlayedCards()[xatt].get(yatt);
                        outputInterior.put("command", command.getCommand());
                        values = objectMapper.createObjectNode();
                        values.put("x", xatt);
                        values.put("y", yatt);
                        outputInterior.put("cardAttacker", values);
                        if(xatt == 0 || xatt == 1)
                            belongsAtt = 2;
                        else
                            belongsAtt = 1;
                        if(card.isFrozen()) {
                            outputInterior.put("error", "Attacker card is frozen.");
                            output.add(outputInterior);
                            break;
                        } else if(card.isUsed()) {
                            outputInterior.put("error",
                                    "Attacker card has already attacked this turn.");
                            output.add(outputInterior);
                            break;
                        } else {
                            if(belongsAtt == 2) {
                                if(!currentBoard.checkTank(2, playerOneHero)) {
                                    outputInterior.put("error",
                                            "Attacked card is not of type 'Tank'.");
                                    output.add(outputInterior);
                                    break;
                                }
                            } else {
                                if (!currentBoard.checkTank(1, playerTwoHero)) {
                                    outputInterior.put("error",
                                            "Attacked card is not of type 'Tank'.");
                                    output.add(outputInterior);
                                    break;
                                }
                            }
                        }
                        card.setUsed(true);
                        if(belongsAtt == 1) {
                            playerTwoHero.setHealth(playerTwoHero.getHealth() - card.getAttackDamage());
                        } else {
                            playerOneHero.setHealth(playerOneHero.getHealth() - card.getAttackDamage());
                        }
                        break;
                    default:
                        break;
                }
                if(playerOneHero.getHealth() < 1 && !gameEnded) {
                    outputInterior = objectMapper.createObjectNode();
                    outputInterior.put("gameEnded", "Player two killed the enemy hero.");
                    output.add(outputInterior);
                    gameEnded = true;
                } else if(playerTwoHero.getHealth() < 1 && !gameEnded){
                    outputInterior = objectMapper.createObjectNode();
                    outputInterior.put("gameEnded", "Player one killed the enemy hero.");
                    output.add(outputInterior);
                    gameEnded = true;
                }
            }
        }
        return output;
    }
}
