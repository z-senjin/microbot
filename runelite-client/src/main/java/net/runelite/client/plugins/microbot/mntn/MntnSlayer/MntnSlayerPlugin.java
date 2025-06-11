package net.runelite.client.plugins.microbot.mntn.MntnSlayer;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.accountselector.AutoLoginPlugin;
import net.runelite.client.plugins.microbot.mntn.MntnSlayer.data.GearStyle;
import net.runelite.client.plugins.microbot.mntn.MntnSlayer.data.MntnSlayerMaster;
import net.runelite.client.plugins.microbot.mntn.MntnSlayer.data.MntnSlayerMonster;
import net.runelite.client.plugins.microbot.shortestpath.ShortestPathPlugin;
import net.runelite.client.plugins.microbot.shortestpath.Transport;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.camera.Rs2Camera;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grounditem.LootingParameters;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcManager;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer;
import net.runelite.client.plugins.microbot.util.slayer.Rs2Slayer;
import net.runelite.client.plugins.microbot.util.tile.Rs2Tile;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.ui.overlay.OverlayManager;

import java.util.*;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;

@PluginDescriptor(
        name = PluginDescriptor.Mntn + "Mntn Slayer",
        description = "Mntn Slayer",
        tags = {"slayer", "mntn", "combat"},
        enabledByDefault = false
)
@Slf4j
public class MntnSlayerPlugin extends Plugin {
    @Inject
    private MntnSlayerConfig config;
    @Provides
    MntnSlayerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(MntnSlayerConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private MntnSlayerOverlay mntnSlayerOverlay;

    @Inject
    MntnSlayerScript mntnSlayerScript;

    private MntnSlayerState state = MntnSlayerState.INITIALIZING;

//    private List<SlayerTaskMonster> allMonsters;

    private MntnSlayerMaster currentSlayerMaster;
    private String currentMonsterName;
    private MntnSlayerMonster currentTask;
    private WorldPoint currentTaskLocation;
    private GearStyle currentCombatType = GearStyle.MELEE;
    private Rs2Prayer currentProtectionPrayer;

    private MntnSlayerMonster[] customWalkingTasks = {MntnSlayerMonster.CAVE_SLIME, MntnSlayerMonster.CAVE_BUG, MntnSlayerMonster.MINOTAUR};

    private String[] wearableSlayerItems = new String[]{"Earmuffs"};

    private boolean needsRestore = false;
    private ArrayList<String> requiredTaskItems = new ArrayList<>();

    private boolean isResupplying = false;

    private Thread healthThread = null;

    @Override
    protected void startUp() throws Exception {
        if (overlayManager != null) {
            overlayManager.add(mntnSlayerOverlay);
        }
        Microbot.log("Loading json.");
        Rs2NpcManager.loadJson();
        state = MntnSlayerState.INITIALIZING;
        Rs2Antiban.resetAntibanSettings();
        Rs2AntibanSettings.naturalMouse = true;
        Rs2AntibanSettings.simulateMistakes = true;
        Rs2AntibanSettings.moveMouseOffScreen = true;
        Rs2AntibanSettings.moveMouseOffScreenChance = 0.07;
        Rs2AntibanSettings.moveMouseRandomly = true;
        Rs2AntibanSettings.moveMouseRandomlyChance = 0.04;
        mntnSlayerScript.run(config);

    }

    protected void loop(){
        // TODO check breakhandler and if less than 1 min teleport/bank if attacking otherwise just bank
        Microbot.status = state.toString();

        if(Rs2Inventory.hasItem(config.FoodType().getName()) && Rs2Player.getHealthPercentage() >= 50){
            Rs2Player.eatAt(50);
        }

        if(!Rs2Player.isRunEnabled() && Rs2Player.getRunEnergy() >= 40){
            Rs2Player.toggleRunEnergy(true);
        }

        switch(state){
            case INITIALIZING:


                checkConfiguration();

                resetInitValues();
                // If no task, get task
                if(!Rs2Slayer.hasSlayerTask() || Rs2Slayer.getSlayerTaskSize() == 0){
                    Microbot.log("No slayer task, setting state to get task.");
                    state = MntnSlayerState.GET_TASK;
                    return;
                }

                getTaskDetails();


                //TODO:
                //         set current combat type?
                //         set current prayer protection? - check if monster has protection then check if prayer level


                break;
            case GET_TASK:
                Microbot.log("Getting task, walking to slayer master");
                if(!Rs2Player.isRunEnabled()) Rs2Player.toggleRunEnergy(true);
                Rs2Slayer.walkToSlayerMaster(currentSlayerMaster.getRuneliteSlayerMaster());
                Rs2Npc.interact(currentSlayerMaster.getRuneliteSlayerMaster().getName(), "Assignment");
                if(Rs2Dialogue.isInDialogue()){
                    Rs2Dialogue.clickContinue();
                    Rs2Random.wait(400, 1000);
                    if(Rs2Slayer.hasSlayerTask()){
                        getTaskDetails();
                    }
                }
                // find slayer master from config and check level to see if able (level and quest requirements)
                    // if all is good, walk to slayer master
                    // talk to slayer master
                    // get new assignment
                    // store current task
                    // set current combat type?
                    // set current prayer protection? - check if monster has protection then check if prayer level
                    // bank

                break;
            case BANK:
                if(currentTaskLocation == null || currentTask == null){
                    state = MntnSlayerState.INITIALIZING;
                    return;
                }

                if(Rs2Slayer.getSlayerTaskSize() == 0){
                    state = MntnSlayerState.GET_TASK;
                    return;
                }

                Microbot.log("Walking to bank.");
                Rs2Bank.walkToBank();

                if(!Rs2Bank.isNearBank(15)){
                    return;
                }
                Microbot.log("Opening bank");
                Rs2Bank.openBank();

                sleepUntil(Rs2Bank::isOpen, 1000);

                if(!Rs2Bank.isOpen()) return;

                String combatGear = "";

                switch(currentCombatType){
                    case MELEE:
                        combatGear = config.MeleeGear();
                        break;
                    case RANGE:
                        combatGear = config.RangeGear();
                        break;
                    case MAGE:
                        combatGear = config.MageGear();
                        break;
                }

                Rs2Bank.depositAll();
                sleepUntil(Rs2Inventory::isEmpty, 500);


                List<Transport> teleports = Rs2Slayer.prepareItemTransports(currentTaskLocation);
                if (!teleports.isEmpty()) {
                    outerLoop:
                    for (Transport t : teleports) {
                        for (Set<Integer> itemIdSet : t.getItemIdRequirements()) {
                            for (int itemId : itemIdSet) {
                                if (Rs2Bank.hasItem(itemId) && Rs2Bank.withdrawX(itemId, 1)) {
                                    // Your logic here (if any)

                                    // Break out of all loops
                                    break outerLoop;
                                }
                            }
                        }
                    }
                }

                //TODO: get required items for task
//                String requiredSlayerItem = Rs2Slayer.getSlayerTaskProtectiveEquipment();
//
//                Microbot.log("required slayer item: " + requiredSlayerItem);
//


                if(!isResupplying){
                    Rs2Bank.depositEquipment();
                    sleepUntil(Rs2Equipment::isNaked, 3000);
                    List<String> shuffledGearList = new ArrayList<>(Arrays.asList(combatGear.split(",\\s*")));
                    Collections.shuffle(shuffledGearList);
                    for (String gearItem: shuffledGearList){
                        Rs2Bank.withdrawAndEquip(gearItem);
                        sleepUntil(() -> Rs2Equipment.hasEquippedContains(gearItem), 2000);
                    }

                    Microbot.log("Got gear, lets get other stuff.");
                }

                for(String reqItem: requiredTaskItems){
                    if(!Rs2Bank.hasItem(reqItem)){
                        Microbot.showMessage("Missing required item: " + reqItem);
                        state = MntnSlayerState.EXIT;
                        return;
                    }

                    Microbot.log("Grabbing " + reqItem);

                    //TODO: check if item is a HEAD wearable and see if we have slayer helm already on
                    if(Arrays.asList(wearableSlayerItems).contains(reqItem)){
                        Rs2Bank.withdrawAndEquip(reqItem);
                        sleepUntil(() -> Rs2Equipment.hasEquippedContains(reqItem), 2000);
                    } else {

                            Rs2Bank.withdrawX(reqItem, Objects.equals(reqItem, "Coins") ? 5000 : 1);
                            sleepUntil(() -> Rs2Inventory.contains(reqItem), 500);

                    }


                    // TODO:
                    // Check if reqItem is in wearbleslayeritems and wear it if slayer helmet is not apart of gear setup
                }

                String usableSlayerItem = currentTask.getUsableSlayerItem();
                if(usableSlayerItem != null){
                    if(Rs2Bank.hasItem(usableSlayerItem)){
                        Rs2Bank.withdrawAll(usableSlayerItem);
                        sleepUntil(() -> Rs2Inventory.contains(usableSlayerItem), 500);
                    } else {
                        Microbot.showMessage("Missing required item: " + usableSlayerItem);
                        state = MntnSlayerState.EXIT;
                        return;
                    }
                }



                if(!Rs2Equipment.hasEquippedSlot(EquipmentInventorySlot.WEAPON)){
                    Microbot.showMessage("No weapon equipped, configure a weapon.");
                    state = MntnSlayerState.EXIT;
                    return;
                }

                Microbot.log("Got weapon");

                //TODO: grab teleport based on slayer master selected
                // Edge - Varrock Teleport
                // Burthope - Falador Teleport
                // etc
                if(!Rs2Bank.hasItem(config.TeleportType().getName()) || !Rs2Bank.hasItem(config.FoodType().getName())){
                    Microbot.showMessage("Out of food or teleports.");
                    state = MntnSlayerState.EXIT;
                    return;
                }
                Microbot.log("Getting teleports");
                Rs2Bank.withdrawX(config.TeleportType().getName(), 3);
                sleepUntil(() -> Rs2Inventory.contains(config.TeleportType().getName()), 1000);
                if(currentTask.isPoisonous() && Rs2Bank.hasItem(Arrays.asList("Superantipoison", "Antipoison"))){
                    if(!Rs2Bank.withdrawX("Superantipoison", 2)){
                        Rs2Bank.withdrawX("Antipoison", 2);
                    }
                }
                if(needsRestore && Rs2Bank.hasItem("Super restore")){
                    Rs2Bank.withdrawX("Super restore", 4);
                }

                Microbot.log("GETTING FOOD");

                if(!Rs2Bank.hasItem(config.FoodType().getName())){
                    Microbot.showMessage("Out of " + config.FoodType().getName() + " - restock and start again.");
                    state = MntnSlayerState.EXIT;
                    return;
                }

                Microbot.log("Found food, withdrawing.");

                Rs2Bank.withdrawAll(config.FoodType().getName());
                sleepUntil(() -> Rs2Inventory.contains(config.FoodType().getName()), 1000);
                Rs2Bank.closeBank();
                isResupplying = false;
                state = MntnSlayerState.WALK_TO_MONSTER;

                // walk to closest bank
                // check current combat style
                    // grab gear from config
                    // if slayer monster has extra items, grab those
                    // equip it all
                // check if prayer protection
                    // grab prayer/super restore pots
                // grab super combat/range/etc *** EVENTUALLY
                // grab fast teleport
                // grab food
                // set state WALK_TO_MONSTER
                break;
            case WALK_TO_MONSTER:
                Microbot.log("Walking to slayer location.");

                if (healthThread == null || !healthThread.isAlive()) {
                    Microbot.log("Creating health thread.");
                    healthThread = new Thread(() -> {
                        while (state == MntnSlayerState.WALK_TO_MONSTER) {
                            if(Rs2Player.eatAt(50)){
                                Microbot.log("Eating food while running");
                            }
                            if (Rs2Dialogue.isInDialogue()) {
                                if (Rs2Dialogue.hasDialogueOption("yes", false)) {
                                    Microbot.log("Dialog has yes");
                                    Rs2Dialogue.clickOption("yes", false);
                                } else if(Rs2Dialogue.hasContinue()) {
                                    Rs2Dialogue.clickContinue();
                                }
                            }

                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                // Thread interrupted, exit loop
                                break;
                            }
                        }
                    });
                    healthThread.start();
                }

                if (Arrays.asList(customWalkingTasks).contains(currentTask)){
                    handleCustomWalkingPaths();
                    return;
                }

                Rs2Walker.walkTo(currentTaskLocation, 3);
                sleepUntil(() -> Rs2Player.distanceTo(currentTaskLocation) <= 3, 5000);
                Microbot.log("is near?" + Rs2Player.distanceTo(currentTaskLocation));
                ShortestPathPlugin.getPathfinderConfig().setUseBankItems(false);
                if (Rs2Player.distanceTo(currentTaskLocation) <= 3 && Rs2Tile.isTileReachable(currentTaskLocation)) {
                    // Stop the health thread
                    if (healthThread != null) healthThread.interrupt();
                    healthThread = null;

                    state = MntnSlayerState.FIGHTING;
                    return;
                }
                // find location of monster
                // walk to monster
                // if near monster
                    // set state to FIGHTING
            case FIGHTING:

                Microbot.log("Task monsters left:" + Rs2Slayer.getSlayerTaskSize());
                if(Rs2Slayer.getSlayerTaskSize() == 0){
                    state = MntnSlayerState.TELEPORT;
                    return;
                }




                //TODO: add array of foods to config and check here
                if(!Rs2Inventory.contains(config.FoodType().getName())){
                    isResupplying = true;
                    state = MntnSlayerState.TELEPORT;
                    return;
                }

                if(currentTask.getUsableSlayerItem() != null && !Rs2Inventory.contains(currentTask.getUsableSlayerItem())){
                    isResupplying = true;
                    state = MntnSlayerState.TELEPORT;
                    return;
                }

                // TODO: check prayer here


                if(currentTask.isPoisonous()){
                    Rs2Player.drinkAntiPoisonPotion();
                }

                if(needsRestore){
                    int currentPrayer = Rs2Player.getPrayerPercentage();
                    if (currentPrayer < 60) {
                        Rs2Player.drinkPrayerPotion();
                    }

                }


                Rs2Player.eatAt(Rs2Random.between(45, 65));

                if(!Rs2Player.isRunEnabled() && Rs2Player.getRunEnergy() > 40){
                    Rs2Player.toggleRunEnergy(true);
                }

                Microbot.log("Current task: " + currentTask.getMonster());

                //TODO: add looting
                if(!Rs2Inventory.isFull()){
                    Rs2GroundItem.lootItemBasedOnValue(3000, 10);
                    Rs2GroundItem.lootUntradables(new LootingParameters(5, 1, 1, 1, false, true, "untradeable"
                    ));
                }

                List<Rs2NpcModel> filteredAttackableNpcs = Rs2Npc.getAttackableNpcs(currentMonsterName)
                        .filter(npc -> npc.getWorldLocation().distanceTo(Rs2Player.getWorldLocation()) <= 15)
                        .sorted(Comparator.comparingInt((Rs2NpcModel npc) -> npc.getInteracting() == Microbot.getClient().getLocalPlayer() ? 0 : 1)
                                .thenComparingInt(npc -> Rs2Player.getRs2WorldPoint().distanceToPath(npc.getWorldLocation())))
                        .collect(Collectors.toList());

                final List<Rs2NpcModel> attackableNpcs = new ArrayList<>();
                Rs2NpcModel currentAttackableNpc = null;
                Microbot.log("NPCS:" + filteredAttackableNpcs.size());
                for (var attackableNpc: filteredAttackableNpcs) {
                    if (attackableNpc == null || attackableNpc.getName() == null) continue;
                        if (Objects.equals(attackableNpc.getName(), currentMonsterName)) {
                            if(currentTask.getUsableSlayerItem() != null){
                                double health = Rs2Npc.getHealth(attackableNpc);

                                if (health <= 5.0) {
                                    currentAttackableNpc = attackableNpc;
                                }
                            }
                            attackableNpcs.add(attackableNpc);
                        }
                }
                Microbot.log("In combat?" + Rs2Combat.inCombat());
                Microbot.log("Is animation? " + Rs2Player.isAnimating());
                if(Rs2Combat.inCombat() || Rs2Player.isAnimating() || Rs2Player.isMoving()){
                    if(currentTask.getUsableSlayerItem() != null){
                        List<Rs2NpcModel> monsters = Rs2Npc.getNpcs().collect(Collectors.toList());
                        for (Rs2NpcModel npc : monsters) {
                            if (currentTask.getMonster().equalsIgnoreCase(npc.getName())) {
                                double health = Rs2Npc.getHealth(npc);
                                if (health <= 5.0) {
                                    if (Rs2Inventory.use(currentTask.getUsableSlayerItem())) {
                                        Rs2Random.wait(100, 300);
                                        Rs2Npc.interact(npc, "Use");
                                        Rs2Random.wait(100, 300);
                                    }
                                }
                            }
                        }
                    }
                    return;
                } else {
                    Rs2Random.waitEx(1200, 300);
                    Microbot.log("In combat again?" + Rs2Combat.inCombat());
                    Microbot.log("Is animation again? " + Rs2Player.isAnimating());
                    if(Rs2Combat.inCombat() || Rs2Player.isAnimating() || Rs2Player.isMoving()){
                        return;
                    }
                }

                if (!attackableNpcs.isEmpty()) {
                    Rs2NpcModel npc = attackableNpcs.stream().findFirst().orElse(null);

                    if (!Rs2Camera.isTileOnScreen(npc.getLocalLocation()))
                        Rs2Camera.turnTo(npc);

                    if(Rs2Walker.canReach(npc.getWorldLocation())){
                        Rs2Npc.interact(npc, "attack");
                        Microbot.status = "Attacking " + npc.getName();
                        sleepUntil(Rs2Player::isInteracting, 1000);
                    } else {
                        Microbot.log("Can't reach NPC: " + npc.getName() + " at location: " + npc.getWorldLocation());
                    }

//                    if (config.togglePrayer()) {
//                        if (!config.toggleQuickPray()) {
//                            AttackStyle attackStyle = AttackStyleMapper
//                                    .mapToAttackStyle(Rs2NpcManager.getAttackStyle(npc.getId()));
//                            if (attackStyle != null) {
//                                switch (attackStyle) {
//                                    case MAGE:
//                                        Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_MAGIC, true);
//                                        break;
//                                    case MELEE:
//                                        Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_MELEE, true);
//                                        break;
//                                    case RANGED:
//                                        Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_RANGE, true);
//                                        break;
//                                }
//                            }
//                        } else {
//                            Rs2Prayer.toggleQuickPrayer(true);
//                        }
//                    }


                } else {
                    Microbot.log("No attackable NPC found");
                    state = MntnSlayerState.WALK_TO_MONSTER;
                    //TODO: check if near and set to walk to them if not
                }
                // check if slayer task is still going
                    // if finished, teleport
                // check if has prayer/restore if prayer enabled
                    // if out, teleport
                // check if has food
                    // if out, teleport
                // restore / eat if needed
                // attack monster if out of combat
                break;
            case TELEPORT:
                Microbot.log("Teleport");
                if(Rs2Inventory.contains(config.TeleportType().getName())){
                    Rs2Inventory.interact(config.TeleportType().getName(), config.TeleportType().getAction());
                    Rs2Inventory.waitForInventoryChanges(3000);
                    state = MntnSlayerState.BANK;
                } else {
                    state = MntnSlayerState.INITIALIZING;
                }
                // find teleport in inventory
                    // use it then set state to BANK
                // find jewerly teleport
                    // use it then set state to bank
                break;
            case EXIT:
                Microbot.stopPlugin(Microbot.getPlugin(AutoLoginPlugin.class.getName()));
                Rs2Player.logout();
                shutDown();
                break;
        }

    }

