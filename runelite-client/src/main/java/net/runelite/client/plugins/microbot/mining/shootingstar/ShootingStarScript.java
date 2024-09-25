package net.runelite.client.plugins.microbot.mining.shootingstar;

import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.inventorysetups.MInventorySetupsPlugin;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.mining.shootingstar.enums.Pickaxe;
import net.runelite.client.plugins.microbot.mining.shootingstar.enums.ShootingStarState;
import net.runelite.client.plugins.microbot.mining.shootingstar.model.Star;
import net.runelite.client.plugins.microbot.util.Rs2InventorySetup;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.antiban.enums.Activity;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Item;
import net.runelite.client.plugins.microbot.util.math.Random;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.tile.Rs2Tile;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ShootingStarScript extends Script {
    private final ShootingStarPlugin plugin;
    Rs2InventorySetup rs2InventorySetup;
    Pickaxe pickaxe;
    ShootingStarState state;
    Star star;
    private boolean hasEquipment = false;
    private boolean hasInventory = false;

    @Inject
    public ShootingStarScript(ShootingStarPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean run(ShootingStarConfig config) {
        Microbot.enableAutoRunOn = false;
        initialPlayerLocation = null;
        hasEquipment = false;
        hasInventory = false;
        Rs2Antiban.resetAntibanSettings();
        applyAntiBanSettings();
        Rs2Antiban.setActivity(Activity.GENERAL_MINING);
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();

                if (initialPlayerLocation == null) {
                    initialPlayerLocation = Rs2Player.getWorldLocation();
                }

                if (hasStateChanged()) {
                    state = updateStarState(config);
                }

                if (state == null) {
                    Microbot.showMessage("Unable to evaluate state");
                    shutdown();
                    return;
                }

                if (Rs2AntibanSettings.actionCooldownActive) return;
                if (Rs2Player.isMoving() || Rs2Antiban.getCategory().isBusy() || Microbot.pauseAllScripts) return;

                switch (state) {
                    case WAITING_FOR_STAR:

                        if (plugin.useNearestHighTierStar()) {
                            star = plugin.getClosestHighestTierStar();

                            if (star == null) {
                                Microbot.showMessage("Unable to find a star within your tier range. Consider disabling useNearestHighTierStar until higher mining level.");
                                shutdown();
                                return;
                            }

                            plugin.updateSelectedStar(star);
                        } else {
                            star = plugin.getSelectedStar();

                            if (!checkSelectedStar()) {
                                Microbot.log("Please select a star inside of the panel to start the script.");
                                sleepUntil(this::checkSelectedStar);
                                return;
                            }
                        }

                        state = ShootingStarState.WALKING;
                        break;
                    case WALKING:
                        if (Rs2Player.getWorld() != star.getWorldObject().getId()) {
                            Microbot.hopToWorld(star.getWorldObject().getId());
                            sleepUntil(() -> Microbot.getClient().getGameState() == GameState.LOGGED_IN);
                            return;
                        }

                        boolean isNearShootingStar = Rs2Player.getWorldLocation().distanceTo(star.getShootingStarLocation().getWorldPoint()) < 6;

                        if (!isNearShootingStar) {
                            Rs2Walker.walkTo(star.getShootingStarLocation().getWorldPoint(), 6);
                            return;
                        }

                        initialPlayerLocation = Rs2Player.getWorldLocation();

                        state = ShootingStarState.MINING;
                        break;
                    case MINING:
                        if (!star.hasRequirements()) {
                            Microbot.status = "Waiting for star to degrade";
                            Microbot.log("Unable to mine current star level, waiting..");
                            return;
                        }

                        if (Rs2Inventory.isFull()) {
                            state = ShootingStarState.BANKING;
                            return;
                        }

                        if (Rs2Equipment.isWearing("Dragon pickaxe"))
                            Rs2Combat.setSpecState(true, 1000);

                        GameObject starObject = (GameObject) Rs2GameObject.findObjectById(star.getObjectID());

                        if (starObject != null) {
                            Rs2GameObject.interact(starObject, "mine");
                            Rs2Antiban.actionCooldown();
                        }

                        break;
                    case BANKING:
                        boolean isBankOpen = Rs2Bank.walkToBankAndUseBank();
                        if (!isBankOpen || !Rs2Bank.isOpen()) return;

                        if (Rs2Inventory.hasItem("uncut")) {
                            Rs2Bank.depositAll(x -> x.name.toLowerCase().contains("uncut"));
                        }

                        if (isUsingInventorySetup(config)) {
                            if (!hasEquipment) {
                                hasEquipment = rs2InventorySetup.loadEquipment();
                                Rs2Random.wait(800, 1200);
                            }
                            if (!hasInventory && rs2InventorySetup.doesEquipmentMatch()) {
                                hasInventory = rs2InventorySetup.loadInventory();
                                Rs2Random.wait(800, 1200);
                            }

                            if (!hasEquipment || !hasInventory) return;
                        } else {
                            if (pickaxe == null) {
                                pickaxe = getBestPickaxe(Rs2Bank.bankItems());
                                if (pickaxe != null) {
                                    Rs2Bank.withdrawItem(pickaxe.getItemName());
                                    Rs2Random.wait(800, 1200);
                                } else {
                                    Microbot.showMessage("Unable to find pickaxe, please purchase a pickaxe");
                                    shutdown();
                                    return;
                                }
                            }
                        }

                        boolean bankClosed = Rs2Bank.closeBank();
                        if (!bankClosed || Rs2Bank.isOpen()) return;

                        if (checkSelectedStar()) {
                            if (!star.equals(plugin.getSelectedStar()))
                                return;

                            state = ShootingStarState.WALKING;
                            return;
                        }

                        state = ShootingStarState.WAITING_FOR_STAR;
                        break;
                }

                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                Microbot.log(ex.getMessage());
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
        state = null;
        star = null;
        pickaxe = null;
        Rs2Antiban.resetAntibanSettings();
    }

    private boolean isUsingInventorySetup(ShootingStarConfig config) {
        boolean isInventorySetupPluginEnabled = Microbot.isPluginEnabled(MInventorySetupsPlugin.class);
        boolean hasInventorySetupConfigured = MInventorySetupsPlugin.getInventorySetups().stream().anyMatch(x -> x.getName().equalsIgnoreCase(config.inventorySetupName()));

        return isInventorySetupPluginEnabled && hasInventorySetupConfigured;
    }

    private boolean checkSelectedStar() {
        return plugin.getSelectedStar() != null;
    }

    public boolean shouldBank(ShootingStarConfig config) {
        if (isUsingInventorySetup(config)) {
            hasEquipment = rs2InventorySetup.doesEquipmentMatch();
            hasInventory = rs2InventorySetup.doesInventoryMatch();
            System.out.printf("hasEquipment %s%nhasInventory %s%n", hasEquipment, hasInventory);

            return (!hasEquipment || !hasInventory) || Rs2Inventory.isFull();
        }
        return pickaxe == null || Rs2Inventory.isFull();
    }

    public ShootingStarState getState(ShootingStarConfig config) {
        if (shouldBank(config)) {
            return ShootingStarState.BANKING;
        }

        if (checkSelectedStar()) {
            return ShootingStarState.WALKING;
        }

        return ShootingStarState.WAITING_FOR_STAR;
    }

    private ShootingStarState updateStarState(ShootingStarConfig config) {
        if (state == null) {
            if (isUsingInventorySetup(config)) {
                rs2InventorySetup = new Rs2InventorySetup(config.inventorySetupName(), mainScheduledFuture);
                if (!rs2InventorySetup.hasSpellBook()) {
                    Microbot.showMessage("Your spellbook is not matching the inventory setup.");
                    shutdown();
                    return null;
                }
            } else {
                if (Rs2Inventory.hasItem("pickaxe") || Rs2Equipment.isWearing("pickaxe")) {
                    pickaxe = getBestPickaxe(Rs2Equipment.items());
                    if (pickaxe == null) {
                        pickaxe = getBestPickaxe(Rs2Inventory.items());
                    }
                }
            }
            return getState(config);
        }

        Star selectedStar = plugin.getSelectedStar();

        if (selectedStar == null) {
            if (shouldBank(config)) {
                return ShootingStarState.BANKING;
            }

            return ShootingStarState.WAITING_FOR_STAR;
        }

        if (!star.equals(selectedStar)) {
            star = selectedStar;
            if (state == ShootingStarState.MINING) {
                WorldPoint randomNearestWalkableTile = getNearestWalkableTile(1);
                Rs2Walker.walkFastCanvas(randomNearestWalkableTile);
            }
            if (state == ShootingStarState.WALKING) {
                Rs2Walker.setTarget(null);
                Rs2Player.waitForWalking();
            }
            return ShootingStarState.WALKING;
        }

        if (state == ShootingStarState.MINING) {
            GameObject starObject = Rs2GameObject.findObject("crashed star", false, 10, false, initialPlayerLocation);

            if (star == null || starObject == null) {
                plugin.removeStar(plugin.getSelectedStar());
                plugin.updatePanelList(true);

                if (shouldBank(config)) {
                    return ShootingStarState.BANKING;
                }

                return ShootingStarState.WAITING_FOR_STAR;
            }

            star.setObjectID(starObject.getId());
            star.setTier(star.getTierBasedOnObjectID());
            star.setMiningLevel(star.getRequiredMiningLevel());
        }
        return ShootingStarState.MINING;
    }

    private boolean hasStateChanged() {
        // If no state (on plugin start)
        if (state == null) return true;
        // If waiting for star or if you are returning to bank & no selected star, no state change (mainly for manual mode, but also to allow waiting for star to run)
        if (state == ShootingStarState.WAITING_FOR_STAR || (state == ShootingStarState.BANKING && plugin.getSelectedStar() == null))
            return false;
        // If you are walking or mining a star & the star becomes null
        if (plugin.getSelectedStar() == null) return true;
        // If the instance of the current star in the script does not equal the selected star in the plugin (only based on world & location)
        if (!star.equals(plugin.getSelectedStar())) return true;
        // If the state is mining state, scan the crashed star game object & check if the game object id has updated.
        if (state == ShootingStarState.MINING) {
            GameObject starObject = Rs2GameObject.findObject("crashed star", false, 10, false, initialPlayerLocation);
            return hasStarGameObjectChanged(starObject);
        }
        return false;
    }

    private boolean hasStarGameObjectChanged(GameObject starObject) {
        // If the GameObject does not exist anymore
        if (starObject == null) return true;

        // If the GameObject has updated to a new tier
        return star.getObjectID() != starObject.getId();
    }

    private WorldPoint getNearestWalkableTile(int distance) {
        List<WorldPoint> worldPoints = Rs2Tile.getWalkableTilesAroundPlayer(distance);
        WorldPoint playerLocation = Rs2Player.getWorldLocation();

        // Create a map to group tiles by their distance from the player
        Map<Integer, List<WorldPoint>> distanceMap = new HashMap<>();

        for (WorldPoint walkablePoint : worldPoints) {
            int tileDistance = playerLocation.distanceTo(walkablePoint);
            distanceMap.computeIfAbsent(tileDistance, k -> new ArrayList<>()).add(walkablePoint);
        }

        // Find the minimum distance that has walkable points
        Optional<Integer> minDistanceOpt = distanceMap.keySet().stream().min(Integer::compare);

        if (minDistanceOpt.isPresent()) {
            List<WorldPoint> closestPoints = distanceMap.get(minDistanceOpt.get());

            // Return a random point from the closest points
            if (!closestPoints.isEmpty()) {
                int randomIndex = Random.random(0, closestPoints.size());
                return closestPoints.get(randomIndex);
            }
        }

        // Recursively increase the distance if no valid point is found
        return getNearestWalkableTile(distance + 1);
    }

    public Pickaxe getBestPickaxe(List<Rs2Item> items) {
        Pickaxe bestPickaxe = null;

        for (Pickaxe pickaxe : Pickaxe.values()) {
            if (items.stream().noneMatch(i -> i.name.toLowerCase().contains(pickaxe.getItemName()))) continue;
            if (pickaxe.hasRequirements()) {
                if (bestPickaxe == null || pickaxe.getMiningLevel() > bestPickaxe.getMiningLevel()) {
                    bestPickaxe = pickaxe;
                }
            }
        }
        return bestPickaxe;
    }

    private void applyAntiBanSettings() {
        Rs2AntibanSettings.antibanEnabled = true;
        Rs2AntibanSettings.usePlayStyle = true;
        Rs2AntibanSettings.simulateFatigue = true;
        Rs2AntibanSettings.simulateAttentionSpan = true;
        Rs2AntibanSettings.behavioralVariability = true;
        Rs2AntibanSettings.nonLinearIntervals = true;
        Rs2AntibanSettings.naturalMouse = true;
        Rs2AntibanSettings.simulateMistakes = true;
        Rs2AntibanSettings.moveMouseOffScreen = true;
        Rs2AntibanSettings.contextualVariability = true;
        Rs2AntibanSettings.devDebug = false;
        Rs2AntibanSettings.playSchedule = true;
        Rs2AntibanSettings.actionCooldownChance = 0.35;
    }
}
