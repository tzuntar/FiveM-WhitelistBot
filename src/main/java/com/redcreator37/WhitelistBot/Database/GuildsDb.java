package com.redcreator37.WhitelistBot.Database;

import com.redcreator37.WhitelistBot.Guild;
import discord4j.common.util.Snowflake;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class GuildsDb {

    /**
     * The SQLite database connection to use for all database-related
     * operations
     */
    private final Connection con;

    /**
     * Constructs a new GuildsDb instance
     *
     * @param connection connection to the SQLite database to use
     */
    public GuildsDb(Connection connection) {
        this.con = connection;
    }

    /**
     * Returns the list of all guilds in the database
     *
     * @return the list of all guilds
     * @throws SQLException on errors
     */
    public HashMap<Snowflake, Guild> getGuilds() throws SQLException {
        HashMap<Snowflake, Guild> guilds = new HashMap<>();
        Statement st = con.createStatement();
        st.closeOnCompletion();
        ResultSet set = st.executeQuery("SELECT * FROM guilds");
        while (set.next()) {
            Snowflake s = Snowflake.of(set.getString("snowflake"));
            Guild guild = new Guild(set.getInt("id"), s,
                    set.getString("joined"),
                    set.getString("admin_role"), con);
            guilds.put(s, guild);
        }
        set.close();
        return guilds;
    }

    /**
     * Adds this guild to the database
     *
     * @param guild the guild to add
     * @throws SQLException on errors
     */
    public void addGuild(Guild guild) throws SQLException {
        PreparedStatement st = con
                .prepareStatement("INSERT INTO guilds(snowflake, joined, admin_role)"
                        + " VALUES(?, ?, ?)");
        st.closeOnCompletion();
        st.setString(1, guild.getSnowflake().asString());
        st.setString(2, guild.getJoined());
        st.setString(3, guild.getAdminRole());
        st.executeUpdate();
    }

    /**
     * Removes this guild from the database
     *
     * @param guild the guild to remove
     * @throws SQLException on errors
     */
    public void removeGuild(Guild guild) throws SQLException {
        String sql = "DELETE FROM guilds WHERE snowflake = " + guild
                .getSnowflake().asString() + ";";
        PreparedStatement st = con.prepareStatement(sql);
        st.executeUpdate();
    }

}
