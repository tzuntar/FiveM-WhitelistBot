package com.redcreator37.WhitelistBot.Database.GameHandling;

import com.mysql.cj.jdbc.MysqlDataSource;
import discord4j.common.util.Snowflake;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a MySQL database connection provider
 */
public class SharedDbProvider {

    private final Snowflake guildId;

    private final String dbServer;

    private final String username;

    private final String password;

    private final String dbName;

    private Connection connection;

    /**
     * Constructs a new shared database provider
     *
     * @param guildId  the ID of the guild this instance is registered in
     * @param dbServer the address of the MySQL server to connect to
     * @param username the database username
     * @param password the database password
     * @param dbName   the name of the database to open
     */
    public SharedDbProvider(Snowflake guildId, String dbServer, String username, String password, String dbName) {
        this.guildId = guildId;
        this.dbServer = dbServer;
        this.username = username;
        this.password = password;
        this.dbName = dbName;
    }

    /**
     * Activates and connects to the database, bound to this provider
     * instance
     *
     * @return the open database connection
     * @throws SQLException on errors
     */
    public Connection connect() throws SQLException {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUser(username);
        dataSource.setPassword(password);
        dataSource.setServerName(dbServer);
        dataSource.setDatabaseName(dbName);
        connection = dataSource.getConnection();
        connection.setAutoCommit(true);
        return connection;
    }

    public Snowflake getGuildId() {
        return guildId;
    }

    public String getDbServer() {
        return dbServer;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDbName() {
        return dbName;
    }

    public Optional<Connection> getConnection() {
        return Optional.ofNullable(connection);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SharedDbProvider)) return false;
        SharedDbProvider that = (SharedDbProvider) o;
        return guildId.equals(that.guildId) &&
                dbServer.equals(that.dbServer) &&
                username.equals(that.username) &&
                password.equals(that.password) &&
                dbName.equals(that.dbName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(guildId, dbServer, username, password, dbName);
    }

}
