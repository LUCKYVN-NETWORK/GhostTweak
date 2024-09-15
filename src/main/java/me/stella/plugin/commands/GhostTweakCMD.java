package me.stella.plugin.commands;

import me.stella.GhostTweak;
import me.stella.nms.MinecraftProtocol;
import me.stella.plugin.inventory.GhostInventoryHandle;
import me.stella.utility.GhostUtility;
import me.stella.utility.GhostVariables;
import me.stella.utility.MinecraftRepository;
import me.stella.utility.ViewPortProfile;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class GhostTweakCMD implements CommandExecutor, TabCompleter {

    private static List<String> modes = Arrays.asList("inventory", "enderchest");

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(!(commandSender instanceof Player))
            commandSender.sendMessage(GhostUtility.color("&cCommand is only usable to players!"));
        else {
            Player player = (Player) commandSender;
            if(!player.isOp())
                player.sendMessage(GhostUtility.color("&cCommand is limited to only operators!"));
            else {
                if(strings.length < 2) {
                    player.sendMessage(GhostUtility.color("&cUsage: /ghosttweak <ec/inv> <player_name>"));
                    return true;
                }
                GhostInventoryHandle.InventoryModule module;
                switch(strings[0]) {
                    case "ec":
                    case "enderchest":
                        module = GhostInventoryHandle.InventoryModule.ENDER_CHEST;
                        break;
                    default:
                        module = GhostInventoryHandle.InventoryModule.INVENTORY;
                        break;
                }
                String playerName = strings[1];
                if(Bukkit.getPlayer(playerName) != null) {
                    player.sendMessage(GhostUtility.color("&cPlayer is currently online! Unable to edit their data files!"));
                    return true;
                }
                if(!MinecraftRepository.uidMappings.containsKey(playerName)) {
                    player.sendMessage(GhostUtility.color("&cPlayer does not exist! Maybe try again later?"));
                    return true;
                }

                GhostInventoryHandle.InventoryModule finalModule = module;
                (new BukkitRunnable() {
                    @Override
                    public void run() {
                        UUID uid = MinecraftRepository.uidMappings.get(playerName);
                        File userFile = MinecraftRepository.fileMappings.get(uid).getAbsoluteFile();
                        MinecraftProtocol activeProtocol = GhostVariables.get(MinecraftProtocol.class);
                        Object userDataNBT = activeProtocol.readPlayerData(userFile);
                        if(userDataNBT == null) {
                            player.sendMessage(GhostUtility.color("&cUnable to read NBT of user data!"));
                            return;
                        }
                        Inventory inventory = null;
                        switch(finalModule) {
                            case INVENTORY:
                                inventory = activeProtocol.readInventory(activeProtocol.readInventoryData(userDataNBT), playerName);
                                break;
                            case ENDER_CHEST:
                                inventory = activeProtocol.readEnderChest(activeProtocol.readEnderChestData(userDataNBT), playerName);
                                break;
                        }
                        player.setMetadata("ghostView", new FixedMetadataValue(GhostTweak.bukkit(), new ViewPortProfile(uid, userDataNBT)));
                        Inventory finalInventory = inventory;
                        (new BukkitRunnable() {
                            @Override
                            public void run() {
                                player.openInventory(finalInventory);
                            }
                        }).runTask(GhostTweak.bukkit());
                    }
                }).runTaskAsynchronously(GhostTweak.bukkit());
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if(strings.length == 1)
            return modes.stream().filter(mode -> mode.startsWith(strings[0].toLowerCase()))
                    .collect(Collectors.toList());
        return Collections.emptyList();
    }
}
