package net.runelite.client.plugins.microbot.mntn.MntnSlayer;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.plugins.microbot.mntn.MntnSlayer.data.MntnSlayerMaster;
import net.runelite.client.plugins.microbot.mntn.MntnSlayer.data.TeleportOption;
import net.runelite.client.plugins.microbot.util.misc.Rs2Food;


@ConfigGroup("Slayer")
public interface MntnSlayerConfig extends Config {
    @ConfigItem(
            keyName = "slayerMaster",
            name = "Slayer Master",
            description = "Choose the slayer master",
            position = 0
    )
    default MntnSlayerMaster MASTER()
    {
        return MntnSlayerMaster.TURAEL;
    }

    @ConfigSection(
            name = "Teleport",
            description = "Teleport Configuration",
            position = 2,
            closedByDefault = false
    )

    String teleportSection = "Teleport";

    @ConfigItem(keyName = "teleportType", name = "Teleport Type", description = "Select the type of teleport to withdraw", section = teleportSection)
    default TeleportOption TeleportType() { return TeleportOption.FALADOR_TELEPORT; }

    @ConfigSection(
            name = "Health",
            description = "Health Configuration",
            position = 5,
            closedByDefault = false
    )

    String foodSection = "Food";

    @ConfigItem(keyName = "foodType", name = "Food Type", description = "Select the type of food to withdraw", section = foodSection)
    default Rs2Food FoodType() { return Rs2Food.LOBSTER; }

    @ConfigSection(
            name = "Combat Methods and Gear",
            description = "Combat configuration.",
            position = 10,
            closedByDefault = false
    )

    String combatSection = "Combat";

    @ConfigItem(
            keyName = "meleeGear",
            name = "Melee Gear",
            description = "List all melee gear including weapon, leave blank to skip this combat method.",
            position = 10,
            section = combatSection
    )
    default String MeleeGear()
    {
        return "Iron chainbody,Coif,Ring of duelling(8),Green d'hide chaps,Leather boots,Green d'hide vambraces,Amulet of glory,Yellow cape,Tzhaar-ket-om";
    }

    @ConfigItem(
            keyName = "rangeGear",
            name = "Range Gear",
            description = "List all range gear including weapon, leave blank to skip this combat method.",
            position = 10,
            section = combatSection
    )
    default String RangeGear()
    {
        return "Shortbow,Bronze arrow,Iron chainbody,Coif,Ring of duelling(8),Green d'hide chaps,Leather boots,Green d'hide vambraces,Amulet of glory,Yellow cape,Tzhaar-ket-om";
    }

    @ConfigItem(
            keyName = "mageGear",
            name = "Mage Gear",
            description = "List all mage gear including weapon, leave blank to skip this combat method.",
            position = 10,
            section = combatSection
    )
    default String MageGear() {
        return "";
    }
}
