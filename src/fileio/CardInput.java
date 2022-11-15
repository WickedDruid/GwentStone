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

    public CardInput() {
    }

    public ObjectNode getJson(ObjectMapper objectmapper, CardInput card) {
        String type = getType(card);
        ObjectNode file = objectmapper.createObjectNode();
        file.put("mana", this.mana);
        if(type.equals("minion") || type.equals("special") || type.equals("tank")) {
            file.put("attackDamage", this.attackDamage);
            file.put("health", this.health);
        }
        file.put("description", this.description);
        file.putPOJO("colors", this.colors);
        file.put("name", this.name);
        if(type.equals("hero")) {
            card.setHealth(30);
            file.put("health", this.health);
        }
        return file;
    }
    public String getType(CardInput card) {
        switch (card.name) {
            case "Sentinel":
                return "minion";
            case "Berserker":
                return "minion";
            case "Goliath":
                return "tank";
            case "Warden":
                return "tank";
            case "Miraj":
                return "special";
            case "The Ripper":
                return "special";
            case "Disciple":
                return "special";
            case "The Cursed One":
                return "special";
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