    void checkConfiguration(){
        currentSlayerMaster = config.MASTER();

        if(currentSlayerMaster.getRequiredCombatLevel() > Rs2Player.getCombatLevel()){
            Microbot.showMessage("Combat level not high enough for selected slayer master.");
            state = MntnSlayerState.EXIT;
            return;
        }

        if(currentSlayerMaster.getRequiredSlayerLevel() > Rs2Player.getRealSkillLevel(Skill.SLAYER)){
            Microbot.showMessage("Slayer level not high enough for selected slayer master.");
            state = MntnSlayerState.EXIT;
            return;
        }
        Quest requiredQuest = currentSlayerMaster.getRequiredQuest();
        if(requiredQuest != null){
            if(Rs2Player.getQuestState(requiredQuest) != QuestState.FINISHED){
                Microbot.showMessage("Required quest not completed for selected slayer master.");
                state = MntnSlayerState.EXIT;
                return;
            }
        }
    }

    void resetInitValues(){
        currentTask = null;
        currentTaskLocation = null;
        currentMonsterName = null;
        requiredTaskItems = new ArrayList<>();;

        Rs2Combat.enableAutoRetialiate();
    }

    void getTaskDetails(){
        String slayerCategory = Rs2Slayer.getSlayerTask();
        Microbot.log("Current slayer task: " + slayerCategory);
        currentTask = MntnSlayerMonster.getMonsterByCategory(slayerCategory);

        if(currentTask == null){
            Microbot.log("Can't find current task for:  " + slayerCategory);
            return;
        }

        for (WorldPoint location: currentTask.getLocations()) {
            Microbot.log("Location: " + location);
            Microbot.log("Can reach? " + Rs2Walker.canReach(location));
//            currentTaskLocation = location;
//            currentMonsterName = currentTask.getMonster();
//            getRequiredTaskItems();
//            state = MntnSlayerState.BANK;
                currentTaskLocation = location;
                currentMonsterName = currentTask.getMonster();
                getRequiredTaskItems();
                state = MntnSlayerState.BANK;
        }
    }

