package net.runelite.client.plugins.microbot.mntn.MntnPker;

import net.runelite.api.HeadIcon;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.player.Rs2Pvp;
import net.runelite.client.plugins.microbot.util.reflection.Rs2Reflection;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;

import java.util.concurrent.TimeUnit;


public class MntnPkerScript extends Script {

    public static boolean test = false;
    public MntnPkerConfig config = null;
    public boolean run(MntnPkerConfig localConfig) {
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                config = localConfig;
                if(config == null) return;
                if(!isConfigValid()) return;
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();

                //CODE HERE

                if(!Rs2Pvp.isInWilderness()){
                    return;
                }

                handleHealth();

                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    /*
        Checks config for invalid or non-practical values.
        @returns Boolean
     */
    private boolean isConfigValid(){
        if(config.EatPercentage() >= 100 | config.EatPercentage() <= 0){
            Microbot.log("[ERROR] Invalid Eat % - should be between O and 1OO");
            return true;
        }
        return true;
    }

    /*
        If out of food, teleports (if runes available) or runs away.
     */
    private void handleEscape(){
        if(!config.ShouldEscape()){
            return;
        }

        // TODO: Check what wilderness level before teleport


        if(Rs2Magic.quickCanCast(MagicAction.VARROCK_TELEPORT)){
            Rs2Magic.quickCast(MagicAction.VARROCK_TELEPORT);
            return;
        }
        if(Rs2Magic.quickCanCast(MagicAction.LUMBRIDGE_TELEPORT)){
            Rs2Magic.quickCast(MagicAction.LUMBRIDGE_TELEPORT);
            return;
        }
        if(Rs2Magic.quickCanCast(MagicAction.FALADOR_TELEPORT)){
            Rs2Magic.quickCast(MagicAction.FALADOR_TELEPORT);
            return;
        }


    }

    /*
        Handles health and food.
     */
    private void handleHealth(){
        //TODO: Check if out of food
        if(Rs2Player.eatAt(config.EatPercentage())){
            Microbot.log("Eating.");
        } else {
            handleEscape();
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}