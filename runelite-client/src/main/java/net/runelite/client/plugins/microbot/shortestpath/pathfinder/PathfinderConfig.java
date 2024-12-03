package net.runelite.client.plugins.microbot.shortestpath.pathfinder;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.itemcharges.ItemChargeConfig;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.shortestpath.*;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.runelite.client.plugins.microbot.shortestpath.TransportType.*;

public class PathfinderConfig {
    private static final WorldArea WILDERNESS_ABOVE_GROUND = new WorldArea(2944, 3523, 448, 448, 0);
    private static final WorldArea WILDERNESS_ABOVE_GROUND_LEVEL_20 = new WorldArea(2944, 3680, 448, 448, 0);
    private static final WorldArea WILDERNESS_ABOVE_GROUND_LEVEL_30 = new WorldArea(2944, 3760, 448, 448, 0);
    private static final WorldArea WILDERNESS_UNDERGROUND = new WorldArea(2944, 9918, 320, 442, 0);
    private static final WorldArea WILDERNESS_UNDERGROUND_LEVEL_20 = new WorldArea(2944, 10075, 320, 442, 0);
    private static final WorldArea WILDERNESS_UNDERGROUND_LEVEL_30 = new WorldArea(2944, 10155, 320, 442, 0);

    private final SplitFlagMap mapData;
    private final ThreadLocal<CollisionMap> map;
    /** All transports by origin {@link WorldPoint}. The null key is used for transports centered on the player. */
    private final Map<WorldPoint, Set<Transport>> allTransports;
    @Setter
    private Set<Transport> usableTeleports;

    @Getter
    private ConcurrentHashMap<WorldPoint, Set<Transport>> transports;
    // Copy of transports with packed positions for the hotpath; lists are not copied and are the same reference in both maps
    @Getter
    @Setter
    private PrimitiveIntHashMap<Set<Transport>> transportsPacked;

    private final Client client;
    private final ShortestPathConfig config;

    @Getter
    private long calculationCutoffMillis;
    @Getter
    private boolean avoidWilderness;
    private boolean useAgilityShortcuts,
            useGrappleShortcuts,
            useBoats,
            useCanoes,
            useCharterShips,
            useShips,
            useFairyRings,
            useGnomeGliders,
            useMinecarts,
            useQuetzals,
            useSpiritTrees,
            useTeleportationLevers,
            useTeleportationPortals,
            useTeleportationSpells,
            useWildernessObelisks;
    //START microbot variables
    @Getter
    private int distanceBeforeUsingTeleport;
    @Getter
    private final List<Restriction> resourceRestrictions;
    @Getter
    private List<Restriction> customRestrictions;
    @Getter
    private Set<Integer> restrictedPointsPacked;
    private boolean useNpcs;
    //END microbot variables
    private TeleportationItem useTeleportationItems;
    private final int[] boostedLevels = new int[Skill.values().length];
    private Map<Quest, QuestState> questStates = new HashMap<>();
    private Map<Integer, Integer> varbitValues = new HashMap<>();

    @Getter
    @Setter
    // Used for manual calculating paths without teleport & items in caves
    private boolean ignoreTeleportAndItems = false;

    public PathfinderConfig(SplitFlagMap mapData, Map<WorldPoint, Set<Transport>> transports,
                            List<Restriction> restrictions,
                            Client client, ShortestPathConfig config) {
        this.mapData = mapData;
        this.map = ThreadLocal.withInitial(() -> new CollisionMap(this.mapData));
        this.allTransports = transports;
        this.usableTeleports = new HashSet<>(allTransports.size() / 20);
        this.transports = new ConcurrentHashMap<>(allTransports.size() / 2);
        this.transportsPacked = new PrimitiveIntHashMap<>(allTransports.size() / 2);
        this.client = client;
        this.config = config;
        //START microbot variables
        this.resourceRestrictions = restrictions;
        this.customRestrictions = new ArrayList<>();
        this.restrictedPointsPacked = new HashSet<>();
        //END microbot variables
    }

    public CollisionMap getMap() {
        return map.get();
    }

