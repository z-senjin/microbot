package net.runelite.client.plugins.microbot.mntn.MossKiller;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("example")
public interface MossKillerConfig extends Config {
    @ConfigItem(
            keyName = "guide",
            name = "How to use",
            description = "How to use this plugin",
            position = 0
    )
    default String GUIDE() {
        return "Have runes for teleport to varrock, lobsters, and bronze axe in bank. Start script in varrock west bank with armor and weapon equipped.";
    }
    @ConfigItem(
            keyName = "keyThreshold",
            name = "Key Threshold",
            description = "How many mossy keys should be collected before killing boss.",
            position = 0
    )
    default int keyThreshold()
    {
        return 30;
 }

    @ConfigItem(
            keyName = "wildyMode",
            name = "Kill Wildy Moss Giants?",
            description = "Turn off Wilderness Warnings. Start in Ferox Enclave. Have all items equipped. It will save and deposit what is not needed.",
            position = 0
    )
    default boolean wildyMode()
    {
        return true;
    }

    @ConfigItem(
            keyName = "isSlashWeaponEquipped",
            name = "Slash Weapon Equipped?",
            description = "Do you have a slash weapon equipped for spiders web? If False, have knife in bank.",
            position = 0
    )
    default boolean isSlashWeaponEquipped()
    {
        return true;
    }
}
