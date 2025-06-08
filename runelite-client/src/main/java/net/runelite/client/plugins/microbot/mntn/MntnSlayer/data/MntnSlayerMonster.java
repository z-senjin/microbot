package net.runelite.client.plugins.microbot.mntn.MntnSlayer.data;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer;

@Getter
public enum MntnSlayerMonster {
    // Turael
    CAVE_CRAWLER("Cave crawler", 10, "Cave Crawlers", new WorldPoint[]{new WorldPoint(2786, 9996, 0)}, null, null, null, true),
    GUARD_DOG("Guard dog", 1, "Dogs", new WorldPoint[]{new WorldPoint(1763, 3597, 0)}, null, null, null, false),
    ZOMBIE("Zombie", 1, "Zombies", new WorldPoint[]{new WorldPoint(3147, 9903, 0)}, null, null, null, false),
    DWARF("Dwarf", 1, "Dwarves", new WorldPoint[]{new WorldPoint(3000, 9809, 0)}, null, null, null, false),
    BAT("Bat", 1, "Bats", new WorldPoint[]{new WorldPoint(3368, 3494, 0)}, null, null, null, false),
    MONKEY("Monkey", 1, "Monkeys", new WorldPoint[]{new WorldPoint(2879, 3155, 0)}, null, null, null, false),
    GHOST("Ghost", 1, "Ghosts", new WorldPoint[]{new WorldPoint(1691, 10063, 0)}, null, null, null, false),
    KALPHITE("Kalphite Worker", 1, "Kalphite", new WorldPoint[]{new WorldPoint(3325, 9500, 0)}, new String[]{"Rope", "Shantay pass"}, null, null, false),
    BEAR("Grizzly bear", 1, "Bears", new WorldPoint[]{new WorldPoint(2706, 3338, 0)}, new String[]{"Rope", "Shantay pass"}, null, null, false),
    BANSHEE("Banshee", 1, "Banshees", new WorldPoint[]{new WorldPoint(3440, 3539, 0)}, new String[]{"Earmuffs"}, null, null, false),
    SCORPION("Scorpion", 1, "Scorpions", new WorldPoint[]{new WorldPoint(3298, 3288, 0)}, null, null, null, false),
    LIZARD("Desert Lizard", 1, "Lizards", new WorldPoint[]{new WorldPoint(3471, 3054, 0)}, new String[]{"Shantay pass"}, "Ice cooler", null, false),
    CAVE_SLIME("Cave slime", 1, "Cave Slimes", new WorldPoint[]{new WorldPoint(3161, 9589, 0)}, new String[]{"Candle lantern", "Rope"}, "Ice cooler", null, true),
    WOLF("White wolf", 1, "Wolves", new WorldPoint[]{new WorldPoint(2864, 3447, 0)}, null, null, null, false),
    MINOTAUR("Minotaur", 1, "Minotaurs", new WorldPoint[]{new WorldPoint(1872, 5235, 0)}, null, null, null, false),
    CAVE_BUG("Cave bug", 1, "Cave Bugs", new WorldPoint[]{new WorldPoint(3153, 9573, 0)}, new String[]{"Candle lantern", "Rope"}, null, null, true),
    GOBLIN("Goblin", 1, "Goblins", new WorldPoint[]{new WorldPoint(3249, 3229, 0)}, null, null, null, false),
    SPIDER("Giant spider", 1, "Spiders", new WorldPoint[]{new WorldPoint(3169, 3244, 0)}, null, null, null, false),
    CRAWLING_HAND("Crawling Hand", 1, "Crawling Hands", new WorldPoint[]{new WorldPoint(3418, 3545, 0)}, null, null, null, false),
    ICEFIEND("Icefiend", 1, "Icefiends", new WorldPoint[]{new WorldPoint(3007, 3477, 0)}, null, null, null, false),
    SKELETON("Skeleton", 1, "Skeletons", new WorldPoint[]{new WorldPoint(3098, 9909, 0)}, null, null, null, false),
    GIANT_RAT("Giant rat", 1, "Rats", new WorldPoint[]{new WorldPoint(3201, 3192, 0)}, null, null, null, false),
    SEAGULL("Seagull", 1, "Birds", new WorldPoint[]{new WorldPoint(3028, 3236, 0)}, null, null, null, false);











    private final String monster;
    private final int slayerLevel;
    private final String category;
    private final WorldPoint[] locations;
    private final String[] itemsRequired;
    private final String usableSlayerItem;
    private final Rs2Prayer protectionPrayer;
    private final boolean poisonous;

    //private String[] attributes;
    //private String[] attackStyles;
    //private String[] alternatives;
    //private String[] slayerMasters;

    MntnSlayerMonster(String monster, int slayerLevel, String category, WorldPoint[] locations, String[] itemsRequired, String usableSlayerItem, Rs2Prayer protectionPrayer, boolean poisonous) {
        this.monster = monster;
        this.slayerLevel = slayerLevel;
        this.category = category;
        this.locations = locations;
        this.itemsRequired = itemsRequired;
        this.usableSlayerItem = usableSlayerItem;
        this.protectionPrayer = protectionPrayer;
        this.poisonous = poisonous;
//        this.attributes = attributes;
//        this.attackStyles = attackStyles;
//        this.alternatives = alternatives;
//        this.slayerMasters = slayerMasters;
    }


    // is valid monster
    public static boolean isValidMonster(String monster) {
        for (net.runelite.client.plugins.microbot.util.slayer.enums.SlayerTaskMonster task : net.runelite.client.plugins.microbot.util.slayer.enums.SlayerTaskMonster.values()) {
            if (task.getMonster().equals(monster)) {
                return true;
            }
            // check for alternatives
            for (String alternative : task.getAlternatives()) {
                if (alternative.equals(monster)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Get item requirements for a monster by name
    public static String[] getItemRequirements(String monster) {
        for (net.runelite.client.plugins.microbot.util.slayer.enums.SlayerTaskMonster task : net.runelite.client.plugins.microbot.util.slayer.enums.SlayerTaskMonster.values()) {
            if (task.getMonster().equalsIgnoreCase(monster)) {
                return task.getItemsRequired();
            }
            // check for alternatives
            for (String alternative : task.getAlternatives()) {
                if (alternative.equalsIgnoreCase(monster)) {
                    return task.getItemsRequired();
                }
            }
        }
        return new String[]{"None"};
    }

    // Get monster by name
    public static MntnSlayerMonster getMonsterByName(String monster) {
        for (MntnSlayerMonster slayerMonster : MntnSlayerMonster.values()) {
            if (slayerMonster.getMonster().equalsIgnoreCase(monster)) {
                return slayerMonster;
            }
        }
        return null;
    }

    // Get monster by category
    public static MntnSlayerMonster getMonsterByCategory(String category) {
        for (MntnSlayerMonster slayerMonster : MntnSlayerMonster.values()) {
            if (slayerMonster.getCategory().equalsIgnoreCase(category)) {
                return slayerMonster;
            }
        }
        return null;
    }


}
