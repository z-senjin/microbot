package net.runelite.client.plugins.microbot.aiofighter;

import net.runelite.api.coords.WorldPoint;
import net.runelite.client.config.*;
import net.runelite.client.plugins.microbot.aiofighter.enums.DefaultLooterStyle;
import net.runelite.client.plugins.microbot.aiofighter.enums.PlayStyle;
import net.runelite.client.plugins.microbot.aiofighter.enums.PrayerStyle;
import net.runelite.client.plugins.microbot.aiofighter.enums.State;
import net.runelite.client.plugins.microbot.inventorysetups.InventorySetup;
import net.runelite.client.plugins.microbot.util.magic.Rs2CombatSpells;
import net.runelite.client.plugins.microbot.util.slayer.enums.SlayerMaster;

@ConfigGroup(AIOFighterConfig.GROUP)
@ConfigInformation("1. Make sure to place the cannon first before starting the plugin. <br />" +
        "2. Use food also supports Guthan's healing, the shield weapon is default set to Dragon Defender. <br />" +
        "3. Prayer, Combat, Ranging & AntiPoison potions are supported. <br />" +
        "4. Items to loot based your requirements. <br />" +
        "5. You can turn auto attack NPC off if you have a cannon. <br />" +
        "6. PrayFlick in different styles. <br />" +
        "7. SafeSpot you can Shift Right-click the ground to select the tile. <br />" +
        "8. Right-click NPCs to add them to the attack list. <br />")
public interface AIOFighterConfig extends Config {

    String GROUP = "PlayerAssistant";

    @ConfigSection(
            name = "Combat",
            description = "Combat",
            position = 10,
            closedByDefault = false
    )
    String combatSection = "Combat";
    @ConfigSection(
            name = "Slayer",
            description = "Slayer",
            position = 11,
            closedByDefault = true
    )
    String slayerSection = "Slayer";
    @ConfigSection(
            name = "Banking",
            description = "Banking settings",
            position = 992,
            closedByDefault = false
    )
    String banking = "Banking";
    //Gear section
    @ConfigSection(
            name = "Gear",
            description = "Gear",
            position = 55,
            closedByDefault = true
    )
    String gearSection = "Gear";
    // Safety section
    @ConfigSection(
            name = "Safety",
            description = "Safety",
            position = 54,
            closedByDefault = true
    )
    String safetySection = "Safety";

    @ConfigSection(
            name = "Food & Potions",
            description = "Food & Potions",
            position = 20,
            closedByDefault = false
    )
    String foodAndPotionsSection = "Food & Potions";
    @ConfigSection(
            name = "Loot",
            description = "Loot",
            position = 30,
            closedByDefault = false
    )
    String lootSection = "Loot";
    //Prayer section
    @ConfigSection(
            name = "Prayer",
            description = "Prayer",
            position = 40,
            closedByDefault = false
    )
    String prayerSection = "Prayer";
    //Skilling section
    @ConfigSection(
            name = "Skilling",
            description = "Skilling",
            position = 50,
            closedByDefault = false
    )
    String skillingSection = "Combat Skilling";

    @ConfigItem(
            keyName = "Combat",
            name = "Auto attack npc",
            description = "Attacks npc",
            position = 0,
            section = combatSection
    )
    default boolean toggleCombat() {
        return false;
    }

    @ConfigItem(
            keyName = "monster",
            name = "Attackable npcs",
            description = "List of attackable npcs",
            position = 1,
            section = combatSection
    )
    default String attackableNpcs() {
        return "";
    }

    @ConfigItem(
            keyName = "Attack Radius",
            name = "Attack Radius",
            description = "The max radius to attack npcs",
            position = 2,
            section = combatSection
    )
    default int attackRadius() {
        return 10;
    }

    @ConfigItem(
            keyName = "Use special attack",
            name = "Use special attack",
            description = "Use special attack",
            position = 3,
            section = combatSection
    )
    default boolean useSpecialAttack() {
        return false;
    }

