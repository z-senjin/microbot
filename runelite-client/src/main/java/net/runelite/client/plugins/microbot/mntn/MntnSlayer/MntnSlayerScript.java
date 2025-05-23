package net.runelite.client.plugins.microbot.mntn.MntnSlayer;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

public class MntnSlayerScript extends Script {
    private final MntnSlayerPlugin plugin;

    @Inject
    public MntnSlayerScript(MntnSlayerPlugin plugin){
        this.plugin = plugin;
    }

    public boolean run(MntnSlayerConfig config) {
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();

                plugin.loop();

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
}