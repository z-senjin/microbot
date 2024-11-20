package net.runelite.client.plugins.microbot.birdhouseruns;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;


@PluginDescriptor(
        name = PluginDescriptor.Forn + "Birdhouse Runner",
        description = "Does a birdhouse run",
        tags = {"FornBirdhouseRuns", "forn"},
        enabledByDefault = false
)
@Slf4j
public class FornBirdhouseRunsPlugin extends Plugin {
    @Inject
    private FornBirdhouseRunsConfig config;
    @Provides
    FornBirdhouseRunsConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(FornBirdhouseRunsConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private PluginManager pluginManager;
    @Inject
    private FornBirdhouseRunsOverlay fornBirdhouseRunsOverlay;

    @Inject
    FornBirdhouseRunsScript fornBirdhouseRunsScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(fornBirdhouseRunsOverlay);
        }
        fornBirdhouseRunsScript.run(config);
//        botStatus = config.STEP(); for debugging
        FornBirdhouseRunsInfo.botStatus = FornBirdhouseRunsInfo.states.GEARING;
        FornBirdhouseRunsInfo.selectedSeed = config.SEED().getItemId();
        FornBirdhouseRunsInfo.seedAmount = config.SEED().getAmountPerHouse();
        FornBirdhouseRunsInfo.selectedLogs = config.LOG().getItemId();
        FornBirdhouseRunsInfo.birdhouseType = config.LOG().getBirdhouseType();
    }

    protected void shutDown() {
        fornBirdhouseRunsScript.shutdown();
        overlayManager.remove(fornBirdhouseRunsOverlay);
    }
}
