package net.runelite.client.plugins.microbot.mntn.AccountBuilder;

import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.mntn.AccountBuilder.PluginHandler.PluginScripts;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;


public class AccountBuilderScript extends Script {

    public static boolean test = false;

    private AccountBuilderPlugin builder;

    @Inject
    public AccountBuilderScript(AccountBuilderPlugin builder) {
        this.builder = builder;
    }

    public boolean run(AccountBuilderConfig config) {

        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();

                builder.activityHandler.checkStatus();

                Microbot.status = String.valueOf(builder.activityHandler.currentActivity);




                switch(builder.activityHandler.currentActivity){
                    case QUEST:
                        break;
                    case MINIGAME:
                        break;
                    case TUTORIAL_ISLAND:
                        handleTutorialIsland();
                        break;
                    case SKILL:
                        handleSkilling();
                        break;
                    case MONEY_MAKER:
                        break;
                    case CHILL:
                        break;
                }


                //TODO:
                // 1) Look at QuestHelper to fix bugs
                // 2) Utilize GE buyer to get all items from bank
                // 3) Create a dynamic banking function to get quest items, gear, etc


                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    private void stopExecution(){
        System.out.println("STOPPING EXECUTION!");
        builder.shutDown();
    }


    public void handleTutorialIsland()  {
        var tutorialIslandPlugin = builder.pluginHandler.findPluginByName(PluginScripts.TUTORIAL_ISLAND.getScriptName());
        if(tutorialIslandPlugin == null){
            System.err.println("Can't find tutorial island plugin!");
            stopExecution();
        }
        // check if tutorial island is enabled
        // if not, run it
        for (Plugin activePlugin : Microbot.getPluginManager().getPlugins()) {
    var pluginName = activePlugin.getName();
    if(pluginName.contains("Tutorial")){
        Microbot.log(pluginName);
        if(Microbot.getPluginManager().isPluginEnabled(activePlugin)){
            return;
        } else {
            System.out.println("Starting tutorial island plugin.");

            builder.pluginHandler.setCurrentPlugin(activePlugin);
            Microbot.startPlugin(activePlugin);
            return;
            // add this to plugin manager
            // set current active plugin so loop can check if its running and return if so.
        }
    }
}
    }

    public void handleSkilling(){

    }
    
    @Override
    public void shutdown() {
        super.shutdown();
    }
}