    @ConfigItem(
            keyName = "Cannon",
            name = "Auto reload cannon",
            description = "Automatically reloads cannon",
            position = 4,
            section = combatSection
    )
    default boolean toggleCannon() {
        return false;
    }

    //safe spot
    @ConfigItem(
            keyName = "Safe Spot",
            name = "Safe Spot",
            description = "Shift Right-click the ground to select the safe spot tile",
            position = 5,
            section = combatSection
    )
    default boolean toggleSafeSpot() {
        return false;
    }

    //PlayStyle
    @ConfigItem(
            keyName = "PlayStyle",
            name = "Play Style",
            description = "Play Style",
            position = 6,
            section = combatSection
    )
    default PlayStyle playStyle() {
        return PlayStyle.AGGRESSIVE;
    }

    @ConfigItem(
            keyName = "ReachableNpcs",
            name = "Only attack reachable npcs",
            description = "Only attack npcs that we can reach with melee",
            position = 7,
            section = combatSection
    )
    default boolean attackReachableNpcs() {
        return true;
    }

    @ConfigItem(
            keyName = "Food",
            name = "Auto eat food",
            description = "Automatically eats food",
            position = 0,
            section = foodAndPotionsSection
    )
    default boolean toggleFood() {
        return false;
    }

    // Testing if full auto potion manager is preferred over individual potion toggles

//    @ConfigItem(
//            keyName = "Auto Prayer Potion",
//            name = "Auto prayer potion",
//            description = "Automatically drinks prayer potions",
//            position = 1,
//            section = foodAndPotionsSection
//    )
//    default boolean togglePrayerPotions() {
//        return false;
//    }
//
//    @ConfigItem(
//            keyName = "Combat potion",
//            name = "Auto combat potion",
//            description = "Automatically drinks combat potions",
//            position = 2,
//            section = foodAndPotionsSection
//    )
//    default boolean toggleCombatPotion() {
//        return false;
//    }
//
//    @ConfigItem(
//            keyName = "Ranging/Bastion potion",
//            name = "Auto Ranging/Bastion potion",
//            description = "Automatically drinks Ranging/Bastion potions",
//            position = 3,
//            section = foodAndPotionsSection
//    )
//    default boolean toggleRangingPotion() {
//        return false;
//    }
//
//    @ConfigItem(
//            keyName = "Magic/Battlemage potion",
//            name = "Auto Magic/Battlemage potion",
//            description = "Automatically drinks Magic/Battlemage potions",
//            position = 4,
//            section = foodAndPotionsSection
//    )
//    default boolean toggleMagicPotion() {
//        return false;
//    }
//
//    @ConfigItem(
//            keyName = "Use AntiPoison",
//            name = "Auto AntiPoison",
//            description = "Use AntiPoison",
//            position = 8,
//            section = foodAndPotionsSection
//    )
//    default boolean useAntiPoison() {
//        return false;
//    }
//
//    // use antifire potion
//    @ConfigItem(
//            keyName = "useAntifirePotion",
//            name = "Auto Antifire Potion",
//            description = "Use Antifire Potion",
//            position = 9,
//            section = foodAndPotionsSection
//    )
//    default boolean useAntifirePotion() {
//        return false;
//    }
//    // Use goading potion
//    @ConfigItem(
//            keyName = "useGoadingPotion",
//            name = "Auto Goading Potion",
//            description = "Use Goading Potion",
//            position = 10,
//            section = foodAndPotionsSection
//    )
//    default boolean useGoadingPotion() {
//        return false;
//    }

    @ConfigItem(
            keyName = "Loot items",
            name = "Auto loot items",
            description = "Enable/disable loot items",
            position = 0,
            section = lootSection
    )
    default boolean toggleLootItems() {
        return true;
    }

    @ConfigItem(
            name = "Loot Style",
            keyName = "lootStyle",
            position = 1,
            description = "Choose Looting Style",
            section = lootSection
    )
    default DefaultLooterStyle looterStyle() {
        return DefaultLooterStyle.MIXED;
    }

