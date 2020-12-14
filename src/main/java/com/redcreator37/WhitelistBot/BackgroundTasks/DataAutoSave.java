package com.redcreator37.WhitelistBot.BackgroundTasks;

import com.redcreator37.WhitelistBot.Database.BotHandling.DbInstances;
import com.redcreator37.WhitelistBot.Database.BotHandling.GuildsDb;
import com.redcreator37.WhitelistBot.DiscordBot;

import java.sql.SQLException;
import java.text.MessageFormat;

import static com.redcreator37.WhitelistBot.Localizations.lc;

/**
 * Saves the data to the internal database in the background
 */
public class DataAutoSave implements Runnable {

    private final DbInstances dbInstances;

    private final GuildsDb guildsDb;

    /**
     * Constructs a new DataAutoSave instance
     *
     * @param dbInstances the connection provider to the external
     *                    database instances database
     * @param guildsDb    the connection provider to the guilds
     *                    database
     */
    public DataAutoSave(DbInstances dbInstances, GuildsDb guildsDb) {
        this.dbInstances = dbInstances;
        this.guildsDb = guildsDb;
    }

    @Override
    public void run() {
        System.out.println(lc("saving-data-do-not-stop"));
        DiscordBot.guilds.values().forEach(guild -> {
            try {
                dbInstances.updateInstance(guild.getSharedDbProvider());
                guildsDb.updateAdminRole(guild);
            } catch (SQLException ex) {
                System.err.println(MessageFormat.format(lc("writing-guild-data"
                                + "-failed-reason"), guild.getSnowflake().toString(),
                        ex.getMessage()));
            }
        });
    }

}
