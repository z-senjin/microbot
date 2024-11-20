package net.runelite.client.plugins.microbot.nateplugins.misc.cluehunter;

import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

import java.util.concurrent.TimeUnit;

public class ClueScript extends Script {

    public static double version = 1.1;

    WorldPoint ClueAreaGloves = new WorldPoint(2579, 3378, 0);
    WorldPoint ClueAreaCloak = new WorldPoint(2614, 3064, 0);
    WorldPoint ClueAreaHelm = new WorldPoint(2590, 3231, 0);
    WorldPoint ClueAreaGarb = new WorldPoint(1595, 3628, 0);
    WorldPoint PortSarim = new WorldPoint(3055, 3245, 0);
    WorldPoint KourendBoat = new WorldPoint(1824, 3695, 1);
    WorldArea Kourend = new WorldArea(1385, 3398, 465, 417, 0);

    private State getState() {
        if (!Rs2Inventory.contains("Spade") && !Rs2Inventory.contains("Nature rune") && !Rs2Inventory.contains("leather boots") && !Rs2Inventory.contains("Superantipoison (1)"))
            return State.ERROR;
        if (!Rs2Inventory.contains("Clue hunter gloves") && !Rs2Inventory.contains("Clue hunter boots"))
            return State.CLUE1;
        if (!Rs2Inventory.contains("Clue hunter cloak"))
            return State.CLUE2;
        if (!Rs2Inventory.contains("Helm of raedwald"))
            return State.CLUE3;
        if (!Rs2Inventory.contains("Clue hunter garb") && !Kourend.contains(Microbot.getClient().getLocalPlayer().getWorldLocation()))
            return State.CLUE4;
        if (!Rs2Inventory.contains("Clue hunter garb") && Kourend.contains(Microbot.getClient().getLocalPlayer().getWorldLocation()))
            return State.CLUE5;
        return State.DONE;
    }

    public boolean run(ClueConfig config) {
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run()) return;
            try {
                if (Microbot.pauseAllScripts) return;

                switch (getState()) {
                    case ERROR:
                        System.out.println("Error - Make sure you have all the required items- Spade,Nature Runes, Leather Boots, SuperAntiPoison (1)");
                        break;
                    case CLUE1:
                        Microbot.status = "collecting Gloves and boots";
                        if (Microbot.getClient().getLocalPlayer().getWorldLocation().equals(ClueAreaGloves)) {
                            Rs2Inventory.interact("Spade", "Dig");
                            sleep(1000, 2000);
                        } else {
                            Rs2Walker.walkTo(ClueAreaGloves);
                        }
                        break;
                    case CLUE2:
                        Microbot.status = "collecting cloak";
                        if (Microbot.getClient().getLocalPlayer().getWorldLocation().equals(ClueAreaCloak)) {
                            Rs2Inventory.interact("Spade", "Dig");
                            sleep(1000, 2000);
                        } else {
                            Rs2Walker.walkTo(ClueAreaCloak);
                        }
                        break;
                    case CLUE3:
                        Microbot.status = "collecting Helm";
                        if (Microbot.getClient().getLocalPlayer().getWorldLocation().equals(ClueAreaHelm)) {
                            Rs2Inventory.interact("Spade", "Dig");
                            sleep(1000, 2000);
                        } else {
                            Rs2Walker.walkTo(ClueAreaHelm);
                        }
                        break;
                    case CLUE4:
                        if (Microbot.getClient().getLocalPlayer().getWorldLocation().equals(KourendBoat)) {
                            Microbot.status = "Crossing to Kourend";
                            Rs2GameObject.interact(27778, "Cross");
                            sleep(2500, 3500);
                        } else {
                            Microbot.status = "Walking to Port Sarim";
                            if (Microbot.getClient().getLocalPlayer().getWorldLocation().equals(PortSarim)) {
                                Rs2Npc.interact("Veos", "Port Piscarilius");
                                sleep(4000, 5000);
                            } else {
                                Rs2Walker.walkTo(PortSarim);
                            }
                        }
                        break;
                    case CLUE5:
                        Microbot.status = "collecting garb";
                        if (Microbot.getClient().getLocalPlayer().getWorldLocation().equals(ClueAreaGarb)) {
                            Rs2Inventory.interact("Spade", "Dig");
                            sleep(1000, 2000);
                        } else {
                            Rs2Walker.walkTo(ClueAreaGarb);
                        }
                        break;
                    case DONE:
                        Microbot.status = "Got the pieces all done";
                        System.out.println("Got 5 pieces of warm gear");
                        shutdown();
                        break;
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

}
