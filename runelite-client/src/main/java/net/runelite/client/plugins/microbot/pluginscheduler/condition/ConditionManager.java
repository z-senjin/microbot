package net.runelite.client.plugins.microbot.pluginscheduler.condition;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.events.GroundObjectDespawned;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.TileItem;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.InteractingChanged;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.logical.AndCondition;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.logical.LogicalCondition;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.logical.NotCondition;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.logical.OrCondition;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.resource.LootItemCondition;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.resource.ResourceCondition;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.time.SingleTriggerTimeCondition;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.time.TimeCondition;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * Manages logical conditions for plugin scheduling and user-defined triggers in a hierarchical structure.
 * <p>
 * The ConditionManager provides a framework for organizing, evaluating, and monitoring complex
 * condition structures using logical operators (AND/OR) to determine when plugin execution
 * should occur. It maintains separate logical trees for plugin-defined and user-defined conditions,
 * which are combined at evaluation time with appropriate logical operators.
 * <p>
 * Key features:
 * <ul>
 *   <li>Separate management of plugin-defined and user-defined conditions</li>
 *   <li>Support for complex nested logical structures (AND/OR)</li>
 *   <li>Event propagation to registered conditions</li>
 *   <li>Time-based trigger calculation and progress tracking</li>
 *   <li>Condition tree manipulation (adding, removing, finding conditions)</li>
 * </ul>
 * <p>
 * The class implements event handlers for various RuneLite events, passing them to all registered
 * conditions so they can update their state accordingly. It also provides methods to calculate
 * when conditions will be satisfied next and track progress toward these triggers.
 * <p>
 * Usage example:
 * <pre>
 * ConditionManager manager = new ConditionManager();
 * manager.setRequireAll(); // Use AND logic for user conditions
 * manager.addUserCondition(new InventoryItemCondition(ItemID.COINS, 1000));
 * manager.addUserCondition(new SkillLevelCondition(Skill.ATTACK, 60));
 * 
 * // Check if conditions are met
 * boolean ready = manager.areConditionsMet();
 * </pre>
 */
@Slf4j
public class ConditionManager {
    
    
    private LogicalCondition pluginCondition = new OrCondition();
    @Getter
    private LogicalCondition userLogicalCondition;
    private final EventBus eventBus;
    private boolean eventsRegistered = false;
    public ConditionManager() {
        this.eventBus = Microbot.getEventBus();
        userLogicalCondition = new AndCondition();

    }
    public void setPluginCondition(LogicalCondition condition) {
        pluginCondition = condition;
    }
    public LogicalCondition getPluginCondition() {
        return pluginCondition;
    }    
    public List<Condition> getConditions() {
        List<Condition> conditions = new ArrayList<>();
        if (pluginCondition != null) {
            conditions.addAll(pluginCondition.getConditions());
        }
        conditions.addAll(userLogicalCondition.getConditions());
        return conditions;
    }
    /**
     * Checks if any conditions exist in the manager.
     * 
     * @return true if at least one condition exists in either plugin or user condition structures
     */
    public boolean hasConditions() {
        return !getConditions().isEmpty();
    }
    /**
     * Retrieves all time-based conditions from both plugin and user condition structures.
     * Uses the LogicalCondition.findTimeConditions method to recursively find all TimeCondition
     * instances throughout the nested logical structure.
     * 
     * @return A list of all TimeCondition instances managed by this ConditionManager
     */
    public List<TimeCondition> getTimeConditions() {
        List<TimeCondition> timeConditions = new ArrayList<>();
        
        //log.info("Searching for TimeConditions in logical structures");
        
        // Get time conditions from user logical structure
        if (userLogicalCondition != null) {
            List<Condition> userTimeConditions = userLogicalCondition.findTimeConditions();
            //log.info("Found {} time conditions in user logical structure", userTimeConditions.size());
            
            for (Condition condition : userTimeConditions) {
                if (condition instanceof TimeCondition) {
                    TimeCondition timeCondition = (TimeCondition) condition;
          //          log.info("Found TimeCondition in user structure: {} (implementation: {})", 
                     //       timeCondition, timeCondition.getClass().getSimpleName());
                    timeConditions.add(timeCondition);
                }
            }
        }
        
        // Get time conditions from plugin logical structure
        if (pluginCondition != null) {
            List<Condition> pluginTimeConditions = pluginCondition.findTimeConditions();
            //log.info("Found {} time conditions in plugin logical structure", pluginTimeConditions.size());
            
            for (Condition condition : pluginTimeConditions) {
                if (condition instanceof TimeCondition) {
                    TimeCondition timeCondition = (TimeCondition) condition;
                    //log.info("Found TimeCondition in plugin structure: {} (implementation: {})", 
                            //timeCondition, timeCondition.getClass().getSimpleName());
                    timeConditions.add(timeCondition);
                }
            }
        }
        
        //log.info("Total TimeConditions found in all logical structures: {}", timeConditions.size());
        return timeConditions;
    }
    /**
     * Returns the user logical condition structure.
     * 
     * @return The logical condition structure containing user-defined conditions
     */
    public LogicalCondition getUserCondition() {
        return userLogicalCondition;
    }
    /**
     * Removes all user-defined conditions while preserving the logical structure.
     * This clears the user condition list without changing the logical operator (AND/OR).
     */
    public void clearUserConditions() {
        userLogicalCondition.getConditions().clear();
    }
    /**
     * Evaluates if all conditions are currently satisfied, respecting the logical structure.
     * <p>
     * This method first checks if user conditions are met according to their logical operator (AND/OR).
     * If plugin conditions exist, they must also be satisfied (always using AND logic between
     * user and plugin conditions).
     * 
     * @return true if all required conditions are satisfied based on the logical structure
     */
    public boolean areConditionsMet() {
        boolean userConditionsMet = false;
        
        userConditionsMet = userLogicalCondition.isSatisfied();                            
        log.debug("User conditions met: {} (using {} logic)", 
            userConditionsMet, 
            (userLogicalCondition instanceof AndCondition) ? "AND" : "OR");
        if (pluginCondition != null && !pluginCondition.getConditions().isEmpty()) {          
            boolean pluginConditionsMet = pluginCondition.isSatisfied();
            log.debug("Plugin conditions met: {}", pluginConditionsMet);
            return userConditionsMet && pluginConditionsMet;            
        }
        return userConditionsMet;                                                
    }
   /**
     * Returns a list of conditions that were defined by the user (not plugin-defined).
     * This method only retrieves conditions from the user logical condition structure,
     * not from the plugin condition structure.
     * 
     * @return List of user-defined conditions, or an empty list if no user logical condition exists
     */
    public List<Condition> getUserConditions() {
        if (userLogicalCondition == null) {
            return new ArrayList<>();
        }
                
        return userLogicalCondition.getConditions();
    }
    /**
     * Registers this condition manager to receive RuneLite events.
     * Event listeners are registered with the event bus to allow conditions
     * to update their state based on game events. This method is idempotent
     * and will not register the same listeners twice.
     */
    public void registerEvents() {
        if (eventsRegistered) {
            return;
        }                
        eventBus.register(this);
        eventsRegistered = true;
    }
    
