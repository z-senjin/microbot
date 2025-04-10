package net.runelite.client.plugins.microbot.magic.aiomagic.scripts;

import net.runelite.api.Skill;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.magic.aiomagic.AIOMagicPlugin;
import net.runelite.client.plugins.microbot.magic.aiomagic.enums.MagicState;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.antiban.enums.Activity;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

public class TeleAlchScript extends Script {

    private MagicState state = MagicState.CASTING;
    private final AIOMagicPlugin plugin;

    @Inject
    public TeleAlchScript(AIOMagicPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean run() {
        Microbot.enableAutoRunOn = false;
        Rs2Antiban.resetAntibanSettings();
        Rs2Antiban.antibanSetupTemplates.applyGeneralBasicSetup();
        Rs2AntibanSettings.simulateAttentionSpan = true;
        Rs2AntibanSettings.nonLinearIntervals = true;
        Rs2AntibanSettings.contextualVariability = true;
        Rs2AntibanSettings.usePlayStyle = true;
        Rs2Antiban.setActivity(Activity.TELEPORT_TRAINING);
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();


                switch (state) {
                    case CASTING:
                        if (plugin.getAlchItemNames().isEmpty() || plugin.getAlchItemNames().stream().noneMatch(Rs2Inventory::hasItem)) {
                            Microbot.log("Missing alch items...");
                            return;
                        }

                        if (!Rs2Magic.hasRequiredRunes(plugin.getAlchSpell())) {
                            Microbot.log("Unable to cast alchemy spell");
                            return;
                        }

                        Rs2ItemModel alchItem = plugin.getAlchItemNames().stream()
                                .filter(Rs2Inventory::hasItem)
                                .map(Rs2Inventory::get)
                                .findFirst()
                                .orElse(null);
                        
                        if (alchItem == null) {
                            Microbot.log("Missing alch items...");
                            return;
                        }
                        
                        if (Rs2AntibanSettings.naturalMouse) {
                            int inventorySlot = Rs2Player.getSkillRequirement(Skill.MAGIC, 55) ? 11 : 4;
                            if (alchItem.getSlot() != inventorySlot) {
                                Rs2Inventory.moveItemToSlot(alchItem, inventorySlot);
                                return;
                            }
                        }

                        Rs2Magic.alch(alchItem, 100, 150);

                        Rs2Magic.cast(plugin.getTeleportSpell().getRs2Spell().getAction());

                        sleep(900, 950);

                        break;
                }

                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        Rs2Antiban.resetAntibanSettings();
        super.shutdown();
    }
}
