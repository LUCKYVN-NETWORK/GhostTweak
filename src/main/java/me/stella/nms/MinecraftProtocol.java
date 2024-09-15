package me.stella.nms;

import me.stella.utility.GhostUtility;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.util.UUID;

public interface MinecraftProtocol {

    Object readPlayerData(File file);

    Object readNBTValue(Object object, String path);

    String readPlayerName(Object playerData);

    void setNBTValue(Object nbtObject, String path, Object value);

    Inventory readInventory(Object inventoryData, String owner);

    Object writeInventory(Inventory bukkitInventory);

    Inventory readEnderChest(Object enderChestData, String owner);

    Object writeEnderChest(Inventory bukkitEnderChest);

    Object readInventoryData(Object playerData);

    Object readEnderChestData(Object playerData);

    void injectInventory(Object playerData, Object inventoryList);

    void injectEnderChest(Object playerData, Object inventoryList);

    void writeToDisk(UUID uid, Object playerData);

    default Class<?> getNMSClass(String name) throws Exception {
        return Class.forName(name, true, GhostUtility.getServerLoader());
    }

}
