package net.runelite.client.plugins.microbot.mixology;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import net.runelite.api.ItemID;

import java.util.Arrays;
import java.util.Map;

public enum PotionType {
    MAMMOTH_MIGHT_MIX(ItemID.MAMMOTHMIGHT_MIX, ItemID.MAMMOTHMIGHT_MIX_30021, 1900, new PotionComponent[]{PotionComponent.MOX, PotionComponent.MOX, PotionComponent.MOX}),
    MYSTIC_MANA_AMALGAM(ItemID.MYSTIC_MANA_AMALGAM, ItemID.MYSTIC_MANA_AMALGAM_30022, 2150, new PotionComponent[]{PotionComponent.MOX, PotionComponent.MOX, PotionComponent.AGA}),
    MARLEYS_MOONLIGHT(ItemID.MARLEYS_MOONLIGHT, ItemID.MARLEYS_MOONLIGHT_30023, 2400, new PotionComponent[]{PotionComponent.MOX, PotionComponent.MOX, PotionComponent.LYE}),
    ALCO_AUGMENTATOR(ItemID.ALCOAUGMENTATOR, ItemID.ALCOAUGMENTATOR_30024, 1900, new PotionComponent[]{PotionComponent.AGA, PotionComponent.AGA, PotionComponent.AGA}),
    AZURE_AURA_MIX(ItemID.AZURE_AURA_MIX, ItemID.AZURE_AURA_MIX_30026, 2650, new PotionComponent[]{PotionComponent.AGA, PotionComponent.AGA, PotionComponent.MOX}),
    AQUALUX_AMALGAM(ItemID.AQUALUX_AMALGAM, ItemID.AQUALUX_AMALGAM_30025, 2900, new PotionComponent[]{PotionComponent.AGA, PotionComponent.LYE, PotionComponent.AGA}),
    LIPLACK_LIQUOR(ItemID.LIPLACK_LIQUOR, ItemID.LIPLACK_LIQUOR_30027, 1900, new PotionComponent[]{PotionComponent.LYE, PotionComponent.LYE, PotionComponent.LYE}),
    MEGALITE_LIQUID(ItemID.MEGALITE_LIQUID, ItemID.MEGALITE_LIQUID_30029, 3150, new PotionComponent[]{PotionComponent.MOX, PotionComponent.LYE, PotionComponent.LYE}),
    ANTI_LEECH_LOTION(ItemID.ANTILEECH_LOTION, ItemID.ANTILEECH_LOTION_30028, 3400, new PotionComponent[]{PotionComponent.AGA, PotionComponent.LYE, PotionComponent.LYE}),
    MIXALOT(ItemID.MIXALOT, ItemID.MIXALOT_30030, 3650, new PotionComponent[]{PotionComponent.MOX, PotionComponent.AGA, PotionComponent.LYE});

    public static final PotionType[] TYPES = values();
    private static final Map<Integer, PotionType> ITEM_MAP;
    private final int itemId;
    @Getter
    private final int fulfilledItemId;
    private final String recipe;
    private final String abbreviation;
    private final int experience;
    private final PotionComponent[] components;

    PotionType(int itemId, int fulfilledItemId, int experience, PotionComponent... components) {
        this.itemId = itemId;
        this.fulfilledItemId = fulfilledItemId;
        this.recipe = colorizeRecipe(components);
        this.experience = experience;
        this.components = components;
        this.abbreviation = "" + components[0].character() + components[1].character() + components[2].character();
    }

    public static PotionType fromItemId(int itemId) {
        return (PotionType)ITEM_MAP.get(itemId);
    }

    public static PotionType fromIdx(int potionTypeId) {
        return potionTypeId >= 0 && potionTypeId < TYPES.length ? TYPES[potionTypeId] : null;
    }

    private static String colorizeRecipe(PotionComponent[] components) {
        if (components.length != 3) {
            throw new IllegalArgumentException("Invalid potion components: " + Arrays.toString(components));
        } else {
            String var10000 = colorizeRecipeComponent(components[0]);
            return var10000 + colorizeRecipeComponent(components[1]) + colorizeRecipeComponent(components[2]);
        }
    }

    private static String colorizeRecipeComponent(PotionComponent component) {
        return "<col=" +  component.color() + ">" + component.character() + "</col>";
    }

    public int itemId() {
        return this.itemId;
    }

    public String recipe() {
        return this.recipe;
    }
    public int experience() {
        return this.experience;
    }

    public PotionComponent[] components() {
        return this.components;
    }

    public String abbreviation() {
        return this.abbreviation;
    }

    static {
        ImmutableMap.Builder<Integer, PotionType> builder = new ImmutableMap.Builder();
        for (PotionType potionType: values()) {
            builder.put(potionType.itemId(), potionType);
        }

        ITEM_MAP = builder.build();
    }
}

