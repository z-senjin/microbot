package net.runelite.client.plugins.microbot.questhelper.helpers.quests.animalmagnetism;


import net.runelite.client.plugins.microbot.questhelper.steps.widget.WidgetDetails;

import java.util.Collections;
import java.util.HashSet;

public class PuzzleSolver {

    //Controls
    private static final WidgetDetails[] buttons = new WidgetDetails[]{
            new WidgetDetails(480, 26, 0),
            //Skip 2
            new WidgetDetails(480, 31, 0),
            new WidgetDetails(480, 34, 0),
            //Skip 5
            new WidgetDetails(480, 40, 0),
            new WidgetDetails(480, 43, 0),
            new WidgetDetails(480, 46, 0)
            //Skip 9
    };

    public PuzzleSolver() {
    }

    public HashSet<WidgetDetails> solver() {
        HashSet<WidgetDetails> highlights = new HashSet<>();

        Collections.addAll(highlights, buttons);

        return highlights;
    }
}