    public void refresh() {
        calculationCutoffMillis = config.calculationCutoff() * Constants.GAME_TICK_LENGTH;
        avoidWilderness = config.avoidWilderness();
        useAgilityShortcuts = config.useAgilityShortcuts();
        useGrappleShortcuts = config.useGrappleShortcuts();
        useBoats = config.useBoats();
        useCanoes = config.useCanoes();
        useCharterShips = config.useCharterShips();
        useShips = config.useShips();
        useFairyRings = config.useFairyRings();
        useGnomeGliders = config.useGnomeGliders();
        useMinecarts = config.useMinecarts();
        useQuetzals = config.useQuetzals();
        useSpiritTrees = config.useSpiritTrees();
        useTeleportationItems = config.useTeleportationItems();
        useTeleportationLevers = config.useTeleportationLevers();
        useTeleportationPortals = config.useTeleportationPortals();
        useTeleportationSpells = config.useTeleportationSpells();
        useWildernessObelisks = config.useWildernessObelisks();
        distanceBeforeUsingTeleport = config.distanceBeforeUsingTeleport();

        //START microbot variables
        useNpcs = config.useNpcs();
        //END microbot variables

        if (GameState.LOGGED_IN.equals(client.getGameState())) {
            for (int i = 0; i < Skill.values().length; i++) {
                boostedLevels[i] = client.getBoostedSkillLevel(Skill.values()[i]);
            }

            refreshTransports();
            //START microbot variables
            refreshRestrictionData();
            Rs2Tab.switchToInventoryTab();
            //END microbot variables
        }
    }

    /** Specialized method for only updating player-held item and spell transports */
    public void refreshTeleports(int packedLocation, int wildernessLevel) {
        Set<Transport> usableWildyTeleports = new HashSet<>(usableTeleports.size());
        if (ignoreTeleportAndItems) return;

        for (Transport teleport : usableTeleports) {
            if (wildernessLevel <= teleport.getMaxWildernessLevel()) {
                usableWildyTeleports.add(teleport);
            }
        }

        if (!usableWildyTeleports.isEmpty()) {
            // Added extra code to check if the key already exists
            // if the key already exists we append instead of overwriting
            // The issue was that the transport list would contain a transport object on the same
            // tile as the player, this would then be overwritten by the usableWildyTeleports
            // therefor losing the original transport object
            WorldPoint key = WorldPointUtil.unpackWorldPoint(packedLocation);
            Set<Transport> existingTeleports = transports.get(key);
            if (existingTeleports != null) {
                existingTeleports.addAll(usableWildyTeleports);
            } else {
                transports.put(key, usableWildyTeleports);
            }
            transportsPacked.put(packedLocation, usableWildyTeleports);
        }
    }

    private void refreshTransports() {
        useFairyRings &= !QuestState.NOT_STARTED.equals(Rs2Player.getQuestState(Quest.FAIRYTALE_II__CURE_A_QUEEN))
                && (Rs2Inventory.contains(ItemID.DRAMEN_STAFF, ItemID.LUNAR_STAFF)
                || Rs2Equipment.isWearing(ItemID.DRAMEN_STAFF)
                || Rs2Equipment.isWearing(ItemID.LUNAR_STAFF)
                || Microbot.getVarbitValue(Varbits.DIARY_LUMBRIDGE_ELITE)  == 1);
        useGnomeGliders &= QuestState.FINISHED.equals(Rs2Player.getQuestState(Quest.THE_GRAND_TREE));
        useSpiritTrees &= QuestState.FINISHED.equals(Rs2Player.getQuestState(Quest.TREE_GNOME_VILLAGE));
        useQuetzals &= QuestState.FINISHED.equals(Rs2Player.getQuestState(Quest.TWILIGHTS_PROMISE));

        transports.clear();
        transportsPacked.clear();
        usableTeleports.clear();
         Microbot.getClientThread().runOnClientThread(() -> {
            for (Map.Entry<WorldPoint, Set<Transport>> entry : allTransports.entrySet()) {
                for (Transport transport : entry.getValue()) {
                    for (Quest quest : transport.getQuests()) {
                        try {
                            QuestState currentState = questStates.get(quest);
                            QuestState newState = Rs2Player.getQuestState(quest);

                            // Only update if the new state is more progressed
                            if (currentState == null || isMoreProgressed(newState, currentState)) {
                                questStates.put(quest, newState);
                            }
                        } catch (NullPointerException ignored) {
                            System.out.println(ignored.getMessage());
                        }
                    }
                    for (TransportVarbit varbitCheck : transport.getVarbits()) {
                        varbitValues.put(varbitCheck.getVarbitId(), Microbot.getVarbitValue(varbitCheck.getVarbitId()));
                    }
                }
            }
            return true;
        });

        for (Map.Entry<WorldPoint, Set<Transport>> entry : allTransports.entrySet()) {
            WorldPoint point = entry.getKey();
            Set<Transport> usableTransports = new HashSet<>(entry.getValue().size());
            for (Transport transport : entry.getValue()) {

                if (point == null && useTransport(transport) && hasRequiredItems(transport)) {
                    usableTeleports.add(transport);
                } else if (useTransport(transport)) {
                    usableTransports.add(transport);
                }
            }

            if (point != null && !usableTransports.isEmpty()) {
                transports.put(point, usableTransports);
                transportsPacked.put(WorldPointUtil.packWorldPoint(point), usableTransports);
            }
        }
    }

