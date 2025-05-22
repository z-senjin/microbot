package net.runelite.client.plugins.microbot.mntn.MntnSlayer;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Default + "Example",
        description = "Microbot example plugin",
        tags = {"example", "microbot"},
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


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(mntnSlayerOverlay);
        }
        mntnSlayerScript.run(config);
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
