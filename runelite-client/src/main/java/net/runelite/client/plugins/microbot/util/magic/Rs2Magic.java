package net.runelite.client.plugins.microbot.util.magic;

import net.runelite.api.Point;
import net.runelite.api.*;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.globval.enums.InterfaceTab;
import net.runelite.client.plugins.microbot.util.camera.Rs2Camera;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Item;
import net.runelite.client.plugins.microbot.util.math.Random;
import net.runelite.client.plugins.microbot.util.menu.NewMenuEntry;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.settings.Rs2SpellBookSettings;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;
import org.apache.commons.lang3.NotImplementedException;

import java.awt.*;
import java.util.Arrays;
import java.util.Optional;

import static net.runelite.api.Varbits.SHADOW_VEIL;
import static net.runelite.client.plugins.microbot.Microbot.log;
import static net.runelite.client.plugins.microbot.util.Global.*;

public class Rs2Magic {
    public static boolean canCast(MagicAction magicSpell) {
        if (!Rs2SpellBookSettings.setAllFiltersOn()) {
            return false;
        }
        if (Rs2Tab.getCurrentTab() != InterfaceTab.MAGIC) {
            Rs2Tab.switchToMagicTab();
            sleep(150, 300);
        }

        if (magicSpell.getName().toLowerCase().contains("enchant")){
            if (Rs2Widget.clickWidget("Jewellery Enchantments", Optional.of(218), 3, true)) {
                sleepUntil(() -> Rs2Widget.hasWidgetText("Jewellery Enchantments", 218, 3, true), 2000);
            }
        } else if (!Rs2Widget.isHidden(14286852)) {
            // back button inside the enchant jewellery interface has no text, thats why we use hardcoded id
            Rs2Widget.clickWidget(14286852);
        }

        Widget widget = Arrays.stream(Rs2Widget.getWidget(218, 3).getStaticChildren()).filter(x -> x.getSpriteId() == magicSpell.getSprite()).findFirst().orElse(null);

        return widget != null;
    }

    public static boolean cast(MagicAction magicSpell) {
        MenuAction menuAction;
        Rs2Tab.switchToMagicTab();
        Microbot.status = "Casting " + magicSpell.getName();
        sleep(150, 300);
        if (!canCast(magicSpell)) {
            log("Unable to cast " + magicSpell.getName());
            return false;
        }
        int identifier = 1;
        if (magicSpell.getName().toLowerCase().contains("teleport") || magicSpell.getName().toLowerCase().contains("Bones to") || Arrays.stream(magicSpell.getActions()).anyMatch(x -> x != null && x.equalsIgnoreCase("cast"))) {
            menuAction = MenuAction.CC_OP;
        } else {
            menuAction = MenuAction.WIDGET_TARGET;
        }

        if (magicSpell.getWidgetId() == -1)
            throw new NotImplementedException("This spell has not been configured yet in the MagicAction.java class");

        Microbot.doInvoke(new NewMenuEntry("cast", -1, magicSpell.getWidgetId(), menuAction.getId(), identifier, -1, magicSpell.getName()), new Rectangle(Rs2Widget.getWidget(magicSpell.getWidgetId()).getBounds()));
        //Rs2Reflection.invokeMenu(-1, magicSpell.getWidgetId(), menuAction.getId(), 1, -1, "Cast", "<col=00ff00>" + magicSpell.getName() + "</col>", -1, -1);
        return true;
    }

    public static void castOn(MagicAction magicSpell, Actor actor) {
        if (actor == null) return;
        cast(magicSpell);
        sleep(150, 300);
        if (!Rs2Camera.isTileOnScreen(actor.getLocalLocation())) {
            Rs2Camera.turnTo(actor.getLocalLocation());
            return;
        }
        if (actor instanceof NPC) {
            Rs2Npc.interact((NPC) actor);
        } else {
            Point point = Perspective.localToCanvas(Microbot.getClient(), actor.getLocalLocation(), Microbot.getClient().getPlane());
            Microbot.getMouse().click(point);
        }
    }

    public static void alch(String itemName, int sleepMin, int sleepMax) {
        Rs2Item item = Rs2Inventory.get(itemName);
        if (Microbot.getClient().getRealSkillLevel(Skill.MAGIC) >= 55) {
            highAlch(item, sleepMin, sleepMax);
        } else {
            lowAlch(item, sleepMin, sleepMax);
        }
    }

