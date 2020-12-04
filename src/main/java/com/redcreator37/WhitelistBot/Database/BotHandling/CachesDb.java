package com.redcreator37.WhitelistBot.Database.BotHandling;

import com.redcreator37.WhitelistBot.DataModels.CacheState;
import discord4j.common.util.Snowflake;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;

public class CachesDb {

    /**
     * The SQLite database connection to use for all database-related
     * operations
     */
    private final Connection con;

    /**
     * Constructs a new CachesDb instance
     *
     * @param con connection to the SQLite database to use
     */
    public CachesDb(Connection con) {
        this.con = con;
    }

    /**
     * Returns the map of all cache refreshes per guild
     *
     * @return the map of all cache refreshes
     * @throws SQLException on errors
     */
    public HashMap<Snowflake, CacheState> getCacheState() throws SQLException {
        HashMap<Snowflake, CacheState> states = new HashMap<>();
        ResultSet set = con.createStatement().executeQuery("select * from caches");
        while (set.next()) {
            Snowflake s = Snowflake.of(set.getString("guild_id"));
            Instant i = Instant.parse(set.getString("last_refresh"));
            states.put(s, new CacheState(s, i));
        }
        set.close();
        return states;
    }

    /**
     * Logs the first cache refresh for this guild in the database.
     * Use only when caching data for this guild for the first time
     * otherwise the unique constraint will fail!
     *
     * @param guildId the snowflake id of the guild to add
     * @throws SQLException on errors
     * @see CachesDb#logRefresh(CacheState)
     */
    public void logFirstRefresh(Snowflake guildId) throws SQLException {
        PreparedStatement st = con.prepareStatement("INSERT INTO" +
                " caches(guild_id, last_refresh) VALUES(?, ?);");
        st.closeOnCompletion();
        st.setString(1, guildId.asString());
        st.setString(2, Instant.now().toString());
        st.executeUpdate();
    }

    /**
     * Logs this cache refresh in the database
     *
     * @param state the new cache state
     * @throws SQLException on errors
     */
    public void logRefresh(CacheState state) throws SQLException {
        PreparedStatement st = con.prepareStatement("UPDATE caches SET"
                + " last_refresh = ? WHERE guild_id = ?;");
        st.closeOnCompletion();
        st.setString(1, state.getLastRefresh().toString());
        st.setString(2, state.getGuildId().asString());
        st.executeUpdate();
    }

    /**
     * Removes the cache refresh data for the guild with this id from
     * the database
     *
     * @param guildId the guild to remove the data for
     * @throws SQLException on errors
     */
    public void clearCacheData(Snowflake guildId) throws SQLException {
        PreparedStatement st = con.prepareStatement("DELETE FROM caches"
                + " WHERE guild_id = ?;");
        st.setString(1, guildId.asString());
        st.executeUpdate();
    }

}
