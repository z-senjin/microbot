package net.runelite.client.plugins.microbot.mntn.MntnSlayer.data;

import lombok.Getter;

@Getter
public enum TeleportOption {
    VARROCK_TELEPORT("Varrock teleport", "Break"),
    FALADOR_TELEPORT("Falador teleport", "Break"),
    LUMBRIDGE_TELEPORT("Lumbridge teleport", "Break"),
    CAMELOT_TELEPORT("Camelot teleport", "Break")
    ;

    private final String name;
    private final String action;

    TeleportOption(String name, String action) {
        this.name = name;
        this.action = action;
    }
}