    public static void alch(String itemName) {
        Rs2Item item = Rs2Inventory.get(itemName);
        if (Microbot.getClient().getRealSkillLevel(Skill.MAGIC) >= 55) {
            highAlch(item, 300, 600);
        } else {
            lowAlch(item, 300, 600);
        }
    }

    /**
     * alch item with minsleep of 300 and maxsleep of 600
     *
     * @param item
     */
    public static void alch(Rs2Item item) {
        alch(item, 300, 600);
    }

    /**
     * @param item
     * @param sleepMin
     * @param sleepMax
     */
    public static void alch(Rs2Item item, int sleepMin, int sleepMax) {
        if (Microbot.getClient().getRealSkillLevel(Skill.MAGIC) >= 55) {
            highAlch(item, sleepMin, sleepMax);
        } else {
            lowAlch(item, sleepMin, sleepMax);
        }
    }

    public static void superHeat(String itemName) {
        Rs2Item item = Rs2Inventory.get(itemName);
        superHeat(item, 300, 600);
    }

    public static void superHeat(String itemName, int sleepMin, int sleepMax) {
        Rs2Item item = Rs2Inventory.get(itemName);
        superHeat(item, sleepMin, sleepMax);
    }

    public static void superHeat(int id) {
        Rs2Item item = Rs2Inventory.get(id);
        superHeat(item, 300, 600);
    }

    public static void superHeat(int id, int sleepMin, int sleepMax) {
        Rs2Item item = Rs2Inventory.get(id);
        superHeat(item, sleepMin, sleepMax);
    }

    public static void superHeat(Rs2Item item) {
        superHeat(item, 300, 600);
    }

    public static void superHeat(Rs2Item item, int sleepMin, int sleepMax) {
        sleepUntil(() -> {
            Rs2Tab.switchToMagicTab();
            sleep(50, 150);
            return Rs2Tab.getCurrentTab() == InterfaceTab.MAGIC;
        });
        if (Rs2Widget.isWidgetVisible(218, 4) && Arrays.stream(Rs2Widget.getWidget(218, 4).getActions()).anyMatch(x -> x.equalsIgnoreCase("back"))){
            Rs2Widget.clickWidget(218, 4);
            sleep(150, 300);
        }
        Widget superHeat = Rs2Widget.findWidget(MagicAction.SUPERHEAT_ITEM.getName());
        if (superHeat.getSpriteId() != SpriteID.SPELL_SUPERHEAT_ITEM) return;
        superHeat(superHeat, item, sleepMin, sleepMax);
    }

    private static void highAlch(Rs2Item item, int sleepMin, int sleepMax) {
        sleepUntil(() -> {
            Rs2Tab.switchToMagicTab();
            sleep(50, 150);
            return Rs2Tab.getCurrentTab() == InterfaceTab.MAGIC;
        });
        if (Rs2Widget.isWidgetVisible(218, 4) && Arrays.stream(Rs2Widget.getWidget(218, 4).getActions()).anyMatch(x -> x.equalsIgnoreCase("back"))){
            Rs2Widget.clickWidget(218, 4);
            sleep(150, 300);
        }
        Widget highAlch = Rs2Widget.findWidget(MagicAction.HIGH_LEVEL_ALCHEMY.getName());
        if (highAlch.getSpriteId() != 41) return;
        alch(highAlch, item, sleepMin, sleepMax);
    }

    private static void lowAlch(Rs2Item item, int sleepMin, int sleepMax) {
        sleepUntil(() -> {
            Rs2Tab.switchToMagicTab();
            sleep(50, 150);
            return Rs2Tab.getCurrentTab() == InterfaceTab.MAGIC;
        });
        if (Rs2Widget.isWidgetVisible(218, 4) && Arrays.stream(Rs2Widget.getWidget(218, 4).getActions()).anyMatch(x -> x.equalsIgnoreCase("back"))){
            Rs2Widget.clickWidget(218, 4);
            sleep(150, 300);
        }
        Widget lowAlch = Rs2Widget.findWidget(MagicAction.LOW_LEVEL_ALCHEMY.getName());
        if (lowAlch.getSpriteId() != 25) return;
        alch(lowAlch, item, sleepMin, sleepMax);
    }

