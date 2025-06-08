package net.runelite.client.plugins.microbot.mntn.MntnSlayer.data;

import lombok.Getter;
import net.runelite.api.Quest;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.util.slayer.enums.SlayerMaster;

public enum MntnSlayerMaster {
    TURAEL("Turael", SlayerMaster.TURAEL, 1, 1, null, new WorldPoint(2930, 3536, 0)); // TODO: update worldpoint
//    MAZCHNA("Mazchna", SlayerMaster.MAZCHNA, 20, 1, Quest.PRIEST_IN_PERIL, ), // TODO
//    VANNAKA("Vannaka", SlayerMaster.VANNAKA, 40, 1, null, new WorldPoint(3147, 9914, 0));

    private final String name;
    @Getter
    private final SlayerMaster runeliteSlayerMaster;
    @Getter
    private final int requiredCombatLevel;
    @Getter
    private final int requiredSlayerLevel;
    @Getter
    private final Quest requiredQuest;
    @Getter
    private final WorldPoint location;

    MntnSlayerMaster(String name, SlayerMaster runeliteSlayerMaster, int requiredCombatLevel, int requiredSlayerLevel, Quest requiredQuest, WorldPoint location) {
        this.name = name;
        this.runeliteSlayerMaster = runeliteSlayerMaster;
        this.requiredCombatLevel = requiredCombatLevel;
        this.requiredSlayerLevel = requiredSlayerLevel;
        this.requiredQuest = requiredQuest;
        this.location = location;
    }
}
