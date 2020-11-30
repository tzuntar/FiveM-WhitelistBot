package com.redcreator37.WhitelistBot.Database;

import com.mysql.cj.jdbc.MysqlDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class SharedDb {

    /**
     * Non-instantiable
     */
    private SharedDb() {
    }

    /**
     * Connects to the shared MySQL instance
     *
     * @param server   the address of the database server
     * @param username the database username
     * @param password the database password
     * @return the database connection object on success
     * @throws SQLException on errors
     */
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

}
