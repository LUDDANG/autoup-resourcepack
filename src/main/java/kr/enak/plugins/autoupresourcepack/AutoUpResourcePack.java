package kr.enak.plugins.autoupresourcepack;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.logging.Logger;

public final class AutoUpResourcePack extends JavaPlugin {
    public static AutoUpResourcePack INSTANCE;

    public static Logger LOGGER;
    FileConfiguration config = getConfig();
    UpToDateModule updateModule;
    private BukkitTask task;

    @Override
    public void onEnable() {
        INSTANCE = this;
        LOGGER = getLogger();

        // Plugin startup logic
        config.addDefault("lastModifiedTimestamp", 0);
        config.addDefault("url", "https://foo.domain/path/to/resourcepack");
        config.options().copyDefaults(true);
        saveConfig();

        updateModule = new UpToDateModule(getConfig());

        this.task = Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::updateChecker, 100L, 60 * 20L);

        Bukkit.getPluginCommand("autoup").setExecutor(new AutoUpCommand());
    }

    public void updateChecker() {
        rawUpdateChecker(false);
    }

    public void rawUpdateChecker(boolean force) {
        boolean flag = updateModule.isPackUpdated(force);

        if (flag) {
            config.set("lastModifiedTimestamp", updateModule.getLastModifiedTimestamp());
            updateModule.notifyPlayers();
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        saveConfig();
        if (this.task != null && !this.task.isCancelled()) this.task.cancel();
    }
}
