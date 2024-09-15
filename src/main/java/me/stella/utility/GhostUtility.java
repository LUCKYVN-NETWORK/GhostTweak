package me.stella.utility;

import me.stella.GhostTweak;
import me.stella.plugin.inventory.GhostInventoryHandle;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

public class GhostUtility {

    public static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(256);
    public static Set<ScheduledFuture<?>> asyncTasks = new HashSet<>();
    private static String serverDirectory = "";

    public static String color(String param) {
        try {
            return String.valueOf(Class.forName("org.bukkit.ChatColor", true, getServerLoader())
                    .getMethod("translateAlternateColorCodes", char.class, String.class)
                    .invoke(null, '&', param));
        } catch(Exception err) { err.printStackTrace(); }
        return param;
    }

    public static ClassLoader getPluginLoader() {
        return GhostTweak.bukkit().getClass().getClassLoader();
    }

    public static ClassLoader getServerLoader() {
        return GhostTweak.bukkit().getServer().getClass().getClassLoader();
    }

    public static Inventory buildHandleInventory(GhostInventoryHandle.InventoryModule module, String owner) {
        String name = module.name() + " - " + owner;
        switch(module) {
            case INVENTORY:
                return Bukkit.createInventory(GhostVariables.get(GhostInventoryHandle.GhostPlayerInventory.class), 54, name);
            case ENDER_CHEST:
                return Bukkit.createInventory(GhostVariables.get(GhostInventoryHandle.GhostEnderChest.class), 27, name);
            default:
                throw new RuntimeException("Invalid inventory module! Please check the source code!");
        }
    }

    public static String getServerDirectory() {
        return serverDirectory;
    }

    public static void setupServerDirectory() {
        File dataFolder = GhostTweak.bukkit().getDataFolder();
        if(!dataFolder.exists())
            dataFolder.mkdirs();
        String pluginFolder = dataFolder.getAbsoluteFile().getAbsolutePath();
        dataFolder.delete();
        String[] subDirs = pluginFolder.replace("\\", "/").split("/");
        StringBuilder pathBuilder = new StringBuilder();
        for(int x = 0; x < subDirs.length - 2; x++)
            pathBuilder.append(subDirs[x]).append("/");
        String pathServer = pathBuilder.toString();
        serverDirectory = pathServer.endsWith("/") ? pathServer : pathServer.concat("/");
    }

    public static GhostInventoryHandle.InventoryModule getModule(InventoryHolder holder) {
        if(holder instanceof GhostInventoryHandle.GhostPlayerInventory)
            return GhostInventoryHandle.InventoryModule.INVENTORY;
        else if(holder instanceof GhostInventoryHandle.GhostEnderChest)
            return GhostInventoryHandle.InventoryModule.ENDER_CHEST;
        else throw new RuntimeException("Invalid inventory module! Please check the source code!");
    }

    public static void shutdownScheduler() {
        asyncTasks.forEach(task -> {
            task.cancel(true);
        });
        asyncTasks.clear();
        scheduler.shutdown();
        System.gc();
        System.runFinalization();
    }

}
