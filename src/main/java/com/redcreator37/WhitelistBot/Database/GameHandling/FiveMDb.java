package com.redcreator37.WhitelistBot.Database.GameHandling;

import com.redcreator37.WhitelistBot.DataModels.WhitelistedPlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FiveMDb {

    /**
     * The SQLite database connection to use for all database-related
     * operations
     */
    private final Connection con;

    /**
     * Constructs a new FiveMDb instance
     *
     * @param connection the MySQL connection to use
     */
    public FiveMDb(Connection connection) {
        this.con = connection;
    }

    /**
     * Returns the list of whitelisted players in the database
     *
     * @return the list of whitelisted players
     * @throws SQLException on errors
     */
    public List<WhitelistedPlayer> getWhitelistedPlayers() throws SQLException {
        List<WhitelistedPlayer> players = new ArrayList<>();
        ResultSet set = con.createStatement().executeQuery("select * from whitelist");
        while (set.next())
            players.add(new WhitelistedPlayer(set.getString("identifier")));
        set.close();
        return players;
    }

    /**
     * Whitelists this player in the db
     *
     * @param player the player to whitelist
     * @throws SQLException on errors
     */
    public void whitelistPlayer(WhitelistedPlayer player) throws SQLException {
        PreparedStatement st = con.prepareStatement("INSERT INTO whitelist(identifier) VALUES(?)");
        st.closeOnCompletion();
        st.setString(1, player.getIdentifier());
        st.executeUpdate();
    }

    /**
     * Removes this player from the whitelist in the db
     *
     * @param player the player to remove
     * @throws SQLException on errors
     */
    public void removePlayer(WhitelistedPlayer player) throws SQLException {
        String sql = "DELETE FROM whitelist WHERE identifier = ?;";
        PreparedStatement st = con.prepareStatement(sql);
        st.setString(1, player.getIdentifier());
        st.executeUpdate();
    }

}
