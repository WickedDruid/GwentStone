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
            CardInput card;
            int turn = index.getStartGame().getStartingPlayer() + 2;
            int playerOneIdx = index.getStartGame().getPlayerOneDeckIdx();
            int playerTwoIdx = index.getStartGame().getPlayerTwoDeckIdx();
            Random rand = new Random(index.getStartGame().getShuffleSeed());
            Collections.shuffle(inputData.getPlayerOneDecks().getDecks().get(playerOneIdx), rand);
            rand = new Random(index.getStartGame().getShuffleSeed());
            Collections.shuffle(inputData.getPlayerTwoDecks().getDecks().get(playerTwoIdx), rand);
            hands currentHands = new hands(inputData, playerOneIdx, playerTwoIdx);
            inputData.getPlayerOneDecks().getDecks().get(playerOneIdx).remove(0);
            inputData.getPlayerTwoDecks().getDecks().get(playerTwoIdx).remove(0);
            ObjectNode outputInterior = objectMapper.createObjectNode();
            for(var command : index.getActions()) {
                currentBoard.checkKilled();
                currentBoard.unfreeze();
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
                            ObjectNode playerOneHero = index.getStartGame().getPlayerOneHero().getJson(objectMapper, index.getStartGame().getPlayerOneHero());
                            outputInterior.put("output", playerOneHero);
                        } else {
                            ObjectNode playerTwoHero = index.getStartGame().getPlayerTwoHero().getJson(objectMapper, index.getStartGame().getPlayerTwoHero());
                            outputInterior.put("output", playerTwoHero);
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
                        //TODO remove frozen cards that were frozen;
                        if(turn % 2 == 0) {
                            ArrayList<CardInput> playerOneDeck;
                            playerOneDeck = inputData.getPlayerOneDecks().getDecks().get(playerOneIdx);
                            if(playerOneDeck.size() == 0)
                                break;
                            currentHands.addPlayerOneHand(playerOneDeck.get(0));
                            inputData.getPlayerOneDecks().getDecks().get(playerOneIdx).remove(0);
                        } else {
                            ArrayList<CardInput> playerTwoDeck;
                            playerTwoDeck = inputData.getPlayerTwoDecks().getDecks().get(playerTwoIdx);
                            if(playerTwoDeck.size() == 0)
                                break;
                            currentHands.addPlayerTwoHand(playerTwoDeck.get(0));
                            inputData.getPlayerTwoDecks().getDecks().get(playerTwoIdx).remove(0);
                        }
                        if(turn % 2 != (index.getStartGame().getStartingPlayer() % 2)) {
                            manaPlayerOne += increment;
                            manaPlayerTwo += increment;
                            if(increment < 10)
                                increment++;
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
                    default:
                        break;
                }
            }
        }
        return output;
    }
}
