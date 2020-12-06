package com.redcreator37.WhitelistBot.DataModels;

import java.util.Objects;

/**
 * Represents a whitelisted player in the game database
 */
public class WhitelistedPlayer {

    /**
     * The identifier of the player (ie. their SteamID)
     */
    private final String identifier;

    /**
     * Constructs a new WhitelistedPlayer instance
     *
     * @param identifier the identifier of the player (ie. their SteamID)
     */
    public WhitelistedPlayer(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WhitelistedPlayer)) return false;
        WhitelistedPlayer that = (WhitelistedPlayer) o;
        return identifier.equals(that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }

}
