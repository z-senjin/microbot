package net.runelite.client.plugins.microbot.mntn.MntnSlayer;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.EquipmentInventorySlot;
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
//import net.runelite.client.plugins.microbot.mntn.MntnSlayer.data.monsters.SlayerMonster;
//import net.runelite.client.plugins.microbot.mntn.MntnSlayer.data.monsters.SlayerMonsterLoader;
import net.runelite.client.plugins.microbot.shortestpath.Transport;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.camera.Rs2Camera;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.misc.Rs2Food;
import net.runelite.client.plugins.microbot.util.npc.MonsterLocation;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcManager;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer;
import net.runelite.client.plugins.microbot.util.slayer.Rs2Slayer;
import net.runelite.client.plugins.microbot.util.slayer.enums.SlayerTaskMonster;
import net.runelite.client.plugins.microbot.util.tile.Rs2Tile;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.slayer.Task;
import net.runelite.client.ui.overlay.OverlayManager;

import java.util.*;

import javax.inject.Inject;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

import static net.runelite.client.plugins.microbot.util.Global.sleep;
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
    private SlayerTaskMonster currentTask;
    private WorldPoint currentTaskLocation;
    private GearStyle currentCombatType = GearStyle.MELEE;
    private Rs2Prayer currentProtectionPrayer;


    private boolean hasStarted = false;


    @Override
    protected void startUp() throws Exception {
        if (overlayManager != null) {
            overlayManager.add(mntnSlayerOverlay);
        }
        Microbot.log("Loading json.");
        Rs2NpcManager.loadJson();
        hasStarted = false;
//        allMonsters = SlayerMonsterLoader.loadMonsters(); // Load monster json file
        state = MntnSlayerState.INITIALIZING;
        currentSlayerMaster = config.MASTER();
        mntnSlayerScript.run(config);

    }

    protected void loop(){

        if(!hasStarted){
            // Check if player can access slayer masterwi
            if(Rs2Player.getCombatLevel() <= currentSlayerMaster.getRequiredCombatLevel()){
                Microbot.showMessage("Combat level too low for selected slayer master.");
            }
            if(Rs2Player.getRealSkillLevel(Skill.SLAYER) <= currentSlayerMaster.getRequiredSlayerLevel()){
                Microbot.showMessage("Slayer level too low for selected slayer master.");
            }
            if(currentSlayerMaster.getRequiredQuest() != null && Rs2Player.getQuestState(currentSlayerMaster.getRequiredQuest()) != QuestState.FINISHED){
                Microbot.showMessage("Required quest not completed to access selected slayer master.");
            }
        }

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

                currentTask = null;
                currentTaskLocation = null;
                currentMonsterName = null;
                // If no task, get task
                if(!Rs2Slayer.hasSlayerTask() || Rs2Slayer.getSlayerTaskSize() == 0){
                    Microbot.log("No slayer task, setting state to get task.");
                    state = MntnSlayerState.GET_TASK;
                    return;
                }

                //TODO: fix
                //TODO: create a map of Task.java where it maps the plural name to the singular name
                Microbot.log("Current slayer task: " + Rs2Slayer.getSlayerTask());
                String category = Rs2Slayer.getSlayerTask();
                List<String> monsters = Rs2Slayer.getSlayerMonsters();
                if(monsters.size() > 0){
                    for (String m: monsters){
                        Microbot.log("Setting current task to " + m);
                        currentTask = SlayerTaskMonster.getMonsterByName(m);
                        currentMonsterName = m;
                        currentTaskLocation = Rs2NpcManager.getClosestLocation(m,3,true).getClosestToCenter();
                        if(Rs2Walker.canReach(currentTaskLocation)){
                            Microbot.log("CAN REACH SPOT!");
                            state = MntnSlayerState.BANK;
                            return;
                        }
                    }


                }
                Microbot.log("Task protective equipment: " + Rs2Slayer.getSlayerTaskProtectiveEquipment());



                //TODO:
                //         set current combat type?
                //         set current prayer protection? - check if monster has protection then check if prayer level


                break;
            case GET_TASK:
                Microbot.log("Getting task, walking to slayer master");
                if(!Rs2Player.isRunEnabled()) Rs2Player.toggleRunEnergy(true);
                Rs2Slayer.walkToSlayerMaster(currentSlayerMaster.getRuneliteSlayerMaster());
                Microbot.log("Getting assignment from slayer master NPC.");
                Rs2Npc.interact(currentSlayerMaster.getRuneliteSlayerMaster().getName(), "Assignment");
                if(Rs2Dialogue.isInDialogue()){
                    Rs2Dialogue.clickContinue();
                    Rs2Random.wait(400, 1000);
                    if(Rs2Slayer.hasSlayerTask()){
                        state = MntnSlayerState.BANK;
                        return;
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
                Microbot.log("Walking to bank.");
                Rs2Bank.walkToBank();
                sleepUntil(() -> Rs2Bank.isNearBank(5), 5000);

                if(!Rs2Bank.isNearBank(5)){
                    return;
                }

                //TODO: figure out how to find teleports
                List<WorldPoint> list = Collections.singletonList(currentTaskLocation);


                List<Transport> teleports = Rs2Slayer.prepareItemTransports(currentTaskLocation);
                if(!teleports.isEmpty()){
                    for(Transport t: teleports){
                        Microbot.log("Tele: " + t.getName());
                    }
                }

                Rs2Bank.openBank();

                sleepUntil(Rs2Bank::isOpen, 10000);
                Microbot.log("Bank is open, lets gear up.");

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
                sleepUntil(Rs2Inventory::isEmpty, 3000);
                Rs2Bank.depositEquipment();
                sleepUntil(Rs2Equipment::isNaked, 3000);

                String[] combatGearArray = combatGear.split(",\\s*");
                for (String gearItem: combatGearArray){
//                    Microbot.log("Grabbing " + gearItem);
                    Rs2Bank.withdrawAndEquip(gearItem);
                    sleepUntil(() -> Rs2Equipment.hasEquippedContains(gearItem), 2000);
                }

                if(!Rs2Equipment.hasEquippedSlot(EquipmentInventorySlot.WEAPON)){
                    Microbot.showMessage("No weapon equipped, configure a weapon.");
                    state = MntnSlayerState.EXIT;
                    return;
                }

                //TODO: grab teleport based on slayer master selected
                // Edge - Varrock Teleport
                // Burthope - Falador Teleport
                // etc
                if(!Rs2Bank.hasItem(config.TeleportType().getName()) || !Rs2Bank.hasItem(config.FoodType().getName())){
                    Microbot.showMessage("Out of food or teleports.");
                    state = MntnSlayerState.EXIT;
                    return;
                }
                Rs2Bank.withdrawX(config.TeleportType().getName(), 5);
                sleepUntil(() -> Rs2Inventory.contains(config.TeleportType().getName()), 1000);
                Rs2Bank.withdrawAll(config.FoodType().getName());
                sleepUntil(() -> Rs2Inventory.contains(config.FoodType().getName()), 1000);
                Rs2Bank.closeBank();
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
                Rs2Walker.walkTo(currentTaskLocation, 3);
                sleepUntil(() -> Rs2Player.distanceTo(currentTaskLocation) <= 5, 5000);
                Microbot.log("is near?" + Rs2Player.distanceTo(currentTaskLocation));
                if(Rs2Player.distanceTo(currentTaskLocation) <= 5 && Rs2Tile.isTileReachable(currentTaskLocation)){
                    state = MntnSlayerState.FIGHTING;
                    return;
                }
                // find location of monster
                // walk to monster
                // if near monster
                    // set state to FIGHTING
            case FIGHTING:

                if(Rs2Slayer.getSlayerTaskSize() == 0){
                    state = MntnSlayerState.TELEPORT;
                    return;
                }




                //TODO: add array of foods to config and check here
                if(!Rs2Inventory.contains(config.FoodType().getName())){
                    state = MntnSlayerState.TELEPORT;
                    return;
                }

                // TODO: check prayer here



                Rs2Player.eatAt(Rs2Random.between(45, 65));

                if(!Rs2Player.isRunEnabled() && Rs2Player.getRunEnergy() > 40){
                    Rs2Player.toggleRunEnergy(true);
                }

                Microbot.log("Current task: " + currentTask.getMonster());

                //TODO: add looting

                List<Rs2NpcModel> filteredAttackableNpcs = Rs2Npc.getAttackableNpcs(currentMonsterName)
                        .filter(npc -> npc.getWorldLocation().distanceTo(Rs2Player.getWorldLocation()) <= 15)
                        .sorted(Comparator.comparingInt((Rs2NpcModel npc) -> npc.getInteracting() == Microbot.getClient().getLocalPlayer() ? 0 : 1)
                                .thenComparingInt(npc -> Rs2Player.getRs2WorldPoint().distanceToPath(npc.getWorldLocation())))
                        .collect(Collectors.toList());

                final List<Rs2NpcModel> attackableNpcs = new ArrayList<>();
                Microbot.log("NPCS:" + filteredAttackableNpcs.size());
                for (var attackableNpc: filteredAttackableNpcs) {
                    if (attackableNpc == null || attackableNpc.getName() == null) continue;
                        if (Objects.equals(attackableNpc.getName(), currentMonsterName)) {
                            attackableNpcs.add(attackableNpc);
                        }
                }

                if(Rs2Combat.inCombat()){
                    return;
                }

                if (!attackableNpcs.isEmpty()) {
                    Rs2NpcModel npc = attackableNpcs.stream().findFirst().orElse(null);

                    if (!Rs2Camera.isTileOnScreen(npc.getLocalLocation()))
                        Rs2Camera.turnTo(npc);

                    Rs2Npc.interact(npc, "attack");
                    Microbot.status = "Attacking " + npc.getName();
                    sleepUntil(Rs2Player::isInteracting, 1000);

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
                    Rs2Inventory.interact(config.FoodType().getName(), config.TeleportType().getAction());
                    Rs2Inventory.waitForInventoryChanges(3000);
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


    protected void shutDown() {
        mntnSlayerScript.shutdown();
        overlayManager.remove(mntnSlayerOverlay);
    }


    int ticks = 10;
    @Subscribe
    public void onGameTick(GameTick tick)
    {
        //System.out.println(getName().chars().mapToObj(i -> (char)(i + 3)).map(String::valueOf).collect(Collectors.joining()));

        if (ticks > 0) {
            ticks--;
        } else {
            ticks = 10;
        }

    }

}
