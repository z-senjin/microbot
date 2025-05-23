package net.runelite.client.plugins.microbot.mntn.MntnSlayer;

public enum MntnSlayerState {
    INITIALIZING,
    GET_TASK, // walk to slayer master and get task
    BANK, // walk to bank and get gear / food
    WALK_TO_MONSTER, // walk to monster location
    FIGHTING, // fight, handle prayer based on monster, when out of food or task is over -> teleport
    TELEPORT, // teleport and bank
    EXIT
}
