package me.stella.plugin.listeners;

import me.stella.GhostTweak;
import me.stella.nms.MinecraftProtocol;
import me.stella.plugin.inventory.GhostInventoryHandle;
import me.stella.utility.GhostUtility;
import me.stella.utility.GhostVariables;
import me.stella.utility.MinecraftRepository;
import me.stella.utility.ViewPortProfile;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class GhostEditLog implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void close(InventoryCloseEvent event) {
        final Inventory close = event.getInventory();
        if(!(close.getHolder() instanceof GhostInventoryHandle))
            return;
        Player ghostEditor = (Player) event.getPlayer();
        (new BukkitRunnable() {
            @Override
            public void run() {
                MinecraftProtocol protocol = GhostVariables.get(MinecraftProtocol.class);
                ViewPortProfile viewPort = (ViewPortProfile) ghostEditor.getMetadata("ghostView").get(0).value();
                if(viewPort == null)
                    return;
                GhostInventoryHandle.InventoryModule module = GhostUtility.getModule(close.getHolder());
                Object playerData = viewPort.getViewingNBT();
                UUID playerUUID = viewPort.getViewingUID();
                try {
                    switch(module) {
                        case INVENTORY:
                            Object inventoryNBTData = protocol.writeInventory(close);
                            protocol.injectInventory(playerData, inventoryNBTData);
                            break;
                        case ENDER_CHEST:
                            Object enderChestNBT = protocol.writeEnderChest(close);
                            protocol.injectInventory(playerData, enderChestNBT);
                            break;
                        default:
                            throw new RuntimeException("Invalid module!");
                    }
                    protocol.writeToDisk(playerUUID, playerData);
                    ghostEditor.sendMessage(GhostUtility.color("&aSaved changes made for " + MinecraftRepository.lookupName(playerUUID)));
                } catch(Exception err) { err.printStackTrace(); }
            }
        }).runTaskAsynchronously(GhostTweak.bukkit());
    }

}