    /**
     * Unregisters this condition manager from receiving RuneLite events.
     * This removes all event listeners from the event bus that were previously registered.
     * This method is idempotent and will do nothing if events are not currently registered.
     */
    public void unregisterEvents() {
        if (!eventsRegistered) {
            return;
        }        
        eventBus.unregister(this);
        eventsRegistered = false;
    }
    
   
    /**
     * Sets the user logical condition to require ALL conditions to be met (AND logic).
     * This creates a new AndCondition to replace the existing user logical condition.
     */
    public void setRequireAll() {
        userLogicalCondition = new AndCondition();
        setUserLogicalCondition(userLogicalCondition);
        
    }
    
    /**
     * Sets the user logical condition to require ANY condition to be met (OR logic).
     * This creates a new OrCondition to replace the existing user logical condition.
     */
    public void setRequireAny() {
        userLogicalCondition = new OrCondition();        
        setUserLogicalCondition(userLogicalCondition);
    }
    
   
    
    /**
     * Generates a human-readable description of the current condition structure.
     * The description includes the logical operator type (ANY/ALL) and descriptions
     * of all user conditions. If plugin conditions exist, those are appended as well.
     * 
     * @return A string representation of the condition structure
     */
    public String getDescription() {
        
                  
        StringBuilder sb;
        if (requiresAny()){
            sb = new StringBuilder("ANY of: (");
        }else{
            sb = new StringBuilder("ALL of: (");
        }
        List<Condition> userConditions = userLogicalCondition.getConditions();
        if (userConditions.isEmpty()) {
            sb.append("No conditions");
        }else{
            for (int i = 0; i < userConditions.size(); i++) {
                if (i > 0) sb.append(" OR ");
                sb.append(userConditions.get(i).getDescription());
            }
        }
        sb.append(")");

        if ( this.pluginCondition!= null) {
            sb.append(" AND : ");
            sb.append(this.pluginCondition.getDescription());            
        }
        return sb.toString();      
        
    }
    
   /**
     * Checks if the user logical condition requires all conditions to be met (AND logic).
     * 
     * @return true if the user logical condition is an AndCondition, false otherwise
     */
    public boolean userConditionRequiresAll() {

        return userLogicalCondition instanceof AndCondition;
    }
    /**
     * Checks if the user logical condition requires any condition to be met (OR logic).
     * 
     * @return true if the user logical condition is an OrCondition, false otherwise
     */
    public boolean userConditionRequiresAny() {
        return userLogicalCondition instanceof OrCondition;
    }
    /**
     * Checks if the full logical structure (combining user and plugin conditions)
     * requires all conditions to be met (AND logic).
     * 
     * @return true if the full logical condition is an AndCondition, false otherwise
     */
    public boolean requiresAll() {
        return this.getFullLogicalCondition() instanceof AndCondition;
    }
    /**
     * Checks if the full logical structure (combining user and plugin conditions)
     * requires any condition to be met (OR logic).
     * 
     * @return true if the full logical condition is an OrCondition, false otherwise
     */
    public boolean requiresAny() {
        return this.getFullLogicalCondition() instanceof OrCondition;
    }
    /**
     * Resets all conditions in both user and plugin logical structures to their initial state.
     * This method calls the reset() method on all conditions.
     */
    public void reset() {
        userLogicalCondition.reset();
        if (pluginCondition != null) {
            pluginCondition.reset();
        }
        
    }
    /**
     * Resets all conditions in both user and plugin logical structures with an option to randomize.
     * 
     * @param randomize If true, conditions will be reset with randomized initial values where applicable
     */
    public void reset(boolean randomize) {
        userLogicalCondition.reset(randomize);
        if (pluginCondition != null) {
            pluginCondition.reset(randomize);
        }
    }

