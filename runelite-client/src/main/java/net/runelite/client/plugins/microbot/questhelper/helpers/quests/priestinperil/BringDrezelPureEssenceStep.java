package net.runelite.client.plugins.microbot.questhelper.helpers.quests.priestinperil;


import net.runelite.api.ItemID;
import net.runelite.api.NpcID;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.microbot.questhelper.questhelpers.QuestHelper;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.ItemRequirement;
import net.runelite.client.plugins.microbot.questhelper.steps.NpcStep;

import java.util.Collections;

public class BringDrezelPureEssenceStep extends NpcStep {
    ItemRequirement essence = new ItemRequirement("Rune/Pure essence", ItemID.RUNE_ESSENCE, 50);

    public BringDrezelPureEssenceStep(QuestHelper questHelper) {
        super(questHelper, NpcID.DREZEL, new WorldPoint(3439, 9896, 0), "Bring Drezel 50 UNNOTED rune/pure essence in the underground of the Salve Temple to finish the quest!");
        essence.addAlternates(ItemID.PURE_ESSENCE);
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        updateSteps();
    }

    protected void updateSteps() {
        int numEssence = 60 - client.getVarpValue(302);
        essence.setQuantity(numEssence);
        this.setRequirements(Collections.singletonList(essence));
        this.setText("Bring Drezel " + numEssence + " UNNOTED rune/pure essence in the underground of the Salve Temple.");
    }
}
