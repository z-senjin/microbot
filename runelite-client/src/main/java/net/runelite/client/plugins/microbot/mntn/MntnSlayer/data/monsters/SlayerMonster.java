package net.runelite.client.plugins.microbot.mntn.MntnSlayer.data.monsters;

import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.mntn.MntnSlayer.data.GearStyle;
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer;

import java.util.ArrayList;
import java.util.List;

public class SlayerMonster {
    private final String name;
    private final List<WorldPoint> locations;
    private final GearStyle preferredGearStyle;
    private final Rs2Prayer protectionPrayer; // nullable
    private final List<String> customItems;           // optional

    public SlayerMonster(
            String name,
            List<WorldPoint> locations,
            GearStyle preferredGearStyle,
            Rs2Prayer protectionPrayer,
            List<String> customItems
    ) {
        this.name = name;
        this.locations = locations;
        this.preferredGearStyle = preferredGearStyle;
        this.protectionPrayer = protectionPrayer;
        this.customItems = customItems != null ? customItems : new ArrayList<>();
    }

    public String getName() { return name; }
    public List<WorldPoint> getLocations() { return locations; }
    public GearStyle getPreferredGearStyle() { return preferredGearStyle; }
    public Rs2Prayer getProtectionPrayer() { return protectionPrayer; }
    public List<String> getCustomItems() { return customItems; }

    public WorldPoint getPrimaryLocation() {
        return locations.isEmpty() ? null : locations.get(0);
    }
}