    @ConfigItem(
            name = "List of Items",
            keyName = "listOfItemsToLoot",
            position = 2,
            description = "List of items to loot",
            section = lootSection
    )
    default String listOfItemsToLoot() {
        return "bones,ashes";
    }

    @ConfigItem(
            keyName = "Min Price of items to loot",
            name = "Min. Price of items to loot",
            description = "Min. Price of items to loot",
            position = 10,
            section = lootSection
    )
    default int minPriceOfItemsToLoot() {
        return 5000;
    }

    @ConfigItem(
            keyName = "Max Price of items to loot",
            name = "Max. Price of items to loot",
            description = "Max. Price of items to loot default is set to 10M",
            position = 11,
            section = lootSection
    )
    default int maxPriceOfItemsToLoot() {
        return 10000000;
    }
    // toggle scatter

    @ConfigItem(
            keyName = "Loot arrows",
            name = "Auto loot arrows",
            description = "Enable/disable loot arrows",
            position = 20,
            section = lootSection
    )
    default boolean toggleLootArrows() {
        return false;
    }

    // toggle loot runes
    @ConfigItem(
            keyName = "Loot runes",
            name = "Loot runes",
            description = "Enable/disable loot runes",
            position = 30,
            section = lootSection
    )
    default boolean toggleLootRunes() {
        return false;
    }

    // toggle loot coins
    @ConfigItem(
            keyName = "Loot coins",
            name = "Loot coins",
            description = "Enable/disable loot coins",
            position = 40,
            section = lootSection
    )
    default boolean toggleLootCoins() {
        return false;
    }

    // toggle loot untreadables
    @ConfigItem(
            keyName = "Loot untradables",
            name = "Loot untradables",
            description = "Enable/disable loot untradables",
            position = 50,
            section = lootSection
    )
    default boolean toggleLootUntradables() {
        return false;
    }

    @ConfigItem(
            keyName = "Bury Bones",
            name = "Bury Bones",
            description = "Picks up and Bury Bones",
            position = 96,
            section = lootSection
    )
    default boolean toggleBuryBones() {
        return false;
    }

    @ConfigItem(
            keyName = "Scatter",
            name = "Scatter",
            description = "Picks up and Scatter ashes",
            position = 97,
            section = lootSection
    )
    default boolean toggleScatter() {
        return false;
    }

    // delayed looting
    @ConfigItem(
            keyName = "delayedLooting",
            name = "Delayed Looting",
            description = "Lets the loot stay on the ground for a while before picking it up",
            position = 98,
            section = lootSection
    )
    default boolean toggleDelayedLooting() {
        return false;
    }

    // only loot my items
    @ConfigItem(
            keyName = "onlyLootMyItems",
            name = "Only Loot My Items",
            description = "Only loot items that are dropped for/by you",
            position = 99,
            section = lootSection
    )
    default boolean toggleOnlyLootMyItems() {
        return false;
    }

    //Force loot regardless if we are in combat or not
    @ConfigItem(
            keyName = "forceLoot",
            name = "Force Loot",
            description = "Force loot regardless if we are in combat or not",
            position = 100,
            section = lootSection
    )
    default boolean toggleForceLoot() {
        return false;
    }

    //toggle High Alch profitable items
    @ConfigItem(
            keyName = "highAlchProfitable",
            name = "High Alch Profitable",
            description = "High Alch Profitable items",
            position = 101,
            section = lootSection
    )
    default boolean toggleHighAlchProfitable() {
        return false;
    }

    @ConfigItem(
            keyName =  "eatFoodForSpace",
            name = "Eat food for space",
            description = "Eats food before looting if low on space",
            position = 102,
            section = lootSection
    )
    default boolean eatFoodForSpace() { return false; }

    //set center tile manually
    @ConfigItem(
            keyName = "Center Tile",
            name = "Manual Center Tile",
            description = "Shift Right-click the ground to select the center tile",
            position = 6,
            section = combatSection
    )
    default boolean toggleCenterTile() {
        return false;
    }

