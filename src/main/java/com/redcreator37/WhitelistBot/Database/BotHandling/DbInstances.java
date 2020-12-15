package com.redcreator37.WhitelistBot.Database.BotHandling;

import com.redcreator37.WhitelistBot.Database.GameHandling.SharedDbProvider;
import discord4j.common.util.Snowflake;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbInstances {

    /**
     * The SQLite database connection to use for all database-related
     * operations
     */
    private final Connection con;

    /**
     * Constructs a new DbInstances instance
     *
     * @param con connection to the SQLite database to use
     */
    public DbInstances(Connection con) {
        this.con = con;
    }

    public SharedDbProvider getByGuild(Snowflake guildId) throws SQLException {
        PreparedStatement st = con.prepareStatement("select * from db_instances"
                + " where guild_id = ?");
        st.closeOnCompletion();
        st.setString(1, guildId.asString());
        ResultSet set = st.executeQuery();
        if (!set.next()) return null;
        SharedDbProvider instance = new SharedDbProvider(guildId,
                set.getString("server"),
                set.getString("username"),
                set.getString("password"),
                set.getString("database"));
        set.close();
        return instance;
    }

    public void registerInstance(SharedDbProvider provider) throws SQLException {
        PreparedStatement st = con.prepareStatement("insert into db_instances(guild_id,"
                + "server, username, password, database) values(?, ?, ?, ?, ?);");
        st.closeOnCompletion();
        st.setString(1, provider.getGuildId().asString());
        st.setString(2, provider.getDbServer());
        st.setString(3, provider.getUsername());
        st.setString(4, provider.getPassword());
        st.setString(5, provider.getDbName());
        st.executeUpdate();
    }

    public void updateInstance(SharedDbProvider provider) throws SQLException {
        PreparedStatement st = con.prepareStatement("update db_instances set server = ?,"
                + " username = ?, password = ?, database = ? where guild_id = ?;");
        st.closeOnCompletion();
        st.setString(1, provider.getDbServer());
        st.setString(2, provider.getUsername());
        st.setString(3, provider.getPassword());
        st.setString(4, provider.getDbName());
        st.setString(5, provider.getGuildId().asString());
        st.executeUpdate();
    }

    public void removeInstance(SharedDbProvider provider) throws SQLException {
        PreparedStatement st = con.prepareStatement("delete from db_instances"
                + " where guild_id = ?;");
        st.setString(1, provider.getGuildId().asString());
        st.executeUpdate();
    }

}
