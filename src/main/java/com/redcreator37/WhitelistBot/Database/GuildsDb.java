package com.redcreator37.WhitelistBot.Database;

import com.redcreator37.WhitelistBot.Guild;
import discord4j.common.util.Snowflake;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class GuildsDb {

    private final Connection con;

    public GuildsDb(Connection connection) {
        this.con = connection;
    }

    /**
     * Returns the list of all guilds in the database
     *
     * @return the list of all guilds
     * @throws SQLException on errors
     */
    public List<Guild> getGuilds() throws SQLException {
        List<Guild> guilds = new ArrayList<>();
        Statement st = con.createStatement();
        st.closeOnCompletion();
        ResultSet set = st.executeQuery("SELECT * FROM guilds");
        while (set.next()) {
            Guild guild = new Guild(set.getInt("id"),
                    Snowflake.of(set.getString("snowflake")),
                    set.getString("joined"),
                    set.getString("rank"), con);
            guilds.add(guild);
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
                .prepareStatement("INSERT INTO guilds(snowflake, joined, rank)"
                        + " VALUES(?, ?, ?)");
        st.closeOnCompletion();
        st.setString(1, guild.getSnowflake().asString());
        st.setString(2, guild.getJoined());
        st.setString(3, guild.getRank());
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