    //Use quick prayer
    @ConfigItem(
            keyName = "Use prayer",
            name = "Use prayer",
            description = "Use prayer",
            position = 0,
            section = prayerSection
    )
    default boolean togglePrayer() {
        return false;
    }

    //Flick quick prayer
    @ConfigItem(
            keyName = "quickPrayer",
            name = "Quick prayer",
            description = "Use quick prayer",
            position = 1,
            section = prayerSection
    )
    default boolean toggleQuickPray() {
        return false;
    }

    //Lazy flick
    @ConfigItem(
            keyName = "prayerStyle",
            name = "Prayer Style",
            description = "Select type of prayer style to use",
            position = 2,
            section = prayerSection
    )
    default PrayerStyle prayerStyle() {
        return PrayerStyle.LAZY_FLICK;
    }

    //Prayer style guide
    @ConfigItem(
            keyName = "prayerStyleGuide",
            name = "Prayer Style Guide",
            description = "Prayer Style Guide",
            position = 3,
            section = prayerSection
    )
    default String prayerStyleGuide() {
        return "Lazy Flick: Flicks tick before hit\n" +
                "Perfect Lazy Flick: Flicks on hit\n" +
                "Continuous: Quick prayer is on when in combat\n" +
                "Always On: Quick prayer is always on";
    }

    // Enable skilling
    @ConfigItem(
            keyName = "enableSkilling",
            name = "Enable Skilling",
            description = "Enable Skilling",
            position = 0,
            section = skillingSection
    )
    default boolean toggleEnableSkilling() {
        return false;
    }

    // Use Magic
    @ConfigItem(
            keyName = "useMagic",
            name = "Use Magic",
            description = "Use Magic",
            position = 1,
            section = skillingSection
    )
    default boolean useMagic() {
        return false;
    }
    // Magic spell
    @ConfigItem(
            keyName = "magicSpell",
            name = "Auto Cast Spell",
            description = "Magic Auto Cast Spell",
            position = 2,
            section = skillingSection
    )
    default Rs2CombatSpells magicSpell() {
        return Rs2CombatSpells.WIND_STRIKE;
    }

    //Balance combat skills
    @ConfigItem(
            keyName = "balanceCombatSkills",
            name = "Balance combat skills",
            description = "Balance combat skills",
            position = 10,
            section = skillingSection
    )
    default boolean toggleBalanceCombatSkills() {
        return false;
    }

    //Avoid Controlled attack style
    @ConfigItem(
            keyName = "avoidControlled",
            name = "No Controlled Attack",
            description = "Avoid Controlled attack style so you won't accidentally train unwanted combat skills",
            position = 11,
            section = skillingSection
    )
    default boolean toggleAvoidControlled() {
        return true;
    }


    //Attack style change delay (Seconds)
    @ConfigItem(
            keyName = "attackStyleChangeDelay",
            name = "Change Delay",
            description = "Attack Style Change Delay In Seconds",
            position = 20,
            section = skillingSection
    )
    default int attackStyleChangeDelay() {
        return 60 * 15;
    }
    // Disable on Max combat
    @ConfigItem(
            keyName = "disableOnMaxCombat",
            name = "Disable on Max Combat",
            description = "Disable on Max Combat",
            position = 30,
            section = skillingSection
    )
    default boolean toggleDisableOnMaxCombat() {
        return true;
    }
    //Attack skill target
    @ConfigItem(
            keyName = "attackSkillTarget",
            name = "Attack Level Target",
            description = "Attack level target",
            position = 97,
            section = skillingSection
    )
    default int attackSkillTarget() {
        return 99;
    }

    //Strength skill target
    @ConfigItem(
            keyName = "strengthSkillTarget",
            name = "Strength Level Target",
            description = "Strength level target",
            position = 98,
            section = skillingSection
    )
    default int strengthSkillTarget() {
        return 99;
    }

    //Defence skill target
    @ConfigItem(
            keyName = "defenceSkillTarget",
            name = "Defence Level Target",
            description = "Defence level target",
            position = 99,
            section = skillingSection
    )
    default int defenceSkillTarget() {
        return 99;
    }


