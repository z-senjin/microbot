package net.runelite.client.plugins.microbot.bankjs.BanksBankStander;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import javax.inject.Inject;
import java.awt.event.KeyEvent;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static net.runelite.client.plugins.microbot.storm.common.Rs2Storm.getRandomItemWithLimit;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;
import static net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory.items;
import static net.runelite.client.plugins.microbot.util.math.Random.random;

// heaps of new features added by Storm
public class BanksBankStanderScript extends Script {
    @Inject
    private BanksBankStanderConfig config;
    public static double version = 1.5;

    int MAX_TRIES = 4;
    public static long previousItemChange;

    public static CurrentStatus currentStatus = CurrentStatus.FETCH_SUPPLIES;

    public static int itemsProcessed;


    static Integer thirdItemId;
    static Integer fourthItemId;

    static String firstItemIdentifier;

    static String thirdItemIdentifier;
    static String fourthItemIdentifier;

    private int firstItemQuantity;
    static Integer firstItemId;
    static String secondItemIdentifier;
    public static Integer secondItemId;
    private int secondItemQuantity;
    private int thirdItemQuantity;
    private int fourthItemQuantity;
    static int firstItemSum;
    static int secondItemSum;
    static int thirdItemSum;
    static int fourthItemSum;

    private int sleepMin;
    private int sleepMax;
    private int sleepTarget;

    public static boolean isWaitingForPrompt = false;
    private static boolean sleep;
    private static String menu;
    public static String firstIdentity;
    public static String secondIdentity;
    public static String thirdIdentity;
    public static String fourthIdentity;

