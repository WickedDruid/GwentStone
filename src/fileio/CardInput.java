package fileio;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;

public final class CardInput {
    private int mana;
    private int attackDamage;
    private int health;
    private String description;
    private ArrayList<String> colors;
    private String name;
    private int timeFrozen = 0;

    private boolean frozen = false;
    private boolean used = false;
    private boolean played = false;

    public CardInput() {
    }

    public ObjectNode getJson(ObjectMapper objectmapper, CardInput card) {
        //returns the card object in json form using jackson
        String type = getType(card);
        ObjectNode file = objectmapper.createObjectNode();
        file.put("mana", this.mana);
        if(type.contains("minion") || type.contains("special") || type.contains("tank")) {
            file.put("attackDamage", this.attackDamage);
            file.put("health", this.health);
        }
        file.put("description", this.description);
        file.putPOJO("colors", this.colors);
        file.put("name", this.name);
        if(type.equals("hero")) {
            file.put("health", this.health);
        }
        return file;
    }
    public String getType(CardInput card) {
        //returns what type the card is, if it's a tank and in which row it's supposed to go
        switch (card.name) {
            case "Sentinel":
                return "minionback";
            case "Berserker":
                return "minionback";
            case "Goliath":
                return "tankfront";
            case "Warden":
                return "tankfront";
            case "Miraj":
                return "specialfront";
            case "The Ripper":
                return "specialfront";
            case "Disciple":
                return "specialback";
            case "The Cursed One":
                return "specialback";
            case "Firestorm":
                return "environment";
            case "Winterfell":
                return "environment";
            case "Heart Hound":
                return "environment";
            default:
                return "hero";

        }
    }
    public int getMana() {
        return mana;
    }

    public void setMana(final int mana) {
        this.mana = mana;
    }

    public int getAttackDamage() {
        return attackDamage;
    }

    public void setAttackDamage(final int attackDamage) {
        this.attackDamage = attackDamage;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(final int health) {
        this.health = health;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public ArrayList<String> getColors() {
        return colors;
    }

    public void setColors(final ArrayList<String> colors) {
        this.colors = colors;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public boolean isUsed() {
        return used;
    }

    public boolean isPlayed() {
        return played;
    }

    public void setPlayed(boolean played) {
        this.played = played;
    }

    public int getTimeFrozen() {
        return timeFrozen;
    }

    public void setTimeFrozen(int timeFrozen) {
        this.timeFrozen = timeFrozen;
    }

    @Override
    public String toString() {
        return "CardInput{"
                +  "mana="
                + mana
                +  ", attackDamage="
                + attackDamage
                + ", health="
                + health
                +  ", description='"
                + description
                + '\''
                + ", colors="
                + colors
                + ", name='"
                +  ""
                + name
                + '\''
                + '}';
    }
}
