package main;
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
            Random rand = new Random(index.getStartGame().getShuffleSeed());
            Collections.shuffle(inputData.getPlayerOneDecks().getDecks().get(index.getStartGame().getPlayerOneDeckIdx()), rand);
            rand = new Random(index.getStartGame().getShuffleSeed());
            Collections.shuffle(inputData.getPlayerTwoDecks().getDecks().get(index.getStartGame().getPlayerTwoDeckIdx()), rand);

            for(var command : index.getActions()) {
                int playerOneIdx = index.getStartGame().getPlayerOneDeckIdx();
                int playerTwoIdx = index.getStartGame().getPlayerTwoDeckIdx();
                switch (command.getCommand()) {
                    case "getPlayerDeck":
                        ObjectNode outputInterior = objectMapper.createObjectNode();
                        ArrayNode deckJson = objectMapper.createArrayNode();
                        ArrayList<CardInput> currentDeck;
                        if(command.getPlayerIdx() == 1) {
                            currentDeck = inputData.getPlayerOneDecks().getDecks().get(playerOneIdx);
                        } else {
                            currentDeck = inputData.getPlayerTwoDecks().getDecks().get(playerTwoIdx);
                        }
                        for(var i : currentDeck) {
                            deckJson.add(i.getJson(objectMapper));
                        }
                        outputInterior.put("command", command.getCommand());
                        outputInterior.put("playerIdx", command.getPlayerIdx());
                        System.out.println(command.getPlayerIdx());
                        outputInterior.put("output", deckJson);
                        output.add(outputInterior);
                        break;
                    case "getPlayerHero":
                        outputInterior = objectMapper.createObjectNode();
                        outputInterior.put("command", command.getCommand());
                        outputInterior.put("playerIdx", command.getPlayerIdx());
                        if(command.getPlayerIdx() == 1) {
                            ObjectNode playerOneHero = index.getStartGame().getPlayerOneHero().getJson(objectMapper);
                            outputInterior.put("output", playerOneHero);
                        } else {
                            ObjectNode playerTwoHero = index.getStartGame().getPlayerTwoHero().getJson(objectMapper);
                            outputInterior.put("output", playerTwoHero);
                        }
                        output.add(outputInterior);
                        break;
                }
            }
        }
        return output;
    }
}