    /**
     * Checks if a condition is a plugin-defined condition that shouldn't be edited by users.
     */
    public boolean isPluginDefinedCondition(Condition condition) {
        // If there are no plugin-defined conditions, return false
        if (pluginCondition == null) {
            return false;
        }
        if (condition instanceof LogicalCondition) {
            // If the condition is a logical condition, check if it's part of the plugin condition
            if (pluginCondition.equals(condition)) {
                return true;
            }            
        }
        // Checfk if the condition is contained in the plugin condition hierarchy
        return pluginCondition.contains(condition);
    }

    

    /**
     * Recursively searches for and removes a condition from nested logical conditions.
     * 
     * @param parent The logical condition to search within
     * @param target The condition to remove
     * @return true if the condition was found and removed, false otherwise
     */
    private boolean removeFromNestedCondition(LogicalCondition parent, Condition target) {
        // Search each child of the parent logical condition
        for (int i = 0; i < parent.getConditions().size(); i++) {
            Condition child = parent.getConditions().get(i);
            
            // If this child is itself a logical condition, search within it
            if (child instanceof LogicalCondition) {
                LogicalCondition logicalChild = (LogicalCondition) child;
                
                // First check if the target is a direct child of this logical condition
                if (logicalChild.getConditions().remove(target)) {
                    // If removing the condition leaves the logical condition empty, remove it too
                    if (logicalChild.getConditions().isEmpty()) {
                        parent.getConditions().remove(i);
                    }
                    return true;
                }
                
                // If not a direct child, recurse into the logical child
                if (removeFromNestedCondition(logicalChild, target)) {
                    // If removing the condition leaves the logical condition empty, remove it too
                    if (logicalChild.getConditions().isEmpty()) {
                        parent.getConditions().remove(i);
                    }
                    return true;
                }
            }
            // Special case for NotCondition
            else if (child instanceof NotCondition) {
                NotCondition notChild = (NotCondition) child;
                
                // If the NOT condition wraps our target, remove the whole NOT condition
                if (notChild.getCondition() == target) {
                    parent.getConditions().remove(i);
                    return true;
                }
                
                // If the NOT condition wraps a logical condition, search within that
                if (notChild.getCondition() instanceof LogicalCondition) {
                    LogicalCondition wrappedLogical = (LogicalCondition) notChild.getCondition();
                    if (removeFromNestedCondition(wrappedLogical, target)) {
                        // If removing the condition leaves the logical condition empty, remove the NOT condition too
                        parent.getConditions().remove(i);
                    }
                }
            }
        }
        
        return false;
    }

    /**
     * Sets the user's logical condition structure
     * 
     * @param logicalCondition The logical condition to set as the user structure
     */
    public void setUserLogicalCondition(LogicalCondition logicalCondition) {
        this.userLogicalCondition = logicalCondition;
    }