    private static void alch(Widget alch, Rs2Item item, int sleepMin, int sleepMax) {
        if (alch == null) return;
        Point point = new Point((int) alch.getBounds().getCenterX(), (int) alch.getBounds().getCenterY());
        sleepUntil(() -> Microbot.getClientThread().runOnClientThread(() -> Rs2Tab.getCurrentTab() == InterfaceTab.MAGIC), 5000);
        sleep(sleepMin, sleepMax);
        Microbot.getMouse().click(point);
        sleepUntil(() -> Microbot.getClientThread().runOnClientThread(() -> Rs2Tab.getCurrentTab() == InterfaceTab.INVENTORY), 5000);
        sleep(sleepMin, sleepMax);
        if (item == null) {
            Microbot.status = "Alching x: " + point.getX() + " y: " + point.getY();
            Microbot.getMouse().click(point);
        } else {
            Microbot.status = "Alching " + item.name;
            Rs2Inventory.interact(item, "cast");
        }
    }

    private static void superHeat(Widget superheat, Rs2Item item, int sleepMin, int sleepMax) {
        if (superheat == null) return;
        Point point = new Point((int) superheat.getBounds().getCenterX(), (int) superheat.getBounds().getCenterY());
        sleepUntil(() -> Microbot.getClientThread().runOnClientThread(() -> Rs2Tab.getCurrentTab() == InterfaceTab.MAGIC), 5000);
        sleep(sleepMin, sleepMax);
        Microbot.getMouse().click(point);
        sleepUntil(() -> Microbot.getClientThread().runOnClientThread(() -> Rs2Tab.getCurrentTab() == InterfaceTab.INVENTORY), 5000);
        sleep(sleepMin, sleepMax);
        if (item == null) {
            Microbot.status = "Superheating x: " + point.getX() + " y: " + point.getY();
            Microbot.getMouse().click(point);
        } else {
            Microbot.status = "Superheating " + item.name;
            Rs2Inventory.interact(item, "cast");
        }
    }

    // humidify
    public static void humidify() {
        sleepUntil(() -> {
            Rs2Tab.switchToMagicTab();
            sleep(50, 150);
            return Rs2Tab.getCurrentTab() == InterfaceTab.MAGIC;
        });
        Widget humidify = Rs2Widget.findWidget(MagicAction.HUMIDIFY.getName());
        if (humidify.getSpriteId() == 1972) {
            Microbot.click(humidify.getBounds());
        }
    }

    public static boolean npcContact(String npcName) {
        if (!isLunar()) {
            Microbot.log("Tried casting npcContact, but lunar spellbook was not found.");
            return false;
        }
        final int chooseCharacterWidgetId = 4915200;
        boolean didCast = cast(MagicAction.NPC_CONTACT);
        if (!didCast) return false;
        boolean result = sleepUntilTrue(() -> Rs2Widget.getWidget(chooseCharacterWidgetId) != null && !Rs2Widget.isHidden(chooseCharacterWidgetId), 100, 5000);
        if (!result) return false;
        boolean clickResult = Rs2Widget.clickWidget(npcName, Optional.of(75), 0, false);
        if (!clickResult) return false;
        Rs2Player.waitForAnimation();
        return true;
    }

    public static boolean repairPouchesWithLunar() {
        log("Repairing pouches...");
        if (npcContact("dark mage")) {
            sleep(Random.randomGaussian(Random.random(600, 1200), 300));
            Rs2Dialogue.clickContinue();
            sleep(Random.randomGaussian(Random.random(1000, 2200), 300));
            Rs2Widget.sleepUntilHasWidget("Can you repair my pouches?");
            sleep(Random.randomGaussian(Random.random(600, 1200), 300));
            Rs2Widget.clickWidget("Can you repair my pouches?", Optional.of(162), 0, true);
            sleep(Random.randomGaussian(Random.random(1000, 2200), 300));
            Rs2Dialogue.clickContinue();
            sleep(Random.randomGaussian(1500, 300));
            Rs2Tab.switchToInventoryTab();
        }
        return !Rs2Inventory.hasDegradedPouch();
    }

    private static void alch(Widget alch) {
        alch(alch, null, 300, 600);
    }

    private static void superHeat(Widget superHeat) {
        superHeat(superHeat, null, 300, 600);
    }

    public static boolean isLunar() {
        return Microbot.getVarbitValue(Varbits.SPELLBOOK) == 2;
    }

    public static boolean isAncient() {
        return Microbot.getVarbitValue(Varbits.SPELLBOOK) == 1;
    }

    public static boolean isModern() {
        return Microbot.getVarbitValue(Varbits.SPELLBOOK) == 0;
    }

    public static boolean isArceeus() {
        return Microbot.getVarbitValue(Varbits.SPELLBOOK) == 3;
    }

    public static boolean isShadowVeilActive() {
        return Microbot.getVarbitValue(SHADOW_VEIL) == 1;
    }
}