package com.redcreator37.WhitelistBot.Database;

import com.mysql.cj.jdbc.MysqlDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FiveMDb {

    public static Connection connect(String server, String username, String password) throws SQLException {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUser(username);
        dataSource.setPassword(password);
        dataSource.setServerName(server);
        dataSource.setDatabaseName("essentialmode");
        Connection con = dataSource.getConnection();
        con.setAutoCommit(true);
        return con;
    }

    public static class Player {
        private final String identifier;

        public Player(String identifier) {
            this.identifier = identifier;
        }

        public String getIdentifier() {
            return identifier;
        }
    }

    /**
     * Returns the list of whitelisted players in the database
     *
     * @return the list of whitelisted players
     * @throws SQLException on errors
     */
    public static List<Player> getWhitelistedPlayers(Connection con) throws SQLException {
        List<Player> players = new ArrayList<>();
        Statement st = con.createStatement();
        st.closeOnCompletion();
        ResultSet set = st.executeQuery("SELECT * FROM whitelist");
        while (set.next())
            players.add(new Player(set.getString("identifier")));
        set.close();
        return players;
    }

    /**
     * Whitelists this player in the db
     *
     * @param player the player to whitelist
     * @throws SQLException on errors
     */
    public static void whitelistPlayer(Connection con, Player player) throws SQLException {
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
    public static void removePlayer(Connection con, Player player) throws SQLException {
        String sql = "DELETE FROM whitelist WHERE identifier = ?;";
        PreparedStatement st = con.prepareStatement(sql);
        st.setString(1, player.getIdentifier());
        st.executeUpdate();
    }

}