    /**
     * Gets the user's logical condition structure
     * 
     * @return The current user logical condition, or null if none exists
     */
    public LogicalCondition getUserLogicalCondition() {
        return this.userLogicalCondition;
    }

    
    public boolean addToLogicalStructure(LogicalCondition parent, Condition toAdd) {
        // Try direct addition first
        if (parent.getConditions().add(toAdd)) {
            return true;
        }
        
        // Try to add to child logical conditions
        for (Condition child : parent.getConditions()) {
            if (child instanceof LogicalCondition) {
                if (addToLogicalStructure((LogicalCondition) child, toAdd)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    /**
     * Recursively removes a condition from a logical structure
     */
    public boolean removeFromLogicalStructure(LogicalCondition parent, Condition toRemove) {
        // Try direct removal first
        if (parent.getConditions().remove(toRemove)) {
            return true;
        }
        
        // Try to remove from child logical conditions
        for (Condition child : parent.getConditions()) {
            if (child instanceof LogicalCondition) {
                if (removeFromLogicalStructure((LogicalCondition) child, toRemove)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    /**
     * Makes sure there's a valid user logical condition to work with
     */
    private void ensureUserLogicalExists() {
        if (userLogicalCondition == null) {
            userLogicalCondition = new AndCondition();
        }
    }

    /**
     * Checks if the condition exists in either user or plugin logical structures
     */
    public boolean containsCondition(Condition condition) {
        ensureUserLogicalExists();
        
        // Check user conditions
        if (userLogicalCondition.contains(condition)) {
            return true;
        }
        
        // Check plugin conditions
        return pluginCondition != null && pluginCondition.contains(condition);
    }

   

    /**
     * Adds a condition to the specified logical condition, or to the user root if none specified
     */
    public void addConditionToLogical(Condition condition, LogicalCondition targetLogical) {
        ensureUserLogicalExists();
        // find if the user logical condition contains the target logical condition
        if (  targetLogical != userLogicalCondition && (targetLogical != null && !userLogicalCondition.contains(targetLogical))) {
            log.warn("Target logical condition not found in user logical structure");
            return;
        }
        // check if condition already exists in logical structure
        if (targetLogical != null && targetLogical.contains(condition)) {
            log.warn("Condition already exists in logical structure");
            return;
        }
        // If no target specified, add to user root
        if (targetLogical == null) {
            userLogicalCondition.addCondition(condition);
            return;
        }
        
        // Otherwise, add to the specified logical
        targetLogical.addCondition(condition);
    }

    /**
     * Adds a condition to the user logical root
     */
    public void addUserCondition(Condition condition) {
        addConditionToLogical(condition, userLogicalCondition);
    }

    /**
     * Removes a condition from any location in the logical structure
     */
    public boolean removeCondition(Condition condition) {
        ensureUserLogicalExists();
        
        // Don't allow removing plugin conditions
        if (isPluginDefinedCondition(condition)) {
            log.warn("Attempted to remove a plugin-defined condition");
            return false;
        }
        
        // Remove from user logical structure
        if (userLogicalCondition.removeCondition(condition)) {
            return true;
        }
        
        log.warn("Condition not found in any logical structure");
        return false;
    }

    

    /**
     * Gets the root logical condition that should be used for the current UI operation
     */
    public LogicalCondition getFullLogicalCondition() {
        // First check if there are plugin conditions
        if (pluginCondition != null && !pluginCondition.getConditions().isEmpty()) {
            // Need to combine user and plugin conditions with AND logic
            AndCondition combinedRoot = new AndCondition();
            
            // Add user logical if it has conditions
            if (userLogicalCondition != null && !userLogicalCondition.getConditions().isEmpty()) {
                combinedRoot.addCondition(userLogicalCondition);
                // Add plugin logical
                combinedRoot.addCondition(pluginCondition);
                return combinedRoot;
            }else {
                // If no user conditions, just return plugin condition
                return pluginCondition;
            }                                                
        }
        
        // If no plugin conditions, just return user logical
        return userLogicalCondition;
    }
    public LogicalCondition getFullLogicalUserCondition() {
        return userLogicalCondition;
    }
    public LogicalCondition getFullLogicalPluginCondition() {
        return pluginCondition;
    }

    /**
     * Checks if a condition is a SingleTriggerTimeCondition
     * 
     * @param condition The condition to check
     * @return true if the condition is a SingleTriggerTimeCondition
     */
    private boolean isSingleTriggerCondition(Condition condition) {
        return condition instanceof SingleTriggerTimeCondition;
    }
    public List<SingleTriggerTimeCondition> getTriggeredOneTimeConditions(){
        List<SingleTriggerTimeCondition> result = new ArrayList<>();
        for (Condition condition : userLogicalCondition.getConditions()) {
            if (isSingleTriggerCondition(condition)) {
                SingleTriggerTimeCondition singleTrigger = (SingleTriggerTimeCondition) condition;
                if (singleTrigger.canTriggerAgain()) {
                    result.add(singleTrigger);
                }
            }
        }
        if (pluginCondition != null) {
            for (Condition condition : pluginCondition.getConditions()) {
                if (isSingleTriggerCondition(condition)) {
                    SingleTriggerTimeCondition singleTrigger = (SingleTriggerTimeCondition) condition;
                    if (singleTrigger.canTriggerAgain()) {
                        result.add(singleTrigger);
                    }
                }
            }
        }
        return result;
    }
    /**
     * Checks if this condition manager contains any SingleTriggerTimeCondition that
     * can no longer trigger (has already triggered)
     * 
     * @return true if at least one single-trigger condition has already triggered
     */
    public boolean hasTriggeredOneTimeConditions() {
        // Check user conditions first
        for (Condition condition : getUserLogicalCondition().getConditions()) {
            if (isSingleTriggerCondition(condition)) {
                SingleTriggerTimeCondition singleTrigger = (SingleTriggerTimeCondition) condition;
                if (!singleTrigger.canTriggerAgain()) {
                    return true;
                }
            }
        }
        
        // Then check plugin conditions if present
        if (pluginCondition != null) {
            for (Condition condition : pluginCondition.getConditions()) {
                if (isSingleTriggerCondition(condition)) {
                    SingleTriggerTimeCondition singleTrigger = (SingleTriggerTimeCondition) condition;
                    if (!singleTrigger.canTriggerAgain()) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }

     /**
     * Checks if the logical structure cannot trigger again due to triggered one-time conditions.
     * Considers the nested AND/OR condition structure to determine if future triggering is possible.
     * 
     * @return true if the structure cannot trigger again due to one-time conditions
     */
    public boolean cannotTriggerDueToOneTimeConditions() {
        // If there are no one-time conditions, the structure can always trigger again
        if (!hasAnyOneTimeConditions()) {
            return false;
        }

        // Start evaluation at the root of the condition tree
        return !canLogicalStructureTriggerAgain(getFullLogicalCondition());
    }

    /**
     * Recursively evaluates if a logical structure can trigger again based on one-time conditions.
     * 
     * @param logical The logical condition to evaluate
     * @return true if the logical structure can trigger again, false otherwise
     */
    private boolean canLogicalStructureTriggerAgain(LogicalCondition logical) {
        if (logical instanceof AndCondition) {
            // For AND logic, if any direct child one-time condition has triggered,
            // the entire AND branch cannot trigger again
            for (Condition condition : logical.getConditions()) {
                if (condition instanceof TimeCondition) {
                    TimeCondition timeCondition = (TimeCondition) condition;
                    if (timeCondition.canTriggerAgain()) {                        
                        return false;
                    }
                }             
                else if (condition instanceof ResourceCondition) {
                    ResourceCondition resourceCondition = (ResourceCondition) condition;
                    
                }
                else if (condition instanceof LogicalCondition) {
                    // Recursively check nested logic
                    if (!canLogicalStructureTriggerAgain((LogicalCondition) condition)) {
                        // If a nested branch can't trigger, this AND branch can't trigger
                        return false;
                    }
                }
            }
            // If we get here, all branches can still trigger
            return true;
        } else if (logical instanceof OrCondition) {
            // For OR logic, if any one-time condition hasn't triggered yet,
            // the OR branch can still trigger
            boolean anyCanTrigger = false;
            
            for (Condition child : logical.getConditions()) {
                if (child instanceof TimeCondition) {
                    TimeCondition singleTrigger = (TimeCondition) child;
                    if (!singleTrigger.hasTriggered()) {
                        // Found an untriggered one-time condition, so this branch can trigger
                        return true;
                    }
                } else if (child instanceof LogicalCondition) {
                    // Recursively check nested logic
                    if (canLogicalStructureTriggerAgain((LogicalCondition) child)) {
                        // If a nested branch can trigger, this OR branch can trigger
                        return true;
                    }
                } else {
                    // Regular non-one-time conditions can always trigger
                    anyCanTrigger = true;
                }
            }
            
            // If there are no one-time conditions in this OR, it can trigger if it has any conditions
            return anyCanTrigger;
        } else {
            // For any other logical condition type (e.g., NOT), assume it can trigger
            return true;
        }
    }
    /**
     * Validates if the current condition structure can be triggered again
     * based on the status of one-time conditions in the logical structure.
     * 
     * @return true if the condition structure can be triggered again
     */
    public boolean canTriggerAgain() {
        return !cannotTriggerDueToOneTimeConditions();
    }

    /**
     * Checks if this condition manager contains any SingleTriggerTimeConditions
     * 
     * @return true if at least one single-trigger condition exists
     */
    public boolean hasAnyOneTimeConditions() {
        // Check user conditions
        for (Condition condition : getUserLogicalCondition().getConditions()) {
            if (isSingleTriggerCondition(condition)) {
                return true;
            }
        }
        
        // Check plugin conditions if present
        if (pluginCondition != null) {
            for (Condition condition : pluginCondition.getConditions()) {
                if (isSingleTriggerCondition(condition)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    /**
     * Calculates overall progress percentage across all conditions.
     * This respects the logical structure of conditions.
     * Returns 0 if progress cannot be determined.
     */
    private double getFullRootConditionProgress() {
        // If there are no conditions, no progress to report -> nothing can be satisfied
        if ( getConditions().isEmpty()) {
            return 0.0;
        }
        
        // If using logical root condition, respect its logical structure
        LogicalCondition rootLogical = getFullLogicalCondition();
        if (rootLogical != null) {
            return rootLogical.getProgressPercentage();
        }
        
        // Fallback for direct condition list: calculate based on AND/OR logic
        boolean requireAll = requiresAll();
        List<Condition> conditions = getConditions();
        
        if (requireAll) {
            // For AND logic, use the minimum progress (weakest link)
            return conditions.stream()
                .mapToDouble(Condition::getProgressPercentage)
                .min()
                .orElse(0.0);
        } else {
            // For OR logic, use the maximum progress (strongest link)
            return conditions.stream()
                .mapToDouble(Condition::getProgressPercentage)
                .max()
                .orElse(0.0);
        }
    }
    /**
     * Gets the overall condition progress
     * Integrates both standard progress and single-trigger time conditions
     */
    public double getFullConditionProgress() {
        // First check for regular condition progress
        double stopProgress = getFullRootConditionProgress();
        
        // Then check if we have any single-trigger conditions
        boolean hasOneTime = hasAnyOneTimeConditions();
        if (hasOneTime) {
            // If all one-time conditions have triggered, return 100%
            if (canTriggerAgain()) {
                return 100.0;
            }
            
            // If no standard progress but we have one-time conditions that haven't
            // all triggered, return progress based on closest one-time condition
            if (stopProgress == 0.0) {
                return calculateClosestOneTimeProgress();
            }
        }
        
        // Return the standard progress
        return stopProgress;
    }

    /**
     * Calculates progress based on the closest one-time condition to triggering
     */
    private double calculateClosestOneTimeProgress() {
        // Find the single-trigger condition that's closest to triggering
        double maxProgress = 0.0;
        
        for (Condition condition : getConditions()) {
            if (condition instanceof SingleTriggerTimeCondition) {
                SingleTriggerTimeCondition singleTrigger = (SingleTriggerTimeCondition) condition;
                if (!singleTrigger.hasTriggered()) {
                    double progress = singleTrigger.getProgressPercentage();
                    maxProgress = Math.max(maxProgress, progress);
                }
            }
        }
        
        return maxProgress;
    }
    /**
     * Gets the next time any condition in the structure will trigger.
     * This recursively examines the logical condition tree and finds the earliest trigger time.
     * If conditions are already satisfied, returns the most recent time in the past.
     * 
     * @return Optional containing the earliest next trigger time, or empty if none available
     */
    public Optional<ZonedDateTime> getCurrentTriggerTime() {
        // Check if conditions are already met
        boolean conditionsMet = areConditionsMet();
        if (conditionsMet) {
            log.debug("Conditions already met, searching for most recent trigger time in the past");
            
            // Find the most recent trigger time in the past from all satisfied conditions
            ZonedDateTime mostRecentTriggerTime = null;
            ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
            
            // Recursively scan all conditions that are satisfied
            for (Condition condition : getConditions()) {
                if (condition.isSatisfied()) {
                    Optional<ZonedDateTime> conditionTrigger = condition.getCurrentTriggerTime();
                    if (conditionTrigger.isPresent()) {
                        ZonedDateTime triggerTime = conditionTrigger.get();
                        
                        // Only consider times in the past
                        if (triggerTime.isBefore(now) || triggerTime.isEqual(now)) {
                            // Keep the most recent time in the past
                            if (mostRecentTriggerTime == null || triggerTime.isAfter(mostRecentTriggerTime)) {
                                mostRecentTriggerTime = triggerTime;
                                log.debug("Found more recent past trigger time: {}", mostRecentTriggerTime);
                            }
                        }
                    }
                }
            }
            
            // If we found a trigger time from satisfied conditions, return it
            if (mostRecentTriggerTime != null) {
                log.debug("Selected most recent past trigger time: {}", mostRecentTriggerTime);
                return Optional.of(mostRecentTriggerTime);
            }
            
            // If no trigger times found from satisfied conditions, default to immediate past
            ZonedDateTime immediateTime = now.minusSeconds(1);
            log.debug("No past trigger times found from satisfied conditions, returning immediate past time: {}", immediateTime);
            return Optional.of(immediateTime);
        }
        
        // Otherwise proceed with normal logic for finding next trigger time
        log.debug("Conditions not yet met, searching for next trigger time in logical structure");
        Optional<ZonedDateTime> nextTime = getCurrentTriggerTimeForLogical(getFullLogicalCondition());
        
        if (nextTime.isPresent()) {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
            ZonedDateTime triggerTime = nextTime.get();
            
            if (triggerTime.isBefore(now)) {
                log.debug("Found trigger time {} is in the past compared to now {}", 
                    triggerTime, now);
                
                // If trigger time is in the past but conditions aren't met,
                // this might indicate a condition that needs resetting
                if (!conditionsMet) {
                    log.debug("Trigger time in past but conditions not met - may need reset");
                }
            } else {
                log.debug("Found future trigger time: {}", triggerTime);
            }
        } else {
            log.debug("No trigger time found in condition structure");
        }
        
        return nextTime;
    }

    /**
     * Recursively finds the appropriate trigger time within a logical condition.
     * - For conditions not yet met: finds the earliest future trigger time
     * - For conditions already met: finds the most recent past trigger time
     * 
     * @param logical The logical condition to examine
     * @return Optional containing the appropriate trigger time, or empty if none available
     */
    private Optional<ZonedDateTime> getCurrentTriggerTimeForLogical(LogicalCondition logical) {
        if (logical == null || logical.getConditions().isEmpty()) {
            log.debug("Logical condition is null or empty, no trigger time available");
            return Optional.empty();
        }
        
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        
        // If the logical condition is already satisfied, find most recent past trigger time
        if (logical.isSatisfied()) {
            ZonedDateTime mostRecentTriggerTime = null;
            
            for (Condition condition : logical.getConditions()) {
                if (condition.isSatisfied()) {
                    log.debug("Checking past trigger time for satisfied condition: {}", condition.getDescription());
                    Optional<ZonedDateTime> triggerTime;
                    
                    if (condition instanceof LogicalCondition) {
                        // Recursively check nested logical conditions
                        triggerTime = getCurrentTriggerTimeForLogical((LogicalCondition) condition);
                    } else {
                        // Get trigger time from individual condition
                        triggerTime = condition.getCurrentTriggerTime();
                    }
                    
                    if (triggerTime.isPresent()) {
                        ZonedDateTime time = triggerTime.get();
                        // Only consider times in the past
                        if (time.isBefore(now) || time.isEqual(now)) {
                            // Keep the most recent time in the past
                            if (mostRecentTriggerTime == null || time.isAfter(mostRecentTriggerTime)) {
                                mostRecentTriggerTime = time;
                                log.debug("Found more recent past trigger time: {}", mostRecentTriggerTime);
                            }
                        }
                    }
                }
            }
            
            if (mostRecentTriggerTime != null) {
                return Optional.of(mostRecentTriggerTime);
            }
        }
        
        // If not satisfied, find earliest future trigger time (original behavior)
        ZonedDateTime earliestTrigger = null;
        
        for (Condition condition : logical.getConditions()) {
            log.debug("Checking next trigger time for condition: {}", condition.getDescription());
            Optional<ZonedDateTime> nextTrigger;
            
            if (condition instanceof LogicalCondition) {
                // Recursively check nested logical conditions
                log.debug("Recursing into nested logical condition");
                nextTrigger = getCurrentTriggerTimeForLogical((LogicalCondition) condition);
            } else {
                // Get trigger time from individual condition
                nextTrigger = condition.getCurrentTriggerTime();
                log.debug("Condition {} trigger time: {}", 
                    condition.getDescription(), 
                    nextTrigger.isPresent() ? nextTrigger.get() : "none");
            }
            
            // Update earliest trigger if this one is earlier
            if (nextTrigger.isPresent()) {
                ZonedDateTime triggerTime = nextTrigger.get();
                if (earliestTrigger == null || triggerTime.isBefore(earliestTrigger)) {
                    log.debug("Found earlier trigger time: {}", triggerTime);
                    earliestTrigger = triggerTime;
                }
            }
        }
        
        if (earliestTrigger != null) {
            log.debug("Earliest trigger time for logical condition: {}", earliestTrigger);
            return Optional.of(earliestTrigger);
        } else {
            log.debug("No trigger times found in logical condition");
            return Optional.empty();
        }
    }

    /**
     * Gets the next time any condition in the structure will trigger.
     * This recursively examines the logical condition tree and finds the earliest trigger time.
     * 
     * @return Optional containing the earliest next trigger time, or empty if none available
     */
    public Optional<ZonedDateTime> getCurrentTriggerTimeBasedOnUserConditions() {
        // Start at the root of the condition tree
        return getCurrentTriggerTimeForLogical(getFullLogicalUserCondition());
    }
    /**
     * Determines the next time a plugin should be triggered based on the plugin's set conditions.
     * This method evaluates the full logical condition tree for the plugin to calculate
     * when the next condition-based execution should occur.
     *
     * @return An Optional containing the ZonedDateTime of the next trigger time if one exists,
     *         or an empty Optional if no future trigger time can be determined
     */
    public Optional<ZonedDateTime> getCurrentTriggerTimeBasedOnPluginConditions() {
        // Start at the root of the condition tree
        return getCurrentTriggerTimeForLogical(getFullLogicalPluginCondition());
    }

    /**
     * Gets the duration until the next condition trigger.
     * For conditions already satisfied, returns Duration.ZERO.
     * 
     * @return Optional containing the duration until next trigger, or empty if none available
     */
    public Optional<Duration> getDurationUntilNextTrigger() {
        // If conditions are already met, return zero duration
        if (areConditionsMet()) {
            return Optional.of(Duration.ZERO);
        }
        
        Optional<ZonedDateTime> nextTrigger = getCurrentTriggerTime();
        if (nextTrigger.isPresent()) {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
            ZonedDateTime triggerTime = nextTrigger.get();
            
            // If trigger time is in the future, return the duration
            if (triggerTime.isAfter(now)) {
                return Optional.of(Duration.between(now, triggerTime));
            }
            
            // If trigger time is in the past but conditions aren't met,
            // this indicates a condition that needs resetting
            log.debug("Trigger time in past but conditions not met - returning zero duration");
            return Optional.of(Duration.ZERO);
        }
        return Optional.empty();
    }

    /**
     * Formats the next trigger time as a human-readable string.
     * 
     * @return A string representing when the next condition will trigger, or "No upcoming triggers" if none
     */
    public String getCurrentTriggerTimeString() {
        Optional<ZonedDateTime> nextTrigger = getCurrentTriggerTime();
        if (nextTrigger.isPresent()) {
            ZonedDateTime triggerTime = nextTrigger.get();
            ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
            
            // Format nicely depending on how far in the future
            Duration timeUntil = Duration.between(now, triggerTime);
            long seconds = timeUntil.getSeconds();
            
            if (seconds < 0) {
                return "Already triggered";
            } else if (seconds < 60) {
                return String.format("Triggers in %d seconds", seconds);
            } else if (seconds < 3600) {
                return String.format("Triggers in %d minutes %d seconds", 
                        seconds / 60, seconds % 60);
            } else if (seconds < 86400) { // Less than a day
                return String.format("Triggers in %d hours %d minutes", 
                        seconds / 3600, (seconds % 3600) / 60);
            } else {
                // More than a day away, use date format
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d 'at' HH:mm");
                return "Triggers on " + triggerTime.format(formatter);
            }
        }
        
        return "No upcoming triggers";
    }

    /**
     * Gets the overall progress percentage toward the next trigger time.
     * For logical conditions, this respects the logical structure.
     * 
     * @return Progress percentage (0-100)
     */
    public double getProgressTowardNextTrigger() {
        LogicalCondition rootLogical = getFullLogicalCondition();
        
        if (rootLogical instanceof AndCondition) {
            // For AND logic, use the minimum progress (we're only as close as our furthest condition)
            return rootLogical.getConditions().stream()
                    .mapToDouble(Condition::getProgressPercentage)
                    .min()
                    .orElse(0.0);
        } else {
            // For OR logic, use the maximum progress (we're as close as our closest condition)
            return rootLogical.getConditions().stream()
                    .mapToDouble(Condition::getProgressPercentage)
                    .max()
                    .orElse(0.0);
        }
    }
    @Subscribe(priority = -1)
    public void onGameStateChanged(GameStateChanged gameStateChanged){
        for (Condition condition : getConditions( )) {
            try {
                condition.onGameStateChanged(gameStateChanged);
            } catch (Exception e) {
                log.error("Error in condition {} during GameStateChanged event: {}", 
                    condition.getDescription(), e.getMessage(), e);
            }
        }        
    }

    @Subscribe(priority = -1)
    public void onStatChanged(StatChanged event) {
      
        for (Condition condition : getConditions( )) {
            try {
                condition.onStatChanged(event);
            } catch (Exception e) {
                log.error("Error in condition {} during StatChanged event: {}", 
                    condition.getDescription(), e.getMessage(), e);
            }
        }        
    }
    
    @Subscribe(priority = -1)
    public void onItemContainerChanged(ItemContainerChanged event) {
         // Propagate event to all conditions
         
         for (Condition condition : getConditions( )) {
            try {
                condition.onItemContainerChanged(event);
            } catch (Exception e) {
                log.error("Error in condition {} during ItemContainerChanged event: {}", 
                    condition.getDescription(), e.getMessage(), e);
            }
        }       
    }
    @Subscribe(priority = -1)
    public void onGameTick(GameTick gameTick) {
        // Propagate event to all conditions
        
        for (Condition condition : getConditions( )) {
            try {
                condition.onGameTick(gameTick);
            } catch (Exception e) {
                log.error("Error in condition {} during GameTick event: {}", 
                    condition.getDescription(), e.getMessage(), e);
            }
        }      
    }

    @Subscribe(priority = -1)
    public void onGroundObjectSpawned(GroundObjectSpawned event) {
        // Propagate event to all conditions
        for (Condition condition : getConditions( )) {
            try {
                condition.onGroundObjectSpawned(event);
            } catch (Exception e) {
                log.error("Error in condition {} during GroundItemSpawned event: {}", 
                    condition.getDescription(), e.getMessage(), e);
            }
        }
        if (pluginCondition != null) {
            try {
                pluginCondition.onGroundObjectSpawned(event);
            } catch (Exception e) {
                log.error("Error in plugin condition during GroundItemSpawned event: {}", 
                    e.getMessage(), e);
            }
        }
    }

    @Subscribe(priority = -1)
    public void onGroundObjectDespawned(GroundObjectDespawned event) {
        for (Condition condition : getConditions( )) {
            try {
                condition.onGroundObjectDespawned(event);
            } catch (Exception e) {
                log.error("Error in condition {} during GroundItemDespawned event: {}", 
                    condition.getDescription(), e.getMessage(), e);
            }
        }       
    }

    @Subscribe(priority = -1)
    public void onMenuOptionClicked(MenuOptionClicked event) {
        for (Condition condition : getConditions( )) {
            try {
                condition.onMenuOptionClicked(event);
            } catch (Exception e) {
                log.error("Error in condition {} during MenuOptionClicked event: {}", 
                    condition.getDescription(), e.getMessage(), e);
            }
        }        
    }

    @Subscribe(priority = -1)
    public void onChatMessage(ChatMessage event) {
        for (Condition condition : getConditions( )) {
            try {
                condition.onChatMessage(event);
            } catch (Exception e) {
                log.error("Error in condition {} during ChatMessage event: {}", 
                    condition.getDescription(), e.getMessage(), e);
            }
        }        
    }

    @Subscribe(priority = -1)
    public void onHitsplatApplied(HitsplatApplied event) {
        for (Condition condition : getConditions( )) {
            try {
                condition.onHitsplatApplied(event);
            } catch (Exception e) {
                log.error("Error in condition {} during HitsplatApplied event: {}", 
                    condition.getDescription(), e.getMessage(), e);
            }
        }
       
    }
    @Subscribe(priority = -1)
	public void onVarbitChanged(VarbitChanged event)
	{
		for (Condition condition :getConditions( )) {
            try {
                condition.onVarbitChanged(event);
            } catch (Exception e) {
                log.error("Error in condition {} during VarbitChanged event: {}", 
                    condition.getDescription(), e.getMessage(), e);
            }
        }        
	}
    @Subscribe(priority = -1)
    void onNpcChanged(NpcChanged event){
        for (Condition condition :getConditions( )) {
            try {
                condition.onNpcChanged(event);
            } catch (Exception e) {
                log.error("Error in condition {} during NpcChanged event: {}", 
                    condition.getDescription(), e.getMessage(), e);
            }
        }        
    }
    @Subscribe(priority = -1)
    void onNpcSpawned(NpcSpawned npcSpawned){
        for (Condition condition : getConditions( )) {
            try {
                condition.onNpcSpawned(npcSpawned);
            } catch (Exception e) {
                log.error("Error in condition {} during NpcSpawned event: {}", 
                    condition.getDescription(), e.getMessage(), e);
            }
        }        
        
    }
    @Subscribe(priority = -1)
    void onNpcDespawned(NpcDespawned npcDespawned){
        for (Condition condition : getConditions( )) {
            try {
                condition.onNpcDespawned(npcDespawned);
            } catch (Exception e) {
                log.error("Error in condition {} during NpcDespawned event: {}", 
                    condition.getDescription(), e.getMessage(), e);
            }
        }        
    }
    @Subscribe(priority = -1)
    void onInteractingChanged(InteractingChanged event){
        for (Condition condition : getConditions( )) {
            try {
                condition.onInteractingChanged(event);
            } catch (Exception e) {
                log.error("Error in condition {} during InteractingChanged event: {}", 
                    condition.getDescription(), e.getMessage(), e);
            }
        }       
    }
    @Subscribe(priority = -1)
    void onItemSpawned(ItemSpawned event){        
        List<Condition> allConditions = getConditions();            
        // Proceed with normal processing
        for (Condition condition : allConditions) {            
            try {                
                condition.onItemSpawned(event);
            } catch (Exception e) {
                log.error("Error in condition {} during ItemSpawned event: {}", 
                    condition.getDescription(), e.getMessage(), e);
            }
        }       
    }
    @Subscribe(priority = -1)
    void onItemDespawned(ItemDespawned event){
        for (Condition condition :getConditions( )) {
            try {
                condition.onItemDespawned(event);
            } catch (Exception e) {
                log.error("Error in condition {} during ItemDespawned event: {}", 
                    condition.getDescription(), e.getMessage(), e);
            }
        }       
    }
    @Subscribe(priority = -1)
    void onAnimationChanged(AnimationChanged event) {
        for (Condition condition :getConditions( )) {
            try {
                condition.onAnimationChanged(event);
            } catch (Exception e) {
                log.error("Error in condition {} during AnimationChanged event: {}", 
                    condition.getDescription(), e.getMessage(), e);
            }
        }       
    }

    /**
     * Finds the logical condition that contains the given condition
     * 
     * @param targetCondition The condition to find
     * @return The logical condition containing it, or null if not found
     */
    public LogicalCondition findContainingLogical(Condition targetCondition) {
        // First check if it's in the plugin condition
        if (pluginCondition != null && findInLogical(pluginCondition, targetCondition) != null) {
            return findInLogical(pluginCondition, targetCondition);
        }
        
        // Then check user logical condition
        if (userLogicalCondition != null) {
            LogicalCondition result = findInLogical(userLogicalCondition, targetCondition);
            if (result != null) {
                return result;
            }
        }
        
        // Try root logical condition as a last resort
        return userLogicalCondition;
    }
    
    /**
     * Recursively searches for a condition within a logical condition
     */
    private LogicalCondition findInLogical(LogicalCondition logical, Condition targetCondition) {
        // Check if the condition is directly in this logical
        if (logical.getConditions().contains(targetCondition)) {
            return logical;
        }
        
        // Check nested logical conditions
        for (Condition condition : logical.getConditions()) {
            if (condition instanceof LogicalCondition) {
                LogicalCondition result = findInLogical((LogicalCondition) condition, targetCondition);
                if (result != null) {
                    return result;
                }
            }
        }
        
        return null;
    }
}