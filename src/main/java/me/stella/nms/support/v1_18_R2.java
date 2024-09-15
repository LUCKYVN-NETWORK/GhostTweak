package me.stella.nms.support;

import me.stella.nms.MinecraftProtocol;
import me.stella.plugin.inventory.GhostInventoryHandle;
import me.stella.utility.GhostUtility;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

public class v1_18_R2 implements MinecraftProtocol {

    private final Class<?> nbtTagCompound;
    private final Class<?> nbtTagList;
    private final Class<?> nbtTagString;
    private final Class<?> nbtBase;
    private final Class<?> nbtCompressedStreamTools;
    private final Class<?> itemStack;
    private final Class<?> craftItemStack;

    public v1_18_R2() throws Exception {
        this.nbtTagCompound = getNMSClass("net.minecraft.nbt.NBTTagCompound");
        this.nbtTagList = getNMSClass("net.minecraft.nbt.NBTTagList");
        this.nbtTagString = getNMSClass("net.minecraft.nbt.NBTTagString");
        this.nbtBase = getNMSClass("net.minecraft.nbt.NBTBase");
        this.nbtCompressedStreamTools = getNMSClass("net.minecraft.nbt.NBTCompressedStreamTools");
        this.itemStack = getNMSClass("net.minecraft.world.item.ItemStack");
        this.craftItemStack = getNMSClass("org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack");
    }

    @Override
    public Object readPlayerData(File file) {
        try {
            return nbtCompressedStreamTools.getMethod("a", File.class).invoke(null, file);
        } catch(Exception err) { err.printStackTrace(); }
        return null;
    }

    @Override
    public Object readNBTValue(Object object, String path) {
        try {
            assert object.getClass().getName().contains("NBTTagCompound");
            return nbtTagCompound.getMethod("c", String.class).invoke(object, path);
        } catch(Exception err) { err.printStackTrace(); }
        return null;
    }

    @Override
    public String readPlayerName(Object playerData) {
        try {
            assert playerData != null;
            return String.valueOf(this.nbtTagString.getMethod("e_").invoke(readNBTValue(playerData, "lastKnownName")));
        } catch(Exception err) { err.printStackTrace(); }
        return "";
    }

    @Override
    public void setNBTValue(Object nbtObject, String path, Object value) {
        assert nbtObject.getClass().getName().contains("NBTTagCompound");
        try {
            nbtTagCompound.getMethod("a", String.class, this.nbtBase)
                    .invoke(nbtObject, path, value);
        } catch(Exception err) { err.printStackTrace(); }
    }

    @Override
    public Inventory readInventory(Object inventoryData, String owner) {
        Inventory inventory = GhostUtility.buildHandleInventory(GhostInventoryHandle.InventoryModule.INVENTORY, owner);
        try {
            int inventoryDataSize = (int) this.nbtTagList.getMethod("size").invoke(inventoryData);
            for(int x = 0; x < inventoryDataSize; x++) {
                Object stackDataNBT = this.nbtTagList.getMethod("k", int.class).invoke(inventoryData, x);
                if((boolean) this.nbtTagCompound.getMethod("f").invoke(stackDataNBT))
                    continue;
                int slot = ((int) ((byte) this.nbtTagCompound.getMethod("f", String.class).invoke(stackDataNBT, "Slot"))) & 0xFF;
                if(slot <= 36) {}
                else if(slot >= 100 && slot <= 103)
                    slot = (48 - (slot - 100));
                else
                    slot = 50;
                inventory.setItem(slot, (ItemStack) this.craftItemStack.getMethod("asBukkitCopy", this.itemStack)
                        .invoke(null, this.itemStack.getMethod("a", this.nbtTagCompound).invoke(null, stackDataNBT)));
            }
        } catch(Exception err) { err.printStackTrace(); }
        return inventory;
    }

    @Override
    public Object writeInventory(Inventory bukkitInventory) {
        assert bukkitInventory.getSize() == 54;
        ItemStack airObject = new ItemStack(Material.AIR);
        try {
            Object inventoryItemList = this.nbtTagList.getConstructors()[0].newInstance();
            Field fieldTagList = this.nbtTagList.getDeclaredField("c"); fieldTagList.setAccessible(true);
            List<Object> tags = (List<Object>) fieldTagList.get(inventoryItemList);
            int i;
            for(i = 0; i < 36; i++) {
                ItemStack bukkitItem = bukkitInventory.getItem(i);
                Object itemNBTData;
                if(bukkitItem == null || bukkitItem.getType() == Material.AIR)
                    itemNBTData = this.craftItemStack.getMethod("asNMSCopy", ItemStack.class).invoke(null, airObject.clone());
                else
                    itemNBTData = this.craftItemStack.getMethod("asNMSCopy", ItemStack.class).invoke(null, bukkitItem);
                Object tagData = this.nbtTagCompound.getConstructors()[0].newInstance();
                tagData = this.itemStack.getMethod("b", this.nbtTagCompound).invoke(itemNBTData, tagData);
                this.nbtTagCompound.getMethod("a", String.class, byte.class).invoke(tagData, "Slot", (byte)i);
                tags.add(tagData);
            }
            for(i = 45; i < 49; i++) {
                ItemStack bukkitItem = bukkitInventory.getItem(i);
                Object itemNBTData;
                if(bukkitItem == null || bukkitItem.getType() == Material.AIR)
                    itemNBTData = this.craftItemStack.getMethod("asNMSCopy", ItemStack.class).invoke(null, airObject.clone());
                else
                    itemNBTData = this.craftItemStack.getMethod("asNMSCopy", ItemStack.class).invoke(null, bukkitItem);
                Object tagData = this.nbtTagCompound.getConstructors()[0].newInstance();
                tagData = this.itemStack.getMethod("b", this.nbtTagCompound).invoke(itemNBTData, tagData);
                this.nbtTagCompound.getMethod("a", String.class, byte.class).invoke(tagData, "Slot", (byte)(i+100));
                tags.add(tagData);
            }
            ItemStack bukkitItem = bukkitInventory.getItem(50);
            Object itemNBTData;
            if(bukkitItem == null || bukkitItem.getType() == Material.AIR)
                itemNBTData = this.craftItemStack.getMethod("asNMSCopy", ItemStack.class).invoke(null, airObject.clone());
            else
                itemNBTData = this.craftItemStack.getMethod("asNMSCopy", ItemStack.class).invoke(null, bukkitItem);
            Object tagData = this.nbtTagCompound.getConstructors()[0].newInstance();
            tagData = this.itemStack.getMethod("b", this.nbtTagCompound).invoke(itemNBTData, tagData);
            this.nbtTagCompound.getMethod("a", String.class, byte.class).invoke(tagData, "Slot", (byte)(i+150));
            tags.add(tagData);
            fieldTagList.set(inventoryItemList, tags);
            return inventoryItemList;
        } catch(Throwable err) { err.printStackTrace(); }
        return null;
    }

