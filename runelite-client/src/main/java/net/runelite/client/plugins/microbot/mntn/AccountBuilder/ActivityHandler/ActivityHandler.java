package net.runelite.client.plugins.microbot.mntn.AccountBuilder.ActivityHandler;


import net.runelite.api.Skill;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

public class ActivityHandler {

    public Activity currentActivity = null;

    public Skill currentSkill = null;
    private int currentSkillLevel = 1;
    private int currentSkillExp = 1;

    private final int CharacterCreationWidgetID = 679;
    private final int NameCreationWidgetID = 558;

    public ActivityHandler(){
        }

    public void checkStatus(){
        // Check if on tutorial island
        // Set to tutorial island if so
        if(isNameCreationVisible() || isCharacterCreationVisible() || Microbot.getVarbitPlayerValue(281) != 1000){
            currentActivity = Activity.TUTORIAL_ISLAND;
            return;
        }

        // Check config for rest - skill / quest / etc
    }


    /*
    Tutorial Island Helpers
     */
    private boolean isNameCreationVisible() {
        return Rs2Widget.isWidgetVisible(NameCreationWidgetID, 2);
    }

    private boolean isCharacterCreationVisible() {
        return Rs2Widget.isWidgetVisible(CharacterCreationWidgetID, 4);
    }

}