    public boolean run(BanksBankStanderConfig config) {
        this.config = config; // Initialize the config object before accessing its parameters
        itemsProcessed = 0;
        firstItemSum = 0;
        secondItemSum = 0;
        thirdItemSum = 0;
        fourthItemSum = 0;
        // Initialize other variables
        firstItemIdentifier = config.firstItemIdentifier();
        firstItemQuantity = config.firstItemQuantity();
        secondItemIdentifier = config.secondItemIdentifier();
        thirdItemIdentifier = config.thirdItemIdentifier();
        fourthItemIdentifier = config.fourthItemIdentifier();


        secondItemQuantity = config.secondItemQuantity();
        thirdItemQuantity = config.thirdItemQuantity();
        fourthItemQuantity = config.fourthItemQuantity();

        sleepMin = config.sleepMin();
        sleepMax = config.sleepMax();
        if (config.sleepMax() > config.sleepMin() + 120) {
            sleepMax = config.sleepMax();
            sleepTarget = config.sleepTarget();
        } else {
            sleepMax = config.sleepMax() + random(120 - (config.sleepMax() - config.sleepMin()), 151);
            sleepTarget = sleepMin + ((sleepMax - sleepMin) / 2);
        }
        // Determine whether the first & second item is the ID or Name.
        firstItemId = TryParseInt(config.firstItemIdentifier());
        secondItemId = TryParseInt(config.secondItemIdentifier());
        thirdItemId = TryParseInt(config.thirdItemIdentifier());
        fourthItemId = TryParseInt(config.fourthItemIdentifier());
        firstIdentity = firstItemId != null ? "identified by ID" : "identified by name";
        secondIdentity = secondItemId != null ? "identified by ID" : "identified by name";
        thirdIdentity = thirdItemId != null ? "identified by ID" : "identified by name";
        fourthIdentity = fourthItemId != null ? "identified by ID" : "identified by name";
        // Print the types of firstItemIdentifier and firstItemId
        System.out.println("Type of firstItemIdentifier: " + firstItemIdentifier.getClass().getSimpleName());
        System.out.println("Type of firstItemId: " + (firstItemId != null ? firstItemId.getClass().getSimpleName() : "null"));
        if (config.secondItemQuantity() > 0) {
            // Print the types of secondItemIdentifier and secondItemId
            System.out.println("Type of secondItemIdentifier: " + secondItemIdentifier.getClass().getSimpleName());
            System.out.println("Type of secondItemId: " + (secondItemId != null ? secondItemId.getClass().getSimpleName() : "null"));
        }
        if (config.thirdItemQuantity() > 0) {
            // Print the types of thirdItemIdentifier and thirdItemId
            System.out.println("Type of thirdItemIdentifier: " + thirdItemIdentifier.getClass().getSimpleName());
            System.out.println("Type of thirdItemId: " + (thirdItemId != null ? thirdItemId.getClass().getSimpleName() : "null"));
        }
        if (config.fourthItemQuantity() > 0) {
            // Print the types of fourthItemIdentifier and fourthItemId
            System.out.println("Type of fourthItemIdentifier: " + fourthItemIdentifier.getClass().getSimpleName());
            System.out.println("Type of fourthItemId: " + (fourthItemId != null ? fourthItemId.getClass().getSimpleName() : "null"));

        }
        //menu = (firstItemId != null ? Microbot.getClientThread().runOnClientThread(() -> Microbot.getClient().getItemDefinition(firstItemId).getName().toLowerCase().contains("grimy")) : firstItemIdentifier.toLowerCase().contains("grimy")) ? "clean" : "use";
        menu = config.menu();
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!Microbot.isLoggedIn()) return;
            try {
                //start
                combineItems();

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 10, TimeUnit.MILLISECONDS);
        return true;
    }

    private boolean hasItems() {
        // Check if the player has the required quantity of both items using the configuration
        if (firstItemId != null && secondItemId != null) {
            // User has inputted the item id for both items.
            System.out.println("Checking for items by ID...");
            return Rs2Inventory.hasItem(firstItemId) &&
                    Rs2Inventory.hasItem(secondItemId);
        } else if (firstItemId != null) {
            // User has inputted the item id for the first item and item identifier for the second item.
            System.out.println("Checking for first item by ID and second item by identifier...");
            return Rs2Inventory.hasItem(firstItemId) &&
                    Rs2Inventory.hasItem(secondItemIdentifier);
        } else if (secondItemId != null) {
            // User has inputted the item id for the second item and item identifier for the first item.
            System.out.println("Checking for second item by ID and first item by identifier...");
            return Rs2Inventory.hasItem(firstItemIdentifier) &&
                    Rs2Inventory.hasItem(secondItemId);
        } else {
            // User has inputted the item identifier for both items.
            System.out.println("Checking for items by identifier...");
            return Rs2Inventory.hasItem(firstItemIdentifier) &&
                    Rs2Inventory.hasItem(secondItemIdentifier);
        }
    }


    private boolean fetchItems() {
        if (config.pause()) {
            while (this.isRunning() && config.pause()) {
                if (!config.pause() || !this.isRunning()) {
                    break;
                }
                sleep(100, 1000);
            }
        }
        if (currentStatus != CurrentStatus.FETCH_SUPPLIES) {
            currentStatus = CurrentStatus.FETCH_SUPPLIES;
        }
        sleep(calculateSleepDuration());
        if (!hasItems()) {
            if (!Rs2Bank.isOpen()) {
                Rs2Bank.openBank();
            }
            sleep = sleepUntilTrue(() -> Rs2Bank.isOpen(), random(67, 97), 18000);
            sleep(calculateSleepDuration());
            if (firstItemId != null && secondItemId != null && thirdItemId != null && fourthItemId != null) {
                Rs2Bank.depositAllExcept(firstItemId, secondItemId, thirdItemId, fourthItemId);
            } else if (firstItemId == null && secondItemId == null && thirdItemId == null && fourthItemId == null) {
                Rs2Bank.depositAllExcept(firstItemIdentifier, secondItemIdentifier, thirdItemIdentifier, fourthItemIdentifier);
            } else {
                Rs2Bank.depositAll();
            }
            sleep = sleepUntilTrue(() -> !Rs2Inventory.isFull(), 100, 6000);
            sleep(100, 300);
            if (!checkItemSums()) {
                return false;
            }
            if (firstItemId != null) {
                if (Rs2Bank.hasItem(firstItemId) && Rs2Inventory.count(firstItemId) < firstItemQuantity) {
                    int missingQuantity = Rs2Inventory.count(firstItemId) < firstItemQuantity
                            ? firstItemQuantity - Rs2Inventory.count(firstItemId)
                            : 0;
                    Rs2Bank.withdrawX(true, firstItemId, missingQuantity);
                    sleep(100, 300);
                }
            } else {
                if (Rs2Bank.hasItem(firstItemIdentifier) && Rs2Inventory.count(firstItemIdentifier) < firstItemQuantity) {
                    int missingQuantity = Rs2Inventory.count(firstItemIdentifier) < firstItemQuantity
                            ? firstItemQuantity - Rs2Inventory.count(firstItemIdentifier)
                            : 0;
                    Rs2Bank.withdrawX(true, firstItemIdentifier, missingQuantity);
                    sleep(100, 300);
                }
            }
            if (config.secondItemQuantity() > 0) {
                if (secondItemId != null) {
                    if (Rs2Bank.hasItem(secondItemId) && Rs2Inventory.count(secondItemId) < secondItemQuantity) {
                        if (!config.withdrawAll()) {
                            int missingQuantity = Rs2Inventory.count(secondItemId) < secondItemQuantity
                                    ? secondItemQuantity - Rs2Inventory.count(secondItemId)
                                    : 0;
                            Rs2Bank.withdrawX(true, secondItemId, missingQuantity);
                        } else {
                            Rs2Bank.withdrawAll(secondItemId);
                        }
                    }
                } else {
                    if (Rs2Bank.hasItem(secondItemIdentifier) && config.secondItemQuantity() > 0) {
                        if (!config.withdrawAll()) {
                            int missingQuantity = Rs2Inventory.count(secondItemIdentifier) < secondItemQuantity
                                    ? secondItemQuantity - Rs2Inventory.count(secondItemIdentifier)
                                    : 0;
                            Rs2Bank.withdrawX(true, secondItemIdentifier, missingQuantity);
                        } else {
                            Rs2Bank.withdrawAll(true, secondItemIdentifier);
                        }
                    }
                }
            }
            if (config.thirdItemQuantity() > 0) {
                if (thirdItemId != null) {
                    if (Rs2Bank.hasItem(thirdItemId) && Rs2Inventory.count(thirdItemId) < config.thirdItemQuantity()) {
                        int missingQuantity = Rs2Inventory.count(thirdItemId) < thirdItemQuantity
                                ? thirdItemQuantity - Rs2Inventory.count(thirdItemId)
                                : 0;
                        Rs2Bank.withdrawX(true, thirdItemId, missingQuantity);
                        sleep(100, 300);
                    }
                } else {
                    if (Rs2Bank.hasItem(thirdItemIdentifier) && Rs2Inventory.count(thirdItemIdentifier) < config.thirdItemQuantity()) {
                        int missingQuantity = Rs2Inventory.count(thirdItemIdentifier) < thirdItemQuantity
                                ? thirdItemQuantity - Rs2Inventory.count(thirdItemIdentifier)
                                : 0;
                        Rs2Bank.withdrawX(true, thirdItemIdentifier, missingQuantity);
                        sleep(100, 300);
                    }
                }
            }
            if (config.fourthItemQuantity() > 0) {
                if (fourthItemId != null) {
                    if (Rs2Bank.hasItem(fourthItemId)) {
                        if (!config.withdrawAll()) {
                            int missingQuantity = Rs2Inventory.count(fourthItemId) < fourthItemQuantity
                                    ? fourthItemQuantity - Rs2Inventory.count(fourthItemId)
                                    : 0;
                            Rs2Bank.withdrawX(true, fourthItemId, missingQuantity);
                        } else {
                            Rs2Bank.withdrawAll(fourthItemId);
                        }
                    }
                } else {
                    // User has inputted the item identifier for the fourth item.
                    if (Rs2Bank.hasItem(fourthItemIdentifier)) {
                        if (!config.withdrawAll()) {
                            int missingQuantity = Rs2Inventory.count(fourthItemIdentifier) < fourthItemQuantity
                                    ? fourthItemQuantity - Rs2Inventory.count(fourthItemIdentifier)
                                    : 0;
                            Rs2Bank.withdrawX(true, fourthItemIdentifier, missingQuantity);
                        } else {
                            Rs2Bank.withdrawAll(true, fourthItemIdentifier);
                        }
                    }
                }
            }
            if (!hasItems()) {
                Rs2Inventory.waitForInventoryChanges(700);
            }
            if (hasItems()) {
                previousItemChange = (System.currentTimeMillis() - 2500);
                if (firstItemSum == 0) {
                    firstItemSum = firstItemId != null ? (Rs2Bank.bankItems.stream().filter(item -> item.id == firstItemId).mapToInt(item -> item.quantity).sum() + Rs2Inventory.count(firstItemId)) : (Rs2Bank.count(firstItemIdentifier) + Rs2Inventory.count(firstItemIdentifier));
                }
                if (config.secondItemQuantity() > 0 && secondItemSum == 0) {
                    secondItemSum = secondItemId != null ? (Rs2Bank.bankItems.stream().filter(item -> item.id == secondItemId).mapToInt(item -> item.quantity).sum() + Rs2Inventory.count(secondItemId)) : (Rs2Bank.count(secondItemIdentifier) + Rs2Inventory.count(secondItemIdentifier));
                }
                if (config.thirdItemQuantity() > 0 && thirdItemSum == 0) {
                    thirdItemSum = thirdItemId != null ? (Rs2Bank.bankItems.stream().filter(item -> item.id == thirdItemId).mapToInt(item -> item.quantity).sum() + Rs2Inventory.count(thirdItemId)) : (Rs2Bank.count(thirdItemIdentifier) + Rs2Inventory.count(thirdItemIdentifier));
                }
                if (config.fourthItemQuantity() > 0 && fourthItemSum == 0) {
                    fourthItemSum = fourthItemId != null ? (Rs2Bank.bankItems.stream().filter(item -> item.id == fourthItemId).mapToInt(item -> item.quantity).sum() + Rs2Inventory.count(fourthItemId)) : (Rs2Bank.count(fourthItemIdentifier) + Rs2Inventory.count(fourthItemIdentifier));
                }
                long bankCloseTime = System.currentTimeMillis();
                while (this.isRunning() && Rs2Bank.isOpen() && (System.currentTimeMillis() - bankCloseTime < 32000)) {
                    closeBank();
                    sleep = sleepUntilTrue(() -> !Rs2Bank.isOpen(), random(60, 97), 5000);
                    sleep(calculateSleepDuration() - 10);
                }
                if (Rs2Bank.isOpen()) {
                    sleep(calculateSleepDuration());
                    Rs2Player.logout();
                    sleep(calculateSleepDuration());
                }
                currentStatus = CurrentStatus.COMBINE_ITEMS;
                return true;
            }
        }
        return true;
    }

    private boolean combineItems() {
        if (!hasItems()) {
            boolean fetchedItems = fetchItems();
            if (!fetchedItems) {
                Microbot.showMessage("Insufficient items found.");
                while (this.isRunning()) {
                    if (hasItems()) {
                        break;
                    }
                    sleep(300, 3000);
                }
            }
            return false;
        }
        if (Rs2Bank.isOpen()) {
            Rs2Bank.closeBank();
            sleep = sleepUntilTrue(() -> !Rs2Bank.isOpen(), random(60, 97), 5000);
            sleep(calculateSleepDuration());
            return false;
        }
        if (config.waitForAnimation()) {
            if (Rs2Player.isAnimating() || (System.currentTimeMillis() - previousItemChange) < 2400) {
                return false;
            }
        }
        if (currentStatus != CurrentStatus.COMBINE_ITEMS) {
            currentStatus = CurrentStatus.COMBINE_ITEMS;
        }
        if (config.pause()) {
            while (this.isRunning() && config.pause()) {
                if (!config.pause()) {
                    break;
                }
                sleep(100, 1000);
            }
        }
        if (firstItemId != null && secondItemId != null) {
            Rs2Inventory.interact(config.randomSelection() ? getRandomItemWithLimit(firstItemId, MAX_TRIES) : items().stream().filter(x -> x.id == firstItemId).findFirst().orElse(null), menu); // Use first Rs2Item (random or not)
            sleep(calculateSleepDuration());

            if (config.secondItemQuantity() > 0) {
                Rs2Inventory.interact(config.randomSelection() ? getRandomItemWithLimit(secondItemId, MAX_TRIES) : items().stream().filter(x -> x.id == secondItemId).findFirst().orElse(null), menu);
            }

        } else if (firstItemId != null) {
            Rs2Inventory.interact(getRandomItemWithLimit(firstItemId, MAX_TRIES), menu); // Use first Rs2Item (random or not)
            sleep(calculateSleepDuration());

            if (config.secondItemQuantity() > 0) {
                Rs2Inventory.interact(config.randomSelection() ? getRandomItemWithLimit(secondItemIdentifier, MAX_TRIES) : items().stream().filter(x -> x.name.equalsIgnoreCase(secondItemIdentifier.toLowerCase())).findFirst().orElse(null), menu);
            }
        } else if (secondItemId != null) {
            Rs2Inventory.interact(getRandomItemWithLimit(firstItemIdentifier, MAX_TRIES), menu); // Use first Rs2Item (random or not)
            sleep(calculateSleepDuration());

            if (config.secondItemQuantity() > 0) {
                Rs2Inventory.interact(config.randomSelection() ? getRandomItemWithLimit(secondItemId, MAX_TRIES) : items().stream().filter(x -> x.id == secondItemId).findFirst().orElse(null), menu);
            }
        } else {
            Rs2Inventory.interact(getRandomItemWithLimit(firstItemIdentifier, MAX_TRIES), menu); // Use first Rs2Item (random or not)
            sleep(calculateSleepDuration());

            if (config.secondItemQuantity() > 0) {
                Rs2Inventory.interact(config.randomSelection() ? getRandomItemWithLimit(secondItemIdentifier, MAX_TRIES) : items().stream().filter(x -> x.name.equalsIgnoreCase(secondItemIdentifier.toLowerCase())).findFirst().orElse(null), menu);
            }
        }

        if (config.needPromptEntry()) {
            sleep(calculateSleepDuration());
            isWaitingForPrompt = true;
            sleep = sleepUntilTrue(() -> !isWaitingForPrompt, random(7, 31), random(800, 1200));
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            previousItemChange = System.currentTimeMillis();
            sleep = sleepUntilTrue(() -> !Rs2Inventory.hasItem(secondItemIdentifier == null ? String.valueOf(secondItemId) : secondItemIdentifier), 10, 40000);
        }
        sleep(calculateSleepDuration());
        return true;
    }


    private int calculateSleepDuration() {
        // Create a Random object
        Random random = new Random();

        // Calculate the mean (average) of sleepMin and sleepMax, adjusted by sleepTarget
        double mean = (sleepMin + sleepMax + sleepTarget) / 3.0;

        // Calculate the standard deviation with added noise
        double noiseFactor = 0.2; // Adjust the noise factor as needed (0.0 to 1.0)
        double stdDeviation = Math.abs(sleepTarget - mean) / 3.0 * (1 + noiseFactor * (random.nextDouble() - 0.5) * 2);

        // Generate a random number following a normal distribution
        int sleepDuration;
        do {
            // Generate a random number using nextGaussian method, scaled by standard deviation
            sleepDuration = (int) Math.round(mean + random.nextGaussian() * stdDeviation);
        } while (sleepDuration < sleepMin || sleepDuration > sleepMax); // Ensure the duration is within the specified range

        return sleepDuration;
    }

    public void closeBank() {
        if (Rs2Bank.isOpen()) {
            Rs2Widget.clickChildWidget(786434, 11);
        }
    }

    public boolean checkItemSums() {
        if (!Rs2Bank.isOpen()) {
            Rs2Bank.openBank();
            sleep = sleepUntilTrue(() -> Rs2Bank.isOpen(), random(67, 97), 18000);
            sleep(200, 600);
        }
        //System.out.println("Attempting to check first item");
        if (firstItemId != null && ((Rs2Bank.bankItems.stream().filter(item -> item.id == firstItemId).mapToInt(item -> item.quantity).sum() + Rs2Inventory.count(firstItemId))) < config.firstItemQuantity()) {
            return false;
        } else if (firstItemId == null && (Rs2Bank.count(firstItemIdentifier) + Rs2Inventory.count(firstItemIdentifier)) < config.firstItemQuantity()) {
            return false;
        }
        //System.out.println("Attempting to check second item");
        if (config.secondItemQuantity() > 0) {
            if (secondItemId != null && ((Rs2Bank.bankItems.stream().filter(item -> item.id == secondItemId).mapToInt(item -> item.quantity).sum() + Rs2Inventory.count(secondItemId))) < config.secondItemQuantity()) {
                return false;
            } else if (secondItemId == null && (Rs2Bank.count(secondItemIdentifier) + Rs2Inventory.count(secondItemIdentifier)) < config.secondItemQuantity()) {
                return false;
            }
        }
        if (config.thirdItemQuantity() > 0) {
            //System.out.println("Attempting to check third item");
            if (thirdItemId != null && ((Rs2Bank.bankItems.stream().filter(item -> item.id == thirdItemId).mapToInt(item -> item.quantity).sum() + Rs2Inventory.count(thirdItemId))) < config.thirdItemQuantity()) {
                return false;
            } else if (thirdItemId == null && (Rs2Bank.count(thirdItemIdentifier) + Rs2Inventory.count(thirdItemIdentifier)) < config.thirdItemQuantity()) {
                return false;
            }
        }
        if (config.fourthItemQuantity() > 0) {
            //System.out.println("Attempting to check fourth item");
            if (fourthItemId != null && ((Rs2Bank.bankItems.stream().filter(item -> item.id == fourthItemId).mapToInt(item -> item.quantity).sum() + Rs2Inventory.count(fourthItemId))) < config.fourthItemQuantity()) {
                return false;
            } else if (fourthItemId == null && (Rs2Bank.count(fourthItemIdentifier) + Rs2Inventory.count(fourthItemIdentifier)) < config.fourthItemQuantity()) {
                return false;
            }
        }
        return true;
    }

    // method to parse string to integer, returns null if parsing fails
    public static Integer TryParseInt(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            System.out.println("Could not Parse Int from Item, using Name Instead");
            return null;
        }
    }
}
