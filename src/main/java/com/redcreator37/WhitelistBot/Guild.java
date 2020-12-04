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
     * The role of the guild members required to alter the data for
     * this guild
     */
    private final String adminRole;

    /**
     * Constructs a new Guild instance
     *
     * @param id        the guild's database id
     * @param snowflake the guild's snowflake
     * @param joined    the guild's join date in an ISO 8601-compliant
     *                  format
     * @param adminRole the role required to edit the data for this
     *                  guild
     * @param db        the database connection
     */
    public Guild(int id, Snowflake snowflake, String joined, String adminRole, Connection db) {
        this.id = id;
        this.snowflake = snowflake;
        this.joined = joined;
        this.adminRole = adminRole;
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

    public String getAdminRole() {
        return adminRole;
    }

}
