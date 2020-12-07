package com.redcreator37.WhitelistBot.Database.BotHandling;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Common database routines for the local datastore
 */
public final class LocalDb {

    /**
     * Attempts to connect to the specified database
     *
     * @param database database path
     * @return a valid JDBC database connection
     * @throws SQLException on errors
     */
    public static Connection connect(String database) throws SQLException {
        Connection con = DriverManager.getConnection("jdbc:sqlite:" + database);
        con.setAutoCommit(true);
        return con;
    }

    /**
     * Creates possibly nonexistent database tables
     *
     * @param con       database connection
     * @param sqlStream the stream to the file which contains SQL
     *                  queries to execute to generate the tables
     * @throws SQLException on errors
     */
    public void createDatabaseTables(Connection con, InputStream sqlStream) throws SQLException, IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(sqlStream));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null)
            if (line.startsWith("--")) {
                con.prepareStatement(builder.toString()).execute();
                builder = new StringBuilder();
            } else builder.append(line).append(" ");
    }

}
