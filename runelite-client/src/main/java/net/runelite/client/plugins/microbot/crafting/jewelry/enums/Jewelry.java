package net.runelite.client.plugins.microbot.crafting.jewelry.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ItemID;

import java.lang.reflect.Array;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum Jewelry {

    GOLD_RING("gold ring", ItemID.GOLD_RING, null, ItemID.RING_MOULD, JewelryType.GOLD, null, 5),
    GOLD_NECKLACE("gold necklace", ItemID.GOLD_NECKLACE, null, ItemID.NECKLACE_MOULD, JewelryType.GOLD, null, 6),
    GOLD_BRACELET("gold bracelet", ItemID.GOLD_BRACELET, null, ItemID.BRACELET_MOULD, JewelryType.GOLD, null, 7),
    OPAL_RING("opal ring", ItemID.OPAL_RING, Gem.OPAL, ItemID.RING_MOULD, JewelryType.SILVER, EnchantSpell.LEVEL_1, 1),
    OPAL_NECKLACE("opal necklace", ItemID.OPAL_NECKLACE, Gem.OPAL, ItemID.NECKLACE_MOULD, JewelryType.SILVER, EnchantSpell.LEVEL_1, 16),
    OPAL_BRACELET("opal bracelet", ItemID.OPAL_BRACELET, Gem.OPAL, ItemID.BRACELET_MOULD, JewelryType.SILVER, EnchantSpell.LEVEL_1, 22),
    JADE_RING("jade ring", ItemID.JADE_RING, Gem.JADE, ItemID.RING_MOULD, JewelryType.SILVER, EnchantSpell.LEVEL_2, 13),
    JADE_NECKLACE("jade necklace", ItemID.JADE_NECKLACE, Gem.JADE, ItemID.NECKLACE_MOULD, JewelryType.SILVER, EnchantSpell.LEVEL_2, 25),
    JADE_BRACELET("jade bracelet", ItemID.JADE_BRACELET, Gem.JADE, ItemID.BRACELET_MOULD, JewelryType.SILVER, EnchantSpell.LEVEL_2, 29),
    TOPAZ_RING("topaz ring", ItemID.TOPAZ_RING, Gem.RED_TOPAZ, ItemID.RING_MOULD, JewelryType.SILVER, EnchantSpell.LEVEL_3, 16),
    TOPAZ_NECKLACE("topaz necklace", ItemID.TOPAZ_NECKLACE, Gem.RED_TOPAZ, ItemID.NECKLACE_MOULD, JewelryType.SILVER, EnchantSpell.LEVEL_3, 32),
    TOPAZ_BRACELET("topaz bracelet", ItemID.TOPAZ_BRACELET, Gem.RED_TOPAZ, ItemID.BRACELET_MOULD, JewelryType.SILVER, EnchantSpell.LEVEL_3, 38),
    SAPPHIRE_RING("sapphire ring", ItemID.SAPPHIRE_RING, Gem.SAPPHIRE, ItemID.RING_MOULD, JewelryType.GOLD, EnchantSpell.LEVEL_1, 20),
    SAPPHIRE_NECKLACE("sapphire necklace", ItemID.SAPPHIRE_NECKLACE, Gem.SAPPHIRE, ItemID.NECKLACE_MOULD, JewelryType.GOLD, EnchantSpell.LEVEL_1, 22),
    SAPPHIRE_BRACELET("sapphire bracelet", ItemID.SAPPHIRE_BRACELET_11072, Gem.SAPPHIRE, ItemID.BRACELET_MOULD, JewelryType.GOLD, EnchantSpell.LEVEL_1, 23),
    EMERALD_RING("emerald ring", ItemID.EMERALD_RING, Gem.EMERALD, ItemID.RING_MOULD, JewelryType.GOLD, EnchantSpell.LEVEL_2, 27),
    EMERALD_NECKLACE("emerald necklace", ItemID.EMERALD_NECKLACE, Gem.EMERALD, ItemID.NECKLACE_MOULD, JewelryType.GOLD, EnchantSpell.LEVEL_2, 29),
    EMERALD_BRACELET("emerald bracelet", ItemID.EMERALD_BRACELET, Gem.EMERALD, ItemID.BRACELET_MOULD, JewelryType.GOLD, EnchantSpell.LEVEL_2, 30),
    RUBY_RING("ruby ring", ItemID.RUBY_RING, Gem.RUBY, ItemID.RING_MOULD, JewelryType.GOLD, EnchantSpell.LEVEL_3, 34),
    RUBY_NECKLACE("ruby necklace", ItemID.RUBY_NECKLACE, Gem.RUBY, ItemID.NECKLACE_MOULD, JewelryType.GOLD, EnchantSpell.LEVEL_3, 40),
    RUBY_BRACELET("ruby bracelet", ItemID.RUBY_BRACELET, Gem.RUBY, ItemID.BRACELET_MOULD, JewelryType.GOLD, EnchantSpell.LEVEL_3, 42),
    DIAMOND_RING("diamond ring", ItemID.DIAMOND_RING, Gem.DIAMOND, ItemID.RING_MOULD, JewelryType.GOLD, EnchantSpell.LEVEL_4, 43),
    DIAMOND_NECKLACE("diamond necklace", ItemID.DIAMOND_NECKLACE, Gem.DIAMOND, ItemID.NECKLACE_MOULD, JewelryType.GOLD, EnchantSpell.LEVEL_4, 56),
    DIAMOND_BRACELET("diamond bracelet", ItemID.DIAMOND_BRACELET, Gem.DIAMOND, ItemID.BRACELET_MOULD, JewelryType.GOLD, EnchantSpell.LEVEL_4, 58),
    DRAGONSTONE_RING("dragonstone ring", ItemID.DRAGONSTONE_RING, Gem.DRAGONSTONE, ItemID.RING_MOULD, JewelryType.GOLD, EnchantSpell.LEVEL_5, 55),
    DRAGON_NECKLACE("dragon necklace", ItemID.DRAGON_NECKLACE, Gem.DRAGONSTONE, ItemID.NECKLACE_MOULD, JewelryType.GOLD, EnchantSpell.LEVEL_5, 72),
    DRAGONSTONE_BRACELET("dragonstone bracelet", ItemID.DRAGONSTONE_BRACELET, Gem.DRAGONSTONE, ItemID.BRACELET_MOULD, JewelryType.GOLD, EnchantSpell.LEVEL_5, 74),
    ONYX_RING("onyx ring", ItemID.ONYX_RING, Gem.ONYX, ItemID.RING_MOULD, JewelryType.GOLD, EnchantSpell.LEVEL_6, 67),
    ONYX_NECKLACE("onyx necklace", ItemID.ONYX_NECKLACE, Gem.ONYX, ItemID.NECKLACE_MOULD, JewelryType.GOLD, EnchantSpell.LEVEL_6, 82),
    ONYX_BRACELET("onyx bracelet", ItemID.ONYX_BRACELET, Gem.ONYX, ItemID.BRACELET_MOULD, JewelryType.GOLD, EnchantSpell.LEVEL_6, 84),
    ZENYTE_RING("zenyte ring", ItemID.ZENYTE_RING, Gem.ZENYTE, ItemID.RING_MOULD, JewelryType.GOLD, EnchantSpell.LEVEL_7, 89),
    ZENYTE_NECKLACE("zenyte necklace", ItemID.ZENYTE_NECKLACE, Gem.ZENYTE, ItemID.NECKLACE_MOULD, JewelryType.GOLD, EnchantSpell.LEVEL_7, 92),
    ZENYTE_BRACELET("zenyte bracelet", ItemID.ZENYTE_BRACELET, Gem.ZENYTE, ItemID.BRACELET_MOULD, JewelryType.GOLD, EnchantSpell.LEVEL_7, 95);

    private final String itemName;
    private final int itemID;
    private final Gem gem;
    private final int toolItemID;
    private final JewelryType jewelryType;
    private final EnchantSpell enchantSpell;
    private final int levelRequired;
}
