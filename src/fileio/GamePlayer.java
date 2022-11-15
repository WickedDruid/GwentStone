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
                switch (command.getCommand()) {
                    case "getPlayerDeck":
                        outputInterior = objectMapper.createObjectNode();
                        ArrayNode deckJson = objectMapper.createArrayNode();
                        ArrayList<CardInput> currentDeck;
                        if(command.getPlayerIdx() == 1) {
                            currentDeck = inputData.getPlayerOneDecks().getDecks().get(playerOneIdx);
                        } else {
                            currentDeck = inputData.getPlayerTwoDecks().getDecks().get(playerTwoIdx);
                        }
                        for(var i : currentDeck) {
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
                        if(command.getPlayerIdx() == 1) {
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
                        if(turn % 2 == 0)
                            outputInterior.put("output", 2);
                        else
                            outputInterior.put("output", 1);
                        output.add(outputInterior);
                }
            }
        }
        return output;
    }
}
