package com.redcreator37.WhitelistBot.DataModels;

import discord4j.common.util.Snowflake;

import java.time.Instant;
import java.util.Objects;

public class CacheState {

    private final Snowflake guildId;

    private final Instant lastRefresh;

    public CacheState(Snowflake guildId, Instant lastRefresh) {
        this.guildId = guildId;
        this.lastRefresh = lastRefresh;
    }

    public Snowflake getGuildId() {
        return guildId;
    }

    public Instant getLastRefresh() {
        return lastRefresh;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CacheState)) return false;
        CacheState that = (CacheState) o;
        return guildId.equals(that.guildId) && lastRefresh.equals(that.lastRefresh);
    }

    @Override
    public int hashCode() {
        return Objects.hash(guildId, lastRefresh);
    }

}
