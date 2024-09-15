package me.stella;

import me.stella.nms.MinecraftProtocol;
import me.stella.plugin.commands.GhostTweakCMD;
import me.stella.plugin.inventory.GhostInventoryHandle;
import me.stella.plugin.listeners.GhostEditLog;
import me.stella.utility.GhostUtility;
import me.stella.utility.GhostVariables;
import me.stella.utility.MinecraftRepository;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class GhostTweak extends JavaPlugin {

    public static final Logger console = Logger.getLogger("Minecraft");
    private static String serverVersion;
    private static GhostTweak main;
    public boolean debug = false;

    @Override
    public void onEnable() {
        main = this;
        GhostUtility.setupServerDirectory();
        serverVersion = getServer().getClass().getName().split("\\.")[3].trim();
        try {
            Class<?> implClass = Class.forName("me.stella.nms.support." + serverVersion, true, GhostUtility.getPluginLoader());
            GhostVariables.inject(MinecraftProtocol.class, implClass.getConstructors()[0].newInstance());
        } catch(Exception err) { err.printStackTrace(); }
        if(GhostVariables.get(MinecraftProtocol.class) == null)
            throw new RuntimeException("Unable to setup NMS fork! Shutting down...");
        console.log(Level.INFO, GhostUtility.color("[GhostTweak] &fDetected server version -> &a" + getServerVersion()));
        GhostVariables.inject(GhostInventoryHandle.GhostPlayerInventory.class, new GhostInventoryHandle.GhostPlayerInventory());
        GhostVariables.inject(GhostInventoryHandle.GhostEnderChest.class, new GhostInventoryHandle.GhostEnderChest());
        GhostTweakCMD cmd = new GhostTweakCMD();
        getCommand("ghosttweak").setExecutor(cmd);
        getCommand("ghosttweak").setTabCompleter(cmd);
        getServer().getPluginManager().registerEvents(new GhostEditLog(), this);
        console.log(Level.INFO, GhostUtility.color("[GhostTweak] Initialized &eBukkit &fcomponents"));
        MinecraftRepository.runMappings();
        console.log(Level.INFO, GhostUtility.color("[GhostTweak] Initialized &cNBT &fmapping tasks"));
        GhostUtility.asyncTasks.add(GhostUtility.scheduler.scheduleAtFixedRate(() -> {
            try {
                System.gc();
                System.runFinalization();
            } catch(Exception err) { err.printStackTrace(); }
        }, 3L, 600L, TimeUnit.SECONDS));
        console.log(Level.INFO, GhostUtility.color("[GhostTweak] Initialized &binternal garbage collector"));
    }

    public static String getServerVersion() {
        return serverVersion;
    }

    public static GhostTweak bukkit() {
        return main;
    }

    @Override
    public void onDisable() {
        GhostUtility.shutdownScheduler();
        Bukkit.getScheduler().cancelTasks(this);
        MinecraftRepository.uidMappings.clear();
        MinecraftRepository.fileMappings.clear();
        System.gc();
        System.runFinalization();
    }
}
