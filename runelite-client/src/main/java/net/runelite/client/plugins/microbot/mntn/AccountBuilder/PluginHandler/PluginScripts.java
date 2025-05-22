package net.runelite.client.plugins.microbot.mntn.AccountBuilder.PluginHandler;

public enum PluginScripts {
    TUTORIAL_ISLAND("TutorialIsland");
//    QUEST_HELPER("QuestHelper")

    private final String scriptName;

    PluginScripts(String scriptName) {
        this.scriptName = scriptName;
    }

    public String getScriptName() {
        return scriptName;
    }
}
