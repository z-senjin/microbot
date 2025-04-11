package net.runelite.client.plugins.microbot.mntn.MntnPker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("Health")
public interface MntnPkerConfig extends Config {
    @ConfigItem(
            keyName = "EatPercentage",
            name = "Eat %",
            description = "Health % to eat at.",
            position = 0
    )
    default int EatPercentage()
    {
        return 25;
    }

    @ConfigItem(
            keyName = "Escape",
            name = "Teleport/Run",
            description = "Run or teleport when out of food (teleport if runes are available.",
            position = 0
    )
    default boolean ShouldEscape()
    {
        return false;
    }
}