    void handleCustomWalkingPaths(){
        Microbot.log("Handling custom walking path for: " + currentTask.getMonster());

        if(currentTask == MntnSlayerMonster.CAVE_BUG || currentTask == MntnSlayerMonster.CAVE_SLIME){
            if(Rs2Walker.walkTo(new WorldPoint(3171, 3170, 0), 2)){
                Rs2Random.wait(3000, 5000);
                if(Rs2GameObject.interact(5947, "Climb-down")){
                    Rs2Random.wait(5500, 8000);
                    if(Rs2Walker.walkTo(currentTaskLocation, 2)){
                        state = MntnSlayerState.FIGHTING;
                    }

                }
            }

        }

        if(currentTask == MntnSlayerMonster.MINOTAUR){
            Rs2Walker.walkTo(new WorldPoint(3081, 3421, 0), 3);
            Rs2Walker.walkTo(new WorldPoint(1859, 5241, 0), 3);
            Rs2Walker.walkTo(currentTaskLocation, 3);
            if(Rs2Player.getWorldLocation().distanceTo(currentTaskLocation) <= 3){
                state = MntnSlayerState.FIGHTING;
            }
        }

    }

    void getRequiredTaskItems(){
        requiredTaskItems = new ArrayList<>();
        //TODO
        if(currentTask.getItemsRequired() != null){
            requiredTaskItems.addAll(Arrays.asList(currentTask.getItemsRequired()));
        }

    }


    protected void shutDown() {
        mntnSlayerScript.shutdown();
        overlayManager.remove(mntnSlayerOverlay);
    }


    int ticks = 10;
    @Subscribe
    public void onGameTick(GameTick tick)
    {

        //TODO: check if in same position for longer than 10 ticks? if not in combat
        //System.out.println(getName().chars().mapToObj(i -> (char)(i + 3)).map(String::valueOf).collect(Collectors.joining()));

        if (ticks > 0) {
            ticks--;
        } else {
            ticks = 10;
        }

    }

}
