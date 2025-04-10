package net.runelite.client.plugins.microbot.magic.aiomagic.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.client.plugins.microbot.util.magic.Rs2Spells;

@Getter
@RequiredArgsConstructor
public enum TeleportSpell {
    VARROCK_TELEPORT(Rs2Spells.VARROCK_TELEPORT),
    LUMBRIDGE_TELEPORT(Rs2Spells.LUMBRIDGE_TELEPORT),
    FALADOR_TELEPORT(Rs2Spells.FALADOR_TELEPORT),
    CAMELOT_TELEPORT(Rs2Spells.CAMELOT_TELEPORT),
    ARDOUGNE_TELEPORT(Rs2Spells.ARDOUGNE_TELEPORT),
    WATCHTOWER_TELEPORT(Rs2Spells.WATCHTOWER_TELEPORT),
    TROLLHEIM_TELEPORT(Rs2Spells.TROLLHEIM_TELEPORT),;
    
    private final Rs2Spells rs2Spell;
}
