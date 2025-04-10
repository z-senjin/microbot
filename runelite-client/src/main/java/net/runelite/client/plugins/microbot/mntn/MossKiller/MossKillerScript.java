package net.runelite.client.plugins.microbot.mntn.MossKiller;

import net.runelite.api.ItemComposition;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.breakhandler.BreakHandlerScript;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Item;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.math.Random;
import net.runelite.client.plugins.microbot.util.models.RS2Item;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.security.Login;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;

import java.awt.event.KeyEvent;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class MossKillerScript extends Script {

   //TODO:
    // 1) add settings for members, food, axe, key threshold
    // 2) add function to banking to check if count(mossy key) >= threshold
    // 2a) if threshold is met, set variable to attack boss
    // 3) write logic if attackBoss, walk to door
    // 4) if at door, handle door
    // 5) if passed door, check location.x (to see if in boss room) and set state to kill boss

    public static double version = 1.0;
    public static MossKillerConfig config;

    public boolean isStarted = false;
    public int playerCounter = 0;
    public boolean bossMode = false;


    public final WorldPoint SEWER_ENTRANCE = new WorldPoint(3237, 3459, 0);
    public final WorldPoint SEWER_LADDER = new WorldPoint(3237, 9859, 0);
    public final WorldPoint NORTH_OF_WEB = new WorldPoint(3210, 9900, 0);
    public final WorldPoint SOUTH_OF_WEB = new WorldPoint(3210, 9898, 0);
    public final WorldPoint OUTSIDE_BOSS_GATE_SPOT = new WorldPoint(3174, 9900, 0);
    public final WorldPoint INSIDE_BOSS_GATE_SPOT = new WorldPoint(3214, 9937, 0);
    public final WorldPoint MOSS_GIANT_SPOT = new WorldPoint(3165, 9879, 0);
    public final WorldPoint VARROCK_SQUARE = new WorldPoint(3212, 3422, 0);
    public final WorldPoint VARROCK_WEST_BANK = new WorldPoint(3253, 3420, 0);

    public final WorldPoint FEROX_ENCLAVE = new WorldPoint(3130, 3631, 0);


    // Items
    public final int AIR_RUNE = 556;
    public final int FIRE_RUNE = 554;
    public final int LAW_RUNE = 563;

    // TODO: convert axe and food to be a list of all available stuff
    public int BRONZE_AXE = 1351;
    public int FOOD = 379;
    public int KNIFE = 946;

    public int MOSSY_KEY = 22374;

    public int NATURE_RUNE = 561;
    public int DEATH_RUNE = 560;
    public int CHAOS_RUNE = 562;
    public int RANARR_SEED = 5295;
    // TODO: add stuff for boss too
    public int[] LOOT_LIST = new int[]{MOSSY_KEY, LAW_RUNE, AIR_RUNE, FIRE_RUNE, DEATH_RUNE, CHAOS_RUNE, NATURE_RUNE, RANARR_SEED};


    public MossKillerState state = MossKillerState.BANK;


    public boolean run(MossKillerConfig config) {
        MossKillerScript.config = config;
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();


                if(!isStarted){
                    init();
                }


                Microbot.log(String.valueOf(state));
                Microbot.log("BossMode: " + bossMode);

                // CODE HERE
                switch(state){
                    case BANK: handleBanking(); break;
                    case TELEPORT: varrockTeleport(); break;
                    case WALK_TO_BANK: walkToVarrockWestBank(); break;
                    case WALK_TO_MOSS_GIANTS: walkToMossGiants(); break;
                    case FIGHT_BOSS: handleBossFight(); break;
                    case FIGHT_MOSS_GIANTS: handleMossGiants(); break;
                    case EXIT_SCRIPT: sleep(10000, 15000); init(); break;
                }

                // switch statement to call functions based on state


                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }

    public void handleMossGiants() {
        WorldPoint playerLocation = Rs2Player.getWorldLocation();
        if (!Rs2Inventory.contains(FOOD) || BreakHandlerScript.breakIn <= 15){
            Microbot.log("Inventory does not contains FOOD or break in less than 15");
            state = MossKillerState.TELEPORT;
            return;
        }

        if(Rs2Walker.getDistanceBetween(playerLocation, MOSS_GIANT_SPOT) > 15){
            Microbot.log("getDistance between MOSS GIANT SPOT > 15");
            init();
            return;
        }

        Rs2Player.eatAt(Random.random(35, 60));

        // Check if loot is nearby and pick it up if it's in LOOT_LIST
        for (int lootItem : LOOT_LIST) {
            Rs2Item item = Rs2Inventory.get(lootItem);
            if(Rs2Inventory.contains(lootItem) && item.isStackable() && Rs2GroundItem.interact(lootItem, "Take", 10)){
                sleep(1000, 3000);
            }else if(!Rs2Inventory.isFull() && Rs2GroundItem.interact(lootItem, "Take", 10)){
                sleep(1000, 3000);
            }else if(Rs2Inventory.isFull() && Rs2GroundItem.exists(MOSSY_KEY, 10)){
                Rs2Player.eatAt(100);
                sleep(500, 1000);
                Rs2GroundItem.interact(MOSSY_KEY, "Take", 10);
                sleep(1000, 3000);
            }
        }

        Rs2GroundItem.lootItemBasedOnValue(1300, 10);

        // Check if any players are near
        if(!getNearbyPlayers(4).isEmpty() && config.hopWhenPlayerIsNear()){
            Microbot.log("Players nearby!! - playerCounter:" + playerCounter);
            // todo: add check in config if member or not
            if(playerCounter > 15) {
                Microbot.log("Players nearby... hopping");
                sleep(10000, 15000);
                int world = Login.getRandomWorld(false, null);
                if(world == 301){
                    return;
                }
                boolean isHopped = Microbot.hopToWorld(world);
                sleepUntil(() -> isHopped, 5000);
                if (!isHopped) return;
                playerCounter = 0;
                if(Random.random(0, 5) > 3){
                    Rs2Inventory.open();
                }
                return;
            }
            playerCounter++;
        } else {
            playerCounter = 0;
        }

        if (!Rs2Combat.inCombat()) {
            Rs2Npc.attack("Moss giant");
        }

        sleep(800, 2000);
    }

    public List<Player> getNearbyPlayers(int distance) {
        WorldPoint playerLocation = Rs2Player.getWorldLocation();
        List<Player> players = Rs2Player.getPlayers();

        return players.stream()
                .filter(p -> p != null && p.getWorldLocation().distanceTo(playerLocation) <= distance)
                .collect(Collectors.toList());
    }

    public void handleBossFight(){
        toggleRunEnergy();
        boolean growthlingAttacked = false;

        if(!Rs2Inventory.contains(BRONZE_AXE) || !Rs2Inventory.contains(FOOD)){
            state = MossKillerState.TELEPORT;
            return;
        }


        if (Rs2Player.eatAt(Random.random(35, 55))) {
            sleep(750, 1250);
        }
        List<NPC> monsters = Microbot.getClient().getNpcs();


        for (NPC npc : monsters) {
            if ("Growthling".equals(npc.getName()) || npc.getId() == 8194) {

                if(!growthlingAttacked){
                    if (Rs2Npc.interact(npc.getId(), "attack")) {
                        Microbot.log("Attacking growthling!");
                        growthlingAttacked = true;
                        sleep(250, 1000);
                    }
                } else {
                    Rs2Npc.attack(npc.getId());
                }


                double health = Rs2Npc.getHealth(npc);

                if (health <= 10.0) {
                    if (Rs2Inventory.use(BRONZE_AXE)) {
                        sleep(750, 1000);
                        Rs2Npc.interact(npc, "Use");
                        sleep(750, 1250);
                    }
                }
            }
        }

        if(Rs2Npc.getNpc("Bryophyta") == null){
            Microbot.log("Boss is dead, lets loot.");
            RS2Item[] groundItems = Rs2GroundItem.getAll(10);
            if(groundItems.length > 0){
                for (RS2Item item : groundItems){
                    if (item != null){
                        if(Rs2Inventory.isFull()){
                            Rs2Player.eatAt(100);
                            sleep(1000, 2000);
                        }
                        if(Rs2GroundItem.interact(item)){
                            sleepUntil(() -> Rs2Inventory.contains(item.getItem().getId()), 10000);
                            sleep(250, 750);
                        }
                    }
                }
                sleep(1000, 3000);
                state = MossKillerState.TELEPORT;
                return;
            }
        } else if(!growthlingAttacked){
            Rs2Npc.attack(Rs2Npc.getNpc("Bryophyta"));
        }
    }


    public void walkToMossGiants(){
        WorldPoint playerLocation = Rs2Player.getWorldLocation();

        toggleRunEnergy();

        if(Rs2Walker.getDistanceBetween(playerLocation,VARROCK_WEST_BANK) < 10 || Rs2Walker.getDistanceBetween(playerLocation, VARROCK_SQUARE) < 10){ // if near bank
            Rs2Walker.walkTo(SEWER_ENTRANCE, 2);
            sleepUntil(() -> Rs2Walker.getDistanceBetween(playerLocation, SEWER_ENTRANCE) < 3 && !Rs2Player.isMoving(), 300);
            return;
        }

        if(Rs2Walker.getDistanceBetween(playerLocation, SEWER_ENTRANCE) < 10){
            if(Rs2GameObject.exists(882)){ // open manhole
                Rs2GameObject.interact(882, "Climb-down");
                return;
            }
            if(Rs2GameObject.exists(881)){ // closed manhole
                Rs2GameObject.interact(881, "Open");
                return;
            }
        }

        if(Rs2Walker.getDistanceBetween(playerLocation, SEWER_LADDER) < 10){
            Rs2Walker.walkTo(NORTH_OF_WEB, 2);
            sleepUntil(() -> Rs2Walker.getDistanceBetween(playerLocation, NORTH_OF_WEB) < 5 && !Rs2Player.isMoving(), 300);
            return;
        }

        if(Rs2Walker.getDistanceBetween(playerLocation, NORTH_OF_WEB) < 5 || Rs2Walker.getDistanceBetween(playerLocation, SOUTH_OF_WEB) < 5){
            if(Rs2GameObject.exists(733)) { // web
                Rs2GameObject.interact(733, "Slash");
                return;
            }
            //TODO: check if fighting boss and walk there instead
            if(bossMode){
                BreakHandlerScript.setLockState(true);
                Microbot.log("Walking to outside boss gaet spot");
                Rs2Walker.walkTo(OUTSIDE_BOSS_GATE_SPOT, 2);
                sleepUntil(() -> Rs2Walker.getDistanceBetween(playerLocation, OUTSIDE_BOSS_GATE_SPOT) < 4 && !Rs2Player.isMoving(), 300);
            } else{ Rs2Walker.walkTo(MOSS_GIANT_SPOT, 2);
                sleepUntil(() -> Rs2Walker.getDistanceBetween(playerLocation, MOSS_GIANT_SPOT) < 3 && !Rs2Player.isMoving(), 300);

            }
           return;
        }

        if(bossMode && Rs2Walker.getDistanceBetween(playerLocation, OUTSIDE_BOSS_GATE_SPOT) < 4){
            if(Rs2GameObject.exists(32534) && Rs2GameObject.interact(32534, "Open")){
                sleep(2000);
                if(Rs2Dialogue.isInDialogue()){
                    sleep(500);
                    Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                    sleep(1000, 3000);
                    Rs2Keyboard.typeString("1");
                    sleep(2500, 3000);
                if(!Rs2Dialogue.isInDialogue()){
                    state = MossKillerState.FIGHT_BOSS;
                    return;
                }
                }
            }
            // set state to fight boss
        }

        if(Rs2Walker.getDistanceBetween(playerLocation, MOSS_GIANT_SPOT) < 5){
            state = MossKillerState.FIGHT_MOSS_GIANTS;
            return;
        }

        if(!Rs2Player.isWalking() && !Rs2Player.isAnimating()){
            Rs2Walker.walkTo(NORTH_OF_WEB, 2);
        }

        state = MossKillerState.WALK_TO_MOSS_GIANTS;
    }


   public void handleBanking() {
    if (Rs2Bank.openBank()) {
        sleepUntil(Rs2Bank::isOpen, 3000);
        sleep(400, 900);
        Rs2Bank.depositAll();
        sleepUntil(Rs2Inventory::isEmpty, 3000);
        sleep(1000, 1500);

        if (!Rs2Bank.hasItem(AIR_RUNE) || !Rs2Bank.hasItem(LAW_RUNE) 
            || !Rs2Bank.hasItem(FIRE_RUNE) || !Rs2Bank.hasItem(FOOD)) {
            state = MossKillerState.EXIT_SCRIPT;
            return;
        }

        int keyTotal = Rs2Bank.count("Mossy key");
        Microbot.log("Key Total: " + keyTotal);

        if (keyTotal >= config.keyThreshold()) {
            Microbot.log("keyTotal >= config threshold");
            bossMode = true;
            Rs2Bank.withdrawOne(MOSSY_KEY);
            sleep(500, 1200);
            Rs2Bank.withdrawOne(BRONZE_AXE);
            sleep(500, 1200);
        } else if (bossMode && keyTotal > 0) {
            Microbot.log("bossMode and keyTotal > 0");
            Rs2Bank.withdrawOne(MOSSY_KEY);
            sleep(500, 1200);
            Rs2Bank.withdrawOne(BRONZE_AXE);
            sleep(500, 1200);
        } else if (keyTotal == 0) {
            Microbot.log("keyTotal == 0");
            bossMode = false;
        }

        Microbot.log(String.valueOf(config.isSlashWeaponEquipped()));

        if(!config.isSlashWeaponEquipped()){
            Rs2Bank.withdrawOne(KNIFE);
            sleep(500, 1200);
        }

        // Randomize withdrawal order and add sleep between each to mimic human behavior
        withdrawItemWithRandomSleep(AIR_RUNE, FIRE_RUNE, LAW_RUNE, FOOD);

        if (Rs2Inventory.containsAll(new int[]{AIR_RUNE, FIRE_RUNE, LAW_RUNE, FOOD})) {
            if (Rs2Bank.closeBank()) {
                state = MossKillerState.WALK_TO_MOSS_GIANTS;
            }
        }
    }
    sleep(500, 1000);
}

private void withdrawItemWithRandomSleep(int... itemIds) {
    for (int itemId : itemIds) {
        Rs2Bank.withdrawAll(itemId);
        sleepUntil(() -> Rs2Inventory.contains(itemId), 3000);
        sleep(300, 700);
    }
}


    public void walkToVarrockWestBank(){
        BreakHandlerScript.setLockState(false);
        WorldPoint playerLocation = Rs2Player.getWorldLocation();
        toggleRunEnergy();
        if(!bossMode && Rs2Inventory.containsAll(new int[]{AIR_RUNE, FIRE_RUNE, LAW_RUNE, FOOD})){
            state = MossKillerState.WALK_TO_MOSS_GIANTS;
            return;
        }
        if(Rs2Walker.getDistanceBetween(playerLocation, VARROCK_WEST_BANK) > 6){
            Rs2Walker.walkTo(VARROCK_WEST_BANK, 4);
        } else {
            System.out.println("distance to varrock west bank < 5, bank now");
            state = MossKillerState.BANK;
        }
    }

    public void varrockTeleport(){
        WorldPoint playerLocation = Rs2Player.getWorldLocation();
        Microbot.log(String.valueOf(Rs2Walker.getDistanceBetween(playerLocation, VARROCK_SQUARE)));
        sleep(1000, 2000);
        if(Rs2Walker.getDistanceBetween(playerLocation, VARROCK_SQUARE) <= 10 && playerLocation.getY() < 5000){
            state = MossKillerState.WALK_TO_BANK;
            return;
        }
        if(Rs2Inventory.containsAll(AIR_RUNE, FIRE_RUNE, LAW_RUNE)){
            Rs2Magic.cast(MagicAction.VARROCK_TELEPORT);
        } else {
            state = MossKillerState.WALK_TO_BANK;
        }
        sleep(2000, 3500);

    }

    public void toggleRunEnergy(){
        if(Microbot.getClient().getEnergy() > 4000 && !Rs2Player.isRunEnabled()){
            Rs2Player.toggleRunEnergy(true);
        }
    }


    public void getInitiailState(){
        WorldPoint playerLocation = Rs2Player.getWorldLocation();
        if(Rs2Walker.getDistanceBetween(playerLocation, VARROCK_SQUARE) < 10 || Rs2Walker.getDistanceBetween(playerLocation, VARROCK_WEST_BANK) < 10){
            state = MossKillerState.WALK_TO_BANK;
            return;
        }

        if(Rs2Walker.getDistanceBetween(playerLocation, MOSS_GIANT_SPOT) < 10){
            state = MossKillerState.FIGHT_MOSS_GIANTS;
            return;
        }


        System.out.println("Must start near varrock square, bank, or moss giatn spot.");
        state = MossKillerState.EXIT_SCRIPT;
    }

    public void init(){
        //todo: set up food
        //todo:  set up membs
        //todo: set up loot filter
        // check state



        getInitiailState();

        if(!Rs2Combat.enableAutoRetialiate()){
            System.out.println("Could not turn on auto retaliate.");
            state = MossKillerState.EXIT_SCRIPT;
        }

        isStarted = true;
    }
}