    // Use Inventory Setup
    @ConfigItem(
            keyName = "useInventorySetup",
            name = "Use Inventory Setup",
            description = "Use Inventory Setup, make sure to select consumables used in the bank section",
            position = 1,
            section = gearSection
    )
    default boolean useInventorySetup() {
        return false;
    }

    // Inventory setup selection TODO: Add inventory setup selection
    @ConfigItem(
            keyName = "InventorySetupName",
            name = "Inventory setup name",
            description = "Create an inventory setup in the inventory setup plugin and enter the name here",
            position = 99,
            section = gearSection
    )
    default InventorySetup inventorySetup() {
        return null;
    }

    @ConfigItem(
            keyName = "bank",
            name = "Bank",
            description = "If enabled, will bank items when inventory is full. If disabled, will just stop looting",
            position = 0,
            section = banking
    )
    default boolean bank() {
        return false;
    }

    //Minimum free inventory slots to bank
    @Range(max = 28)
    @ConfigItem(
            keyName = "minFreeSlots",
            name = "Min. free slots",
            description = "Minimum free inventory slots to bank, if less than this, will bank items",
            position = 1,
            section = banking
    )
    default int minFreeSlots() {
        return 5;
    }

    // checkbox to use stamina potions when banking
    @ConfigItem(
            keyName = "useStamina",
            name = "Use stamina potions",
            description = "Use stamina potions when banking",
            position = 2,
            section = banking
    )
    default boolean useStamina() {
        return false;
    }

    @ConfigItem(
            keyName = "staminaValue",
            name = "Stamina Potions",
            description = "Amount of stamina potions to withdraw",
            position = 2,
            section = banking
    )
    default int staminaValue() {
        return 0;
    }

    // checkbox to use food when banking
    @ConfigItem(
            keyName = "useFood",
            name = "Use food",
            description = "Use food when banking",
            position = 3,
            section = banking
    )
    default boolean useFood() {
        return false;
    }

    @ConfigItem(
            keyName = "foodValue",
            name = "Food",
            description = "Amount of food to withdraw",
            position = 3,
            section = banking
    )
    default int foodValue() {
        return 0;
    }

    // checkbox to use restore potions when banking
    @ConfigItem(
            keyName = "useRestore",
            name = "Use restore potions",
            description = "Use restore potions when banking",
            position = 4,
            section = banking
    )
    default boolean useRestore() {
        return false;
    }

    @ConfigItem(
            keyName = "restoreValue",
            name = "Restore Potions",
            description = "Amount of restore potions to withdraw",
            position = 4,
            section = banking
    )
    default int restoreValue() {
        return 0;
    }

    // checkbox to use prayer potions when banking
    @ConfigItem(
            keyName = "usePrayer",
            name = "Use prayer potions",
            description = "Use prayer potions when banking",
            position = 5,
            section = banking
    )
    default boolean usePrayer() {
        return false;
    }

    @ConfigItem(
            keyName = "prayerValue",
            name = "Prayer Potions",
            description = "Amount of prayer potions to withdraw",
            position = 5,
            section = banking
    )
    default int prayerValue() {
        return 0;
    }

    // checkbox to use antipoison potions when banking
    @ConfigItem(
            keyName = "useAntipoison",
            name = "Use antipoison potions",
            description = "Use antipoison potions when banking",
            position = 6,
            section = banking
    )
    default boolean useAntipoison() {
        return false;
    }

    @ConfigItem(
            keyName = "antipoisonValue",
            name = "Antipoison Potions",
            description = "Amount of antipoison potions to withdraw",
            position = 6,
            section = banking
    )
    default int antipoisonValue() {
        return 0;
    }

    // checkbox to use antifire potions when banking
    @ConfigItem(
            keyName = "useAntifire",
            name = "Use antifire potions",
            description = "Use antifire potions when banking",
            position = 7,
            section = banking
    )
    default boolean useAntifire() {
        return false;
    }

