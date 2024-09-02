package net.runelite.client.plugins.microbot.mntn.birdhunter;

import net.runelite.api.GameObject;
import net.runelite.api.Skill;
import net.runelite.client.plugins.grounditems.GroundItem;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Item;
import net.runelite.client.plugins.microbot.util.models.RS2Item;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.pestcontrol.Game;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

enum State {
    WAITING,
    HANDLE_TRAP,
    MOVE_AWAY

}


public class BirdHunterScript extends Script {

    public final int BIRD_SNARE = 10006;

    public final int IDLE_TRAP = 9345;
    public final int MID_TRAP = 9347;
    public final int SUCCESSFUL_TRAP = 9348;
    public final int FAILED_TRAP = 9344;

    public static String version = "1.0.0";
    State state = State.HANDLE_TRAP;

    int availableBirdTrapsPerLevel = 1;

    public boolean run(BirdHunterConfig config) {
        initialPlayerLocation = null;
        Rs2Antiban.resetAntibanSettings();
        Rs2Antiban.antibanSetupTemplates.applyHunterSetup();
        Rs2AntibanSettings.actionCooldownChance = 0.1;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!super.run()) return;
                if (!Microbot.isLoggedIn()) return;
                if (Rs2AntibanSettings.actionCooldownActive) return;
                if (initialPlayerLocation == null) {
                    initialPlayerLocation = Rs2Player.getWorldLocation();
                }

                if (!config.BIRD().hasRequiredLevel()) {
                    Microbot.showMessage("You do not have the required hunter level to trap this bird.");
                    shutdown();
                    return;
                }

                availableBirdTrapsPerLevel = getAvailableTraps();





                if (Rs2Player.isMoving() || Rs2Player.isAnimating() || Microbot.pauseAllScripts) return;

                switch (state) {
                    case HANDLE_TRAP:
                        sleep(600);
                        /*
                                1) check if caught/fallen trap is near
                                    - if caught/fallen trap, pick it up
                                        - random chance of burying bones right then (0.2% of the time) if enabled
                                    - if not caught/fallen trap
                                        - check if another trap is near if proper level allows it
                                            - if another trap is near and level doesn't allow more, MOVE_AWAY
                                        - if no trap found, set trap
                                            - check if trap is set
                                        - MOVE_AWAY
                         */
                        if (Rs2Inventory.isFull()) {
                            buryBones();
                            dropItems();
                            break;
                        }

                        List<GameObject> successfulBirdSnares = Rs2GameObject.getGameObjects(SUCCESSFUL_TRAP);
                        if(!successfulBirdSnares.isEmpty()){
                            if(interactWithBirdSnare(successfulBirdSnares.get(0))){
                                Rs2Player.waitForXpDrop(Skill.HUNTER, true);
                                Rs2Antiban.actionCooldown();
                                Rs2Antiban.takeMicroBreakByChance();
                            } else{
                                break;
                            }
                        }

                        List<GameObject> middleBirdSnares = Rs2GameObject.getGameObjects(MID_TRAP);
                        if(!middleBirdSnares.isEmpty()){
                            break;
                        }

                        List<GameObject> failedBirdSnares = Rs2GameObject.getGameObjects(FAILED_TRAP);
                        if(!failedBirdSnares.isEmpty()){
                            if(interactWithBirdSnare(failedBirdSnares.get(0))){
                                Rs2Antiban.actionCooldown();
                                Rs2Antiban.takeMicroBreakByChance();
                            } else{
                                break;
                            }
                        }





                    List<GameObject> idleTraps = Rs2GameObject.getGameObjects(IDLE_TRAP);
                        if(idleTraps.size() < availableBirdTrapsPerLevel){
                            setTrap();
                        } else {
                            state = State.MOVE_AWAY;
                        }


                        break;
                    case MOVE_AWAY:
                        /*
                        1) Move random direction away
                                 - WAITING
                         */
//                        List<String> itemNames = Arrays.stream(config.itemsToBank().split(",")).map(String::toLowerCase).collect(Collectors.toList());
//
//                        if (config.useBank()) {
//                            if (!Rs2Bank.bankItemsAndWalkBackToOriginalPosition(itemNames, initialPlayerLocation, 0, config.distanceToStray()))
//                                return;
//                        } else {
//                            Rs2Inventory.dropAllExcept("pickaxe");
//                        }
//
                        state = State.HANDLE_TRAP;
                        break;
                    case WAITING:
                        /*
                            1) Check if trap is caught or fallen
                                - if trap found, HANDLE_TRAP
                                - if no trap found
                                    - antiban - 0.001 chance of checking xp for hunter
                                    - antiban - 0.01 chance of rotating camera
                                    - sleep randomly
                             */
//                        List<String> itemNames = Arrays.stream(config.itemsToBank().split(",")).map(String::toLowerCase).collect(Collectors.toList());
//
//                        if (config.useBank()) {
//                            if (!Rs2Bank.bankItemsAndWalkBackToOriginalPosition(itemNames, initialPlayerLocation, 0, config.distanceToStray()))
//                                return;
//                        } else {
//                            Rs2Inventory.dropAllExcept("pickaxe");
//                        }
//
//                        state = State.MINING;
                        break;
                }
            } catch (Exception ex) {
                Microbot.log(ex.getMessage());
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
        return true;
    }

    public void setTrap() {
        // Define the bird snare ID
        int birdSnareId = 10006; // Replace with the correct ID for bird snare

        // Check if a bird snare is in the inventory
        if (Rs2Inventory.contains(birdSnareId)) {
            // Get the bird snare item from the inventory
            Rs2Item birdSnare = Rs2Inventory.get(BIRD_SNARE);

            // Interact with the bird snare to lay it
            if (Rs2Inventory.interact(birdSnare, "Lay")) {
                // Wait for the trap to be placed
                sleep(2500); // Adjust timing as necessary

                //todo: implement success check
                System.out.println("Bird snare was successfully laid.");
            } else {
                System.out.println("Failed to interact with the bird snare.");
            }
        } else {
            System.out.println("No bird snare found in inventory.");
        }
    }

    public void pickUpBirdSnare() {
        Microbot.log("Picking up bird snares.. sike");
    }


    public boolean interactWithBirdSnare(GameObject birdSnare){
            Rs2GameObject.interact(birdSnare);

            // Wait a moment to allow the game to process the click
            sleep(1000);  // Adjust timing as necessary

            // Check for success
            boolean success = !Rs2GameObject.getGameObjects(birdSnare.getId()).contains(birdSnare); // Or check for a chat message

            if (success) {
                System.out.println("Bird snare interaction was successful.");
                return true;
            } else {
                System.out.println("Bird snare interaction failed.");
            }
        return false;
    }

    public void dropItems(){
        // drop all items except bird snares, coins, etc
    }

    public void buryBones(){
        //
    }

    public int getAvailableTraps(){
        int hunterLevel = Rs2Player.getRealSkillLevel(Skill.HUNTER);
        if(hunterLevel >= 80){
            return 5;
        } else if (hunterLevel >= 60){
            return 4;
        } else if (hunterLevel >= 40){
            return 3;
        } else if (hunterLevel >= 20){
            return 2;
        } else {
            return 1;
        }
    }
}