    private void refreshRestrictionData() {
        restrictedPointsPacked.clear();

        Set<Quest> questsToFetch = new HashSet<>();
        Set<Integer> varbitsToFetch = new HashSet<>();
        List<Restriction> allRestrictions = Stream.concat(resourceRestrictions.stream(), customRestrictions.stream())
                .collect(Collectors.toList());

        for (Restriction entry : allRestrictions) {
            questsToFetch.addAll(entry.getQuests());
            for (TransportVarbit varbitCheck : entry.getVarbits()) {
                varbitsToFetch.add(varbitCheck.getVarbitId());
            }
        }

        // Fetch quest states and varbit values directly
        for (Quest quest : questsToFetch) {
            try {
                QuestState currentState = questStates.get(quest);
                QuestState newState = Rs2Player.getQuestState(quest);

                // Only update if the new state is more progressed
                if (currentState == null || isMoreProgressed(newState, currentState)) {
                    questStates.put(quest, newState);
                }
            } catch (NullPointerException ignored) {
                // Handle exceptions if necessary
            }
        }
        for (Integer varbitId : varbitsToFetch) {
            varbitValues.put(varbitId, Microbot.getVarbitValue(varbitId));
        }

        for (Restriction entry : allRestrictions) {
            boolean restrictionApplies = false;

            // Check if there are no quests, varbits, or skills, used for explicit restrictions
            if (entry.getQuests().isEmpty() && entry.getVarbits().isEmpty() && Arrays.stream(entry.getSkillLevels()).allMatch(level -> level == 0)) {
                restrictionApplies = true;
            }

            // Quest check
            if (!restrictionApplies) {
                for (Quest quest : entry.getQuests()) {
                    if (questStates.getOrDefault(quest, QuestState.NOT_STARTED) != QuestState.FINISHED) {
                        restrictionApplies = true;
                        break;
                    }
                }
            }

            // Varbit check
            if (!restrictionApplies) {
                for (TransportVarbit varbitCheck : entry.getVarbits()) {
                    int varbitId = varbitCheck.getVarbitId();
                    int actualValue = varbitValues.getOrDefault(varbitId, -1);
                    if (!varbitCheck.matches(actualValue)) {
                        restrictionApplies = true;
                        break;
                    }
                }
            }

            // Skill level check
            if (!restrictionApplies && !hasRequiredLevels(entry)) {
                restrictionApplies = true;
            }

            if (restrictionApplies) {
                restrictedPointsPacked.add(entry.getPackedWorldPoint());
            }
        }
    }

    public static boolean isInWilderness(WorldPoint p) {
        return WILDERNESS_ABOVE_GROUND.distanceTo(p) == 0 || WILDERNESS_UNDERGROUND.distanceTo(p) == 0;
    }

    public static boolean isInWilderness(int packedPoint) {
        return WorldPointUtil.distanceToArea(packedPoint, WILDERNESS_ABOVE_GROUND) == 0
                || WorldPointUtil.distanceToArea(packedPoint, WILDERNESS_UNDERGROUND) == 0;
    }

    public boolean avoidWilderness(int packedPosition, int packedNeightborPosition, boolean targetInWilderness) {
        return avoidWilderness && !targetInWilderness
                && !isInWilderness(packedPosition) && isInWilderness(packedNeightborPosition);
    }