    @ConfigItem(
            keyName = "antifireValue",
            name = "Antifire Potions",
            description = "Amount of antifire potions to withdraw",
            position = 7,
            section = banking
    )
    default int antifireValue() {
        return 0;
    }

    // checkbox to use combat potions when banking
    @ConfigItem(
            keyName = "useCombat",
            name = "Use combat potions",
            description = "Use combat potions when banking",
            position = 8,
            section = banking
    )
    default boolean useCombat() {
        return false;
    }

    @ConfigItem(
            keyName = "combatValue",
            name = "Combat Potions",
            description = "Amount of combat potions to withdraw",
            position = 8,
            section = banking
    )
    default int combatValue() {
        return 0;
    }


    // checkbox to use teleportation items when banking
    @ConfigItem(
            keyName = "ignoreTeleport",
            name = "Ignore Teleport Items",
            description = "ignore teleport items when banking",
            position = 9,
            section = banking
    )
    default boolean ignoreTeleport() {
        return true;
    }

    // Safety section
    @ConfigItem(
            keyName = "useSafety",
            name = "Use Safety",
            description = "Use Safety",
            position = 0,
            section = safetySection
    )
    default boolean useSafety() {
        return false;
    }

    // Missing runes
    @ConfigItem(
            keyName = "missingRunes",
            name = "Missing Runes",
            description = "Go to bank and logout if missing runes",
            position = 1,
            section = safetySection
    )
    default boolean missingRunes() {
        return true;
    }
    // Missing arrows
    @ConfigItem(
            keyName = "missingArrows",
            name = "Missing Arrows",
            description = "Go to bank and logout if missing arrows",
            position = 2,
            section = safetySection
    )
    default boolean missingArrows() {
        return true;
    }
    // Missing food
    @ConfigItem(
            keyName = "missingFood",
            name = "Missing Food",
            description = "Go to bank and logout if missing food and banking isn't enabled",
            position = 3,
            section = safetySection
    )
    default boolean missingFood() {
        return true;
    }

    // Low health
    @ConfigItem(
            keyName = "lowHealth",
            name = "Low Health",
            description = "Go to bank and logout if low health",
            position = 4,
            section = safetySection
    )
    default boolean lowHealth() {
        return true;
    }
    // Health safety value
    @ConfigItem(
            keyName = "healthSafetyValue",
            name = "Health Safety %",
            description = "Health Safety %",
            position = 5,
            section = safetySection
    )
    default int healthSafetyValue() {
        return 25;
    }

    // Slayer mode
    @ConfigItem(
            keyName = "slayerMode",
            name = "Slayer Mode",
            description = "Slayer Mode",
            position = 0,
            section = slayerSection,
            hidden = true
    )
    default boolean slayerMode() {
        return false;
    }

    // Slayer master
    @ConfigItem(
            keyName = "slayerMaster",
            name = "Slayer Master",
            description = "Slayer Master",
            position = 1,
            section = slayerSection,
            hidden = true
    )
    default SlayerMaster slayerMaster() {
        return SlayerMaster.VANNAKA;
    }


    //hidden config item for state
    @ConfigItem(
            keyName = "state",
            name = "State",
            description = "State",
            hidden = true
    )
    default State state() {
        return State.IDLE;
    }

    // Hidden config item for inventory setup
    @ConfigItem(
            keyName = "inventorySetupHidden",
            name = "inventorySetupHidden",
            description = "inventorySetupHidden",
            hidden = true
    )
    default InventorySetup inventorySetupHidden() {
        return null;
    }

    //hidden config item for center location
    @ConfigItem(
            keyName = "centerLocation",
            name = "Center Location",
            description = "Center Location",
            hidden = true
    )
    default WorldPoint centerLocation() {
        return new WorldPoint(0, 0, 0);
    }

    //hidden config item for safe spot location
    @ConfigItem(
            keyName = "safeSpotLocation",
            name = "Safe Spot Location",
            description = "Safe Spot Location",
            hidden = true
    )
    default WorldPoint safeSpot() {
        return new WorldPoint(0, 0, 0);
    }

}


