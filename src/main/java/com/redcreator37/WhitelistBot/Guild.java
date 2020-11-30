package com.redcreator37.WhitelistBot;

import discord4j.common.util.Snowflake;

import java.sql.Connection;
import java.util.List;

/**
 * Represents a Discord guild ("server")
 */
@SuppressWarnings("unused")
public class Guild {

    /**
     * This guild's database id
     */
    private final int id;

    /**
     * This guild's unique snowflake
     */
    private final Snowflake snowflake;

    /**
     * The date when the bot has joined this guild in an ISO-8601
     * compliant format
     */
    private final String joined;

    /**
     * The rank of the guild members required to alter the data for
     * this guild
     */
    private final String rank;

    /**
     * Constructs a new Guild instance
     *
     * @param id        the guild's database id
     * @param snowflake the guild's snowflake
     * @param joined    the guild's join date in an ISO 8601-compliant
     *                  format
     * @param rank      the rank required to edit the data for this
     *                  guild
     * @param db        the database connection
     */
    public Guild(int id, Snowflake snowflake, String joined, String rank, Connection db) {
        this.id = id;
        this.snowflake = snowflake;
        this.joined = joined;
        this.rank = rank;
    }

    public int getId() {
        return id;
    }

    public Snowflake getSnowflake() {
        return snowflake;
    }

    public String getJoined() {
        return joined;
    }

    public String getRank() {
        return rank;
    }

    /**
     * Returns the guild with this snowflake or <code>null</code> if
     * such guild was not found in this list
     *
     * @param snowflake the snowflake id of the guild to look for
     * @param guilds    the list of all registered guilds
     * @return the matching Guild object or <code>null</code> if not
     * found
     */
    public static Guild guildBySnowflake(Snowflake snowflake, List<Guild> guilds) {
        return guilds.stream().filter(guild -> guild.getSnowflake()
                .equals(snowflake))
                .findAny().orElse(null);
    }

}
