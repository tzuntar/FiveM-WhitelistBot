package com.redcreator37.WhitelistBot.DataModels;

import java.util.Objects;

public class WhitelistedPlayer {
    private final String identifier;

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
