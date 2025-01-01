package net.runelite.client.plugins.microbot.util.magic;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;

import java.util.Map;

@Getter
public enum Rs2CombatSpells {
    WIND_STRIKE(MagicAction.WIND_STRIKE, Map.of(
            Runes.AIR, 1,
            Runes.MIND, 1
    )),
    WATER_STRIKE(MagicAction.WATER_STRIKE, Map.of(
            Runes.WATER, 1,
            Runes.AIR, 1,
            Runes.MIND, 1
    )),
    EARTH_STRIKE(MagicAction.EARTH_STRIKE, Map.of(
            Runes.EARTH, 2,
            Runes.AIR, 1,
            Runes.MIND, 1
    )),
    FIRE_STRIKE(MagicAction.FIRE_STRIKE, Map.of(
            Runes.FIRE, 3,
            Runes.AIR, 2,
            Runes.MIND, 1
    )),
    WIND_BOLT(MagicAction.WIND_BOLT, Map.of(
            Runes.AIR, 2,
            Runes.CHAOS, 1
    )),
    WATER_BOLT(MagicAction.WATER_BOLT, Map.of(
            Runes.WATER, 2,
            Runes.AIR, 2,
            Runes.CHAOS, 1
    )),
    EARTH_BOLT(MagicAction.EARTH_BOLT, Map.of(
            Runes.EARTH, 3,
            Runes.AIR, 2,
            Runes.CHAOS, 1
    )),
    FIRE_BOLT(MagicAction.FIRE_BOLT, Map.of(
            Runes.FIRE, 4,
            Runes.AIR, 3,
            Runes.CHAOS, 1
    )),
    WIND_BLAST(MagicAction.WIND_BLAST, Map.of(
            Runes.AIR, 3,
            Runes.DEATH, 1
    )),
    WATER_BLAST(MagicAction.WATER_BLAST, Map.of(
            Runes.WATER, 3,
            Runes.AIR, 3,
            Runes.DEATH, 1
    )),
    EARTH_BLAST(MagicAction.EARTH_BLAST, Map.of(
            Runes.EARTH, 4,
            Runes.AIR, 3,
            Runes.DEATH, 1
    )),
    FIRE_BLAST(MagicAction.FIRE_BLAST, Map.of(
            Runes.FIRE, 5,
            Runes.AIR, 4,
            Runes.DEATH, 1
    )),
    WIND_WAVE(MagicAction.WIND_WAVE, Map.of(
            Runes.AIR, 5,
            Runes.BLOOD, 1
    )),
    WATER_WAVE(MagicAction.WATER_WAVE, Map.of(
            Runes.WATER, 7,
            Runes.AIR, 5,
            Runes.BLOOD, 1
    )),
    EARTH_WAVE(MagicAction.EARTH_WAVE, Map.of(
            Runes.EARTH, 7,
            Runes.AIR, 5,
            Runes.BLOOD, 1
    )),
    FIRE_WAVE(MagicAction.FIRE_WAVE, Map.of(
            Runes.FIRE, 7,
            Runes.AIR, 5,
            Runes.BLOOD, 1
    )),
    WIND_SURGE(MagicAction.WIND_SURGE, Map.of(
            Runes.AIR, 7,
            Runes.BLOOD, 1,
            Runes.DEATH, 1
    )),
    WATER_SURGE(MagicAction.WATER_SURGE, Map.of(
            Runes.WATER, 10,
            Runes.AIR, 7,
            Runes.BLOOD, 1,
            Runes.DEATH, 1
    )),
    EARTH_SURGE(MagicAction.EARTH_SURGE, Map.of(
            Runes.EARTH, 10,
            Runes.AIR, 7,
            Runes.BLOOD, 1,
            Runes.DEATH, 1
    )),
    FIRE_SURGE(MagicAction.FIRE_SURGE, Map.of(
            Runes.FIRE, 10,
            Runes.AIR, 7,
            Runes.BLOOD, 1,
            Runes.DEATH, 1
    ));

    private final MagicAction magicAction;
    private final Map<Runes, Integer> requiredRunes;
    private final Integer varbitValue; // Varbit 276

    Rs2CombatSpells(MagicAction magicAction, Map<Runes, Integer> requiredRunes) {
        this.magicAction = magicAction;
        this.requiredRunes = requiredRunes;
        this.varbitValue = ordinal() + 1;
    }
}