    public boolean isInLevel20Wilderness(int packedPoint) {
        return WorldPointUtil.distanceToArea(packedPoint, WILDERNESS_ABOVE_GROUND_LEVEL_20) == 0
                || WorldPointUtil.distanceToArea(packedPoint, WILDERNESS_UNDERGROUND_LEVEL_20) == 0;
    }

    public boolean isInLevel30Wilderness(int packedPoint){
        return WorldPointUtil.distanceToArea(packedPoint, WILDERNESS_ABOVE_GROUND_LEVEL_30) == 0
                || WorldPointUtil.distanceToArea(packedPoint, WILDERNESS_UNDERGROUND_LEVEL_30) == 0;

    }

    private boolean completedQuests(Transport transport) {
        for (Quest quest : transport.getQuests()) {
            QuestState state = questStates.getOrDefault(quest, QuestState.NOT_STARTED);
            if (state != QuestState.FINISHED) {
                return false;
            }
        }
        return true;
    }

    private boolean varbitChecks(Transport transport) {
        if (varbitValues.isEmpty()) return true;
        for (TransportVarbit varbitCheck : transport.getVarbits()) {
            int actualValue = varbitValues.getOrDefault(varbitCheck.getVarbitId(), -1);
            if (!varbitCheck.matches(actualValue)) {
                return false;
            }
        }
        return true;
    }

    private boolean useTransport(Transport transport) {
        // Check if the feature flag is disabled
        if (!isFeatureEnabled(transport.getType())) return false;
        // If you don't meet level requirements
        if (!hasRequiredLevels(transport)) return false;
        // If the transport has quest requirements & the quest haven't been completed
        if (transport.isQuestLocked() && !completedQuests(transport)) return false;
        // If the transport has varbit requirements & the varbits do not match
        if (!varbitChecks(transport)) return false;
        // If you don't have the required Items & Amount for transport (used for charters & minecarts)
        if (transport.getAmtItemRequired() > 0 && !Rs2Inventory.hasItemAmount(transport.getItemRequired(), transport.getAmtItemRequired())) return false;
        // Check Teleport Item Settings
        if (transport.getType() == TELEPORTATION_ITEM) return isTeleportationItemUsable(transport);

        return true;
    }

    /** Checks if the player has all the required skill levels for the transport */
    private boolean hasRequiredLevels(Transport transport) {
        int[] requiredLevels = transport.getSkillLevels();
        for (int i = 0; i < boostedLevels.length; i++) {
            int boostedLevel = boostedLevels[i];
            int requiredLevel = requiredLevels[i];
            if (boostedLevel < requiredLevel) {
                return false;
            }
        }
        return true;
    }

    /** Checks if the player has all the required skill levels for the restriction */
    private boolean hasRequiredLevels(Restriction restriction) {
        int[] requiredLevels = restriction.getSkillLevels();
        for (int i = 0; i < boostedLevels.length; i++) {

            if (Skill.values()[i] == Skill.AGILITY && requiredLevels[i] > 0 && !config.useAgilityShortcuts()) return false;

            int boostedLevel = boostedLevels[i];
            int requiredLevel = requiredLevels[i];

            if (boostedLevel < requiredLevel) {
                return false;
            }
        }
        return true;
    }

    private boolean isFeatureEnabled(TransportType type) {
        if (!client.getWorldType().contains(WorldType.MEMBERS)) {
            // Transport types that require membership
            switch (type) {
                case AGILITY_SHORTCUT:
                case GRAPPLE_SHORTCUT:
                case BOAT:
                case CANOE:
                case CHARTER_SHIP:
                case FAIRY_RING:
                case GNOME_GLIDER:
                case MINECART:
                case QUETZAL:
                case SPIRIT_TREE:
                    return false; // Not enabled without membership
            }
        }

        switch (type) {
            case AGILITY_SHORTCUT:
                return useAgilityShortcuts;
            case GRAPPLE_SHORTCUT:
                return useGrappleShortcuts;
            case BOAT:
                return useBoats;
            case CANOE:
                return useCanoes;
            case CHARTER_SHIP:
                return useCharterShips;
            case SHIP:
                return useShips;
            case FAIRY_RING:
                return useFairyRings;
            case GNOME_GLIDER:
                return useGnomeGliders;
            case MINECART:
                return useMinecarts;
            case NPC:
                return useNpcs;
            case QUETZAL:
                return useQuetzals;
            case SPIRIT_TREE:
                return useSpiritTrees;
            case TELEPORTATION_ITEM:
                return useTeleportationItems != TeleportationItem.NONE;
            case TELEPORTATION_LEVER:
                return useTeleportationLevers;
            case TELEPORTATION_PORTAL:
                return useTeleportationPortals;
            case TELEPORTATION_SPELL:
                return useTeleportationSpells;
            case WILDERNESS_OBELISK:
                return useWildernessObelisks;
            default:
                return true; // Default to enabled if no specific toggle
        }
    }

