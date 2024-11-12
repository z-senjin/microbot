package net.runelite.client.plugins.microbot.util.inventory;

import net.runelite.api.Varbits;
import net.runelite.api.annotations.Varbit;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.magic.Runes;

import java.util.Arrays;

/**
 * Use this class to check your rune pouch
 */
public class RunePouch {

    private static final int NUM_SLOTS = 6;
    private static final int[] AMOUNT_VARBITS = {
            Varbits.RUNE_POUCH_AMOUNT1, Varbits.RUNE_POUCH_AMOUNT2, Varbits.RUNE_POUCH_AMOUNT3, Varbits.RUNE_POUCH_AMOUNT4,
            Varbits.RUNE_POUCH_AMOUNT5, Varbits.RUNE_POUCH_AMOUNT6
    };
    private static final int[] RUNE_VARBITS = {
            Varbits.RUNE_POUCH_RUNE1, Varbits.RUNE_POUCH_RUNE2, Varbits.RUNE_POUCH_RUNE3, Varbits.RUNE_POUCH_RUNE4,
            Varbits.RUNE_POUCH_RUNE5, Varbits.RUNE_POUCH_RUNE6
    };

    private static final int[] runeIds = new int[NUM_SLOTS];
    private static final int[] amounts = new int[NUM_SLOTS];

    /**
     *
     */
    private static void load() {
        for (int i = 0; i < NUM_SLOTS; i++)
        {
            @Varbit int amountVarbit = AMOUNT_VARBITS[i];
            int amount = Microbot.getClient().getVarbitValue(amountVarbit);
            amounts[i] = amount;

            @Varbit int runeVarbit = RUNE_VARBITS[i];
            int runeId = Microbot.getClient().getVarbitValue(runeVarbit);
            runeIds[i] = runeId;
        }
    }

    /**
     *
     * @param itemId
     * @return
     */
    public static boolean contains(int itemId) {
        return Microbot.getClientThread().runOnClientThread(() -> {
            load();
            for (int runeId: runeIds) {
                Runes rune = Arrays.stream(Runes.values()).filter(x -> x.getId() == runeId).findFirst().orElse(null);

                if (rune == null) continue;

                if (rune.getItemId() == itemId)
                    return true;
            }
            return false;
        });
    }

    /**
     *
     * @param itemId
     * @param amt
     * @return
     */
    public static boolean contains(int itemId, int amt) {
        return Microbot.getClientThread().runOnClientThread(() -> {
            load();
            for (int i = 0; i < runeIds.length; i++) {
                final int _i = i;
                Runes rune = Arrays.stream(Runes.values()).filter(x -> x.getId() == runeIds[_i]).findFirst().orElse(null);

                if (rune == null) continue;

                if (rune.getItemId() == itemId && amounts[_i] >= amt)
                    return true;
            }
            return false;
        });
    }
}
