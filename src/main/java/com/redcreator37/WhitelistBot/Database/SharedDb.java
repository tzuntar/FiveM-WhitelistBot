package com.redcreator37.WhitelistBot.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Common database routines
 */
public final class SharedDb {

    /**
     * Non-instantiable
     */
    private SharedDb() {
    }

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
     * @param con database connection
     * @throws SQLException on errors
     */
    public static void createDatabaseTables(Connection con) throws SQLException {
        String guildsTable = "create table if not exists guilds(\n" +
                "    id        integer not null\n" +
                "        primary key autoincrement,\n" +
                "    snowflake text,\n" +
                "    joined    date,\n" +
                "    rank      text);";
        con.prepareStatement(guildsTable).execute();
        String guildsTableUnique = "create unique index guilds_snowflake_uindex"
                + " on guilds(snowflake);";
        con.prepareStatement(guildsTableUnique).execute();
    }

}