    /** Checks if a teleportation item is usable */
    private boolean isTeleportationItemUsable(Transport transport) {
        if (useTeleportationItems == TeleportationItem.NONE) return false;
        // Check consumable items configuration
        if (useTeleportationItems == TeleportationItem.ALL_NON_CONSUMABLE && transport.isConsumable()) return false;
        
        return hasRequiredItems(transport);
    }

    /** Checks if the player has all the required equipment and inventory items for the transport */
    private boolean hasRequiredItems(Transport transport) {
        // Global flag to disable teleports
        if (Rs2Walker.disableTeleports) return false;

        // Handle teleportation items
        if (TransportType.TELEPORTATION_ITEM.equals(transport.getType())) {
            // Special case for Chronicle teleport
            if (requiresChronicle(transport)) return hasChronicleCharges();

            return transport.getItemIdRequirements()
                    .stream()
                    .flatMap(Collection::stream)
                    .anyMatch(itemId -> Rs2Equipment.isWearing(itemId) || Rs2Inventory.hasItem(itemId));
        }
        
        // Handle teleportation spells
        if (TransportType.TELEPORTATION_SPELL.equals(transport.getType())) return Rs2Magic.quickCanCast(transport.getDisplayInfo());

        // Check membership restrictions
        if (!client.getWorldType().contains(WorldType.MEMBERS)) return false;

        // General item requirements
        return transport.getItemIdRequirements()
                .stream()
                .flatMap(Collection::stream)
                .anyMatch(itemId -> Rs2Equipment.isWearing(itemId) || Rs2Inventory.hasItem(itemId));
    }

    /** Checks if the transport requires the Chronicle */
    private boolean requiresChronicle(Transport transport) {
        return transport.getItemIdRequirements()
                .stream()
                .flatMap(Collection::stream)
                .anyMatch(itemId -> itemId == ItemID.CHRONICLE);
    }

    /** Checks if the Chronicle has charges */
    private boolean hasChronicleCharges() {
        if (!Rs2Equipment.isWearing(ItemID.CHRONICLE) || !Rs2Inventory.hasItem(ItemID.CHRONICLE)) return false;
        
        String charges = Microbot.getConfigManager()
                .getRSProfileConfiguration(ItemChargeConfig.GROUP, ItemChargeConfig.KEY_CHRONICLE);

        // If charges are unknown, attempt to retrieve them
        if (charges == null || charges.isEmpty()) {
            if (Rs2Inventory.hasItem(ItemID.CHRONICLE)) {
                Rs2Inventory.interact(ItemID.CHRONICLE, "Check charges");
            } else if (Rs2Equipment.hasEquipped(ItemID.CHRONICLE)) {
                Rs2Equipment.interact(ItemID.CHRONICLE, "Check charges");
            }
            charges = Microbot.getConfigManager().getRSProfileConfiguration(ItemChargeConfig.GROUP, ItemChargeConfig.KEY_CHRONICLE);
        }

        // Validate charges
        return charges != null && Integer.parseInt(charges) > 0;
    }
    
    /** Checks if a QuestState is further progressed than currentState **/
    private boolean isMoreProgressed(QuestState newState, QuestState currentState) {
        if (currentState == null) return false;
        if (newState == null) return false;
        
        // Define the progression order of states
        List<QuestState> progressionOrder = Arrays.asList(
                QuestState.NOT_STARTED,
                QuestState.IN_PROGRESS,
                QuestState.FINISHED
        );

        return progressionOrder.indexOf(newState) > progressionOrder.indexOf(currentState);
    }
    
    @Deprecated(since = "1.6.2 - Add Restrictions to restrictions.tsv", forRemoval = true)
    public void setRestrictedTiles(Restriction... restrictions){
        this.customRestrictions = List.of(restrictions);
    }
}
