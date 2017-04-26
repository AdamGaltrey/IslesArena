package com.adamgaltrey.islesarena;

import com.adamgaltrey.islesarena.commands.CoreCommands;
import com.adamgaltrey.islesarena.configuration.IslesConfig;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Logger;

/**
 * Created by Adam on 26/04/2017.
 */
public class IslesArena extends JavaPlugin {

    private final CoreCommands cmds = new CoreCommands();

    private Logger logger;

    private static IslesArena plugin;

    @Override
    public void onEnable(){
        logger = getLogger();

        //Register commands.
        getCommand("islesarena").setExecutor(cmds);

        //Initialise config.
        File root = new File("plugins" + File.separator + "IslesArena");
        if(IslesConfig.init(root)){
            logger.info("Configuration loaded successfully.");
        } else {
            logger.info("Failed to initialise config @ " + root.getAbsolutePath());
        }

        plugin = this;

        logger.info("IslesArena started successfully.");
    }

    public static IslesArena getPlugin() {
        return plugin;
    }
}
