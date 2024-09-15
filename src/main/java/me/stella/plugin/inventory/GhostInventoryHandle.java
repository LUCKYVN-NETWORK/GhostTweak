package me.stella.plugin.inventory;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class GhostInventoryHandle implements InventoryHolder {

    @Override
    public Inventory getInventory() {
        return null;
    }

    public enum InventoryModule {
        INVENTORY, ENDER_CHEST
    }

    public static class GhostPlayerInventory extends GhostInventoryHandle {}

    public static class GhostEnderChest extends GhostInventoryHandle {}

}