    @Override
    public Inventory readEnderChest(Object enderChestData, String owner) {
        Inventory enderChest = GhostUtility.buildHandleInventory(GhostInventoryHandle.InventoryModule.ENDER_CHEST, owner);
        try {
            int enderChestDataSize = (int) this.nbtTagList.getMethod("size").invoke(enderChestData);
            for(int x = 0; x < enderChestDataSize; x++) {
                Object stackDataNBT = this.nbtTagList.getMethod("k", int.class).invoke(enderChestData, x);
                if((boolean) this.nbtTagCompound.getMethod("f").invoke(stackDataNBT))
                    continue;
                int slot = ((int) ((byte) this.nbtTagCompound.getMethod("f", String.class).invoke(stackDataNBT, "Slot"))) & 0xFF;
                enderChest.setItem(slot, (ItemStack) this.craftItemStack.getMethod("asBukkitCopy", this.itemStack)
                        .invoke(null, this.itemStack.getMethod("a", this.nbtTagCompound).invoke(null, stackDataNBT)));
            }
        } catch(Exception err) { err.printStackTrace(); }
        return enderChest;
    }

    @Override
    public Object writeEnderChest(Inventory bukkitEnderChest) {
        assert bukkitEnderChest.getSize() == 27;
        ItemStack airObject = new ItemStack(Material.AIR);
        try {
            Object enderChestItemList = this.nbtTagList.getConstructors()[0].newInstance();
            Field fieldTagList = this.nbtTagList.getDeclaredField("c"); fieldTagList.setAccessible(true);
            List<Object> tags = (List<Object>) fieldTagList.get(enderChestItemList);
            int i;
            for(i = 0; i < 27; i++) {
                ItemStack bukkitItem = bukkitEnderChest.getItem(i);
                Object itemNBTData;
                if(bukkitItem == null || bukkitItem.getType() == Material.AIR)
                    itemNBTData = this.craftItemStack.getMethod("asNMSCopy", ItemStack.class).invoke(null, airObject.clone());
                else
                    itemNBTData = this.craftItemStack.getMethod("asNMSCopy", ItemStack.class).invoke(null, bukkitItem);
                Object tagData = this.nbtTagCompound.getConstructors()[0].newInstance();
                tagData = this.itemStack.getMethod("b", this.nbtTagCompound).invoke(itemNBTData, tagData);
                this.nbtTagCompound.getMethod("a", String.class, byte.class).invoke(tagData, "Slot", (byte)i);
                tags.add(tagData);
            }
            fieldTagList.set(enderChestItemList, tags);
            return enderChestItemList;
        } catch(Exception err) { err.printStackTrace(); }
        return null;
    }

    @Override
    public Object readInventoryData(Object playerData) {
        return readNBTValue(playerData, "Inventory");
    }

    @Override
    public Object readEnderChestData(Object playerData) {
        return readNBTValue(playerData, "EnderItems");
    }

    @Override
    public void injectInventory(Object playerData, Object inventoryList) {
        setNBTValue(playerData,"Inventory", inventoryList);
    }

    @Override
    public void injectEnderChest(Object playerData, Object inventoryList) {
        setNBTValue(playerData,"EnderItems", inventoryList);
    }

    @Override
    public void writeToDisk(UUID uid, Object playerData) {
        File dataRepository = new File(GhostUtility.getServerDirectory(), Bukkit.getWorlds().get(0).getName() + "/playerdata");
        try {
            File tempFile = new File(dataRepository, uid.toString().concat(".dat.tmp"));
            this.nbtCompressedStreamTools.getMethod("a", this.nbtTagCompound, File.class)
                    .invoke(null, playerData, tempFile);
            File main = new File(dataRepository, uid.toString().concat(".dat"));
            if(main.exists())
                main.delete();
            tempFile.renameTo(main.getAbsoluteFile());
        } catch(Exception err) { err.printStackTrace(); }
    }

}
