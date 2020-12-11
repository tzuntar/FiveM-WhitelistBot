package com.redcreator37.WhitelistBot.DataModels;

import com.redcreator37.WhitelistBot.Database.GameHandling.FiveMDb;
import com.redcreator37.WhitelistBot.Database.GameHandling.SharedDbProvider;
import discord4j.common.util.Snowflake;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents exactly one Discord {@link discord4j.core.object.entity.Guild}
 * (called a "server" in the UI).
 * <p>
 * All data, required to connect to external databases successfully and
 * transiently handle errors is handled by this class.
 */
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
     * The date when the bot has joined this guild
     */
    private final Instant joined;

    /**
     * The role of the guild members required to retrieve / alter the
     * data for this guild
     */
    private String adminRole;

    /**
     * The connection information for the shared database
     */
    private SharedDbProvider sharedDbProvider;

    /**
     * The shared MySQL database with all game data
     */
    private FiveMDb fiveMDb;

    /**
     * A list of all whitelisted players in this guild
     */
    private List<WhitelistedPlayer> whitelisted;

    /**
     * Constructs a new Guild instance
     * <p>
     * This constructor is meant to be used <i>exclusively</i> when
     * registering new guilds, as it doesn't require additional data to
     * be present yet. When possible, use the full constructor
     * {@link Guild#Guild(int, Snowflake, Instant, String, SharedDbProvider)}.
     *
     * @param id        the guild's database id
     * @param snowflake the guild's snowflake
     * @param joined    the guild's join date in an ISO 8601-compliant
     *                  format
     */
    public Guild(int id, Snowflake snowflake, Instant joined) {
        this.id = id;
        this.snowflake = snowflake;
        this.joined = joined;
    }

    /**
     * Constructs a new Guild instance.
     * <p>
     * Shared database connection and related fields (ex. the list of
     * whitelisted players) are manually lazily-initialized using the
     * {@link Guild#connectSharedDb()} method.
     *
     * @param id        the guild's database id
     * @param snowflake the guild's snowflake
     * @param joined    the guild's join date
     * @param adminRole the role required to edit the data for this
     *                  guild
     * @param db        the database data provider
     */
    public Guild(int id, Snowflake snowflake, Instant joined, String adminRole, SharedDbProvider db) {
        this.id = id;
        this.snowflake = snowflake;
        this.joined = joined;
        this.adminRole = adminRole;
        this.sharedDbProvider = db;
    }

    /**
     * Connects to the shared game database, registered in this guild
     *
     * @throws SQLException on errors
     */
    public void connectSharedDb() throws SQLException {
        fiveMDb = new FiveMDb(sharedDbProvider.connect());
        whitelisted = fiveMDb.getWhitelistedPlayers();
    }

    /**
     * Uses the connection to the in-game database to whitelist the
     * player with this SteamID
     *
     * @param playerId the SteamID of the player to whitelist
     * @return an empty {@link Optional} on success or the error
     * message
     */
    public Optional<String> whitelistPlayer(String playerId) {
        try {
            fiveMDb.whitelistPlayer(new WhitelistedPlayer(playerId));
            whitelisted.add(new WhitelistedPlayer(playerId));
        } catch (SQLException e) {
            return Optional.of(e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Uses the connection to the in-game database to remove the player
     * from its whitelist
     *
     * @param playerId the SteamID of the player to un-whitelist
     * @return an empty {@link Optional} on success or the error
     * message
     */
    public Optional<String> unlistPlayer(String playerId) {
        try {
            fiveMDb.removePlayer(new WhitelistedPlayer(playerId));
            whitelisted.remove(new WhitelistedPlayer(playerId));
        } catch (SQLException e) {
            return Optional.of(e.getMessage());
        }
        return Optional.empty();
    }

    public Snowflake getSnowflake() {
        return snowflake;
    }

    public Instant getJoined() {
        return joined;
    }

    public String getAdminRole() {
        return adminRole;
    }

    public SharedDbProvider getSharedDbProvider() {
        return sharedDbProvider;
    }

    public List<WhitelistedPlayer> getWhitelisted() {
        return whitelisted;
    }

    public void setAdminRole(String adminRole) {
        this.adminRole = adminRole;
    }

    public void setSharedDbProvider(SharedDbProvider sharedDbProvider) {
        this.sharedDbProvider = sharedDbProvider;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Guild)) return false;
        Guild guild = (Guild) o;
        return id == guild.id && snowflake.equals(guild.snowflake)
                && joined.equals(guild.joined) && Objects.equals(adminRole, guild.adminRole)
                && Objects.equals(sharedDbProvider, guild.sharedDbProvider)
                && Objects.equals(fiveMDb, guild.fiveMDb)
                && Objects.equals(whitelisted, guild.whitelisted);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, snowflake, joined, adminRole, sharedDbProvider, fiveMDb, whitelisted);
    }

}
