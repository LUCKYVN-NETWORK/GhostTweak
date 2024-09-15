package me.stella.utility;

import me.stella.GhostTweak;
import me.stella.nms.MinecraftProtocol;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class MinecraftRepository {

    public static Map<UUID, File> fileMappings = Collections.synchronizedMap(new HashMap<>());
    public static Map<String, UUID> uidMappings = Collections.synchronizedMap(new HashMap<>());

    public static void runMappings() {
        GhostUtility.asyncTasks.add(GhostUtility.scheduler.scheduleAtFixedRate(() -> {
            final boolean debugActive = GhostTweak.bukkit().debug;
            AtomicInteger indexed = new AtomicInteger();
            BukkitTask task = (new BukkitRunnable() {
                @Override
                public void run() {
                    if(debugActive)
                        GhostTweak.console.log(Level.INFO, "Indexed files > " + indexed.get());
                }
            }).runTaskTimerAsynchronously(GhostTweak.bukkit(), 20L, 60L);
            try {
                MinecraftProtocol activeProtocol = GhostVariables.get(MinecraftProtocol.class);
                File dataDirectory = new File(GhostUtility.getServerDirectory() + Bukkit.getWorlds().get(0).getName() + "/playerdata/");
                if(debugActive)
                    GhostTweak.console.log(Level.INFO, "Fetching data from " + dataDirectory.getAbsolutePath());
                if(dataDirectory.listFiles() == null) {
                    if(debugActive)
                        GhostTweak.console.log(Level.INFO, "Unable to extrapolate user data from world!");
                    return;
                }
                boolean errorSpouted = false;
                for(File playerDataFile: dataDirectory.listFiles()) {
                    try {
                        if(!(playerDataFile.isFile() && playerDataFile.canRead() && playerDataFile.getName().endsWith(".dat")))
                            continue;
                        indexed.incrementAndGet();
                        UUID uid = UUID.fromString(playerDataFile.getName().replace(".dat", ""));
                        Object nbtPlayerData = activeProtocol.readPlayerData(playerDataFile);
                        if(nbtPlayerData != null) {
                            String name = activeProtocol.readPlayerName(activeProtocol.readNBTValue(nbtPlayerData, "bukkit"));
                            if(!name.isEmpty()) {
                                fileMappings.put(uid, playerDataFile);
                                uidMappings.put(name, uid);
                            }
                        }
                    } catch(Throwable err2) {
                        if(!errorSpouted && debugActive) {
                            GhostTweak.console.log(Level.INFO, "Error on file > " + playerDataFile.getName());
                            errorSpouted = true;
                        }
                    }
                }
                task.cancel();
            } catch(Exception err) { err.printStackTrace(); }
            if(debugActive)
                GhostTweak.console.log(Level.INFO, "[GhostDebug] Indexed " + uidMappings.size() + " accounts.");
            task.cancel();
        }, 0L, 300L, TimeUnit.SECONDS));
    }

    public static String lookupName(UUID uid) {
        Set<Map.Entry<String, UUID>> entryCopy = new HashSet<>(uidMappings.entrySet());
        for(Map.Entry<String, UUID> entry: entryCopy) {
            if(entry.getValue().equals(uid))
                return entry.getKey();
        }
        return null;
    }


}
