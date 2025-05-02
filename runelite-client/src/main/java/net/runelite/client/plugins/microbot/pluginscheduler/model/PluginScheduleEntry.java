package net.runelite.client.plugins.microbot.pluginscheduler.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.pluginscheduler.api.ConditionProvider;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.Condition;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.ConditionManager;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.logical.LogicalCondition;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.logical.OrCondition;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.time.IntervalCondition;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.time.SingleTriggerTimeCondition;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.time.TimeCondition;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.time.TimeWindowCondition;
import net.runelite.client.plugins.microbot.pluginscheduler.event.PluginScheduleEntrySoftStopEvent;

import net.runelite.client.plugins.microbot.pluginscheduler.serialization.ScheduledSerializer;



import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


import java.util.List;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Data
@AllArgsConstructor
@Getter
@Slf4j
public class PluginScheduleEntry {
    private transient Plugin plugin;
    private String name;    
    private boolean enabled;
    private boolean hasStarted = false; // Flag to indicate if the plugin has started

    // New fields for tracking stop reason
    private String lastStopReason;
    private boolean lastRunSuccessful;
    private StopReason stopReasonType = StopReason.NONE;
    
    /**
    * Enumeration of reasons why a plugin might stop
    */
    public enum StopReason {
        NONE,
        CONDITIONS_MET,
        MANUAL_STOP,
        PLUGIN_FINISHED,
        ERROR,
        SCHEDULED_STOP,
        HARD_STOP_TIMEOUT
    }
 
    private ZonedDateTime lastRunTime; // When the plugin last ran    

    private String cleanName;
    final private ConditionManager stopConditionManager;
    final private ConditionManager startConditionManager;
    private boolean stopInitiated = false;
    private boolean finished  = false; // Flag to indicate if the plugin has finished its task

    private boolean allowRandomScheduling = true; // Whether this plugin can be randomly scheduled
    private int runCount = 0; // Track how many times this plugin has been run

    
    private ZonedDateTime stopInitiatedTime; // When the first stop was attempted
    private ZonedDateTime lastStopAttemptTime; // When the last stop attempt was made
    private Duration softStopRetryInterval = Duration.ofSeconds(30); // Default 30 seconds between retries
    private Duration hardStopTimeout = Duration.ofMinutes(2); // Default 2 Minutes before hard stop

    
    private transient Thread stopMonitorThread;
    private transient volatile boolean isMonitoringStop = false;

    // Static formatter for time display
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");    
    private int priority = 0; // Higher numbers = higher priority
    private boolean isDefault = false; // Flag to indicate if this is a default plugin
    public PluginScheduleEntry(String pluginName, String duration, boolean enabled, boolean allowRandomScheduling) {
        this(pluginName, parseDuration(duration), enabled, allowRandomScheduling);
    }
    private TimeCondition mainTimeStartCondition;
    private static Duration parseDuration(String duration) {
        // If duration is specified, parse it
        if (duration != null && !duration.isEmpty()) {
            try {
                String[] parts = duration.split(":");
                if (parts.length == 2) {
                    int hours = Integer.parseInt(parts[0]);
                    int minutes = Integer.parseInt(parts[1]);
                    return Duration.ofHours(hours).plusMinutes(minutes);                    
                }
            } catch (Exception e) {
                // Invalid duration format, no condition added
                throw new IllegalArgumentException("Invalid duration format: " + duration);
            }
        }
        return null;
    }
   
    public PluginScheduleEntry(String pluginName, Duration interval, boolean enabled, boolean allowRandomScheduling) { //allowRandomScheduling .>allows soft start
        this(pluginName, new IntervalCondition(interval), enabled, allowRandomScheduling);                
    }
    public PluginScheduleEntry(String pluginName, TimeCondition startingCondition, boolean enabled, boolean allowRandomScheduling) {
        this.name = pluginName;        
        this.enabled = enabled;
        this.allowRandomScheduling = allowRandomScheduling;
        this.cleanName = pluginName.replaceAll("<html>|</html>", "")
                .replaceAll("<[^>]*>([^<]*)</[^>]*>", "$1")
                .replaceAll("<[^>]*>", "");

        this.stopConditionManager = new ConditionManager();
        this.startConditionManager = new ConditionManager();
        
        // Check if this is a default/1-second interval plugin
        boolean isDefaultByScheduleType = false;
        if (startingCondition != null) {
            if (startingCondition instanceof IntervalCondition) {
                IntervalCondition interval = (IntervalCondition) startingCondition;
                if (interval.getInterval().getSeconds() <= 1) {
                    isDefaultByScheduleType = true;
                }
            }
            this.mainTimeStartCondition  = startingCondition;
            startConditionManager.setUserLogicalCondition(new OrCondition(startingCondition));
        }
        
        // If it's a default by schedule type, enforce the default settings
        if (isDefaultByScheduleType) {
            this.isDefault = true;
            this.priority = 0;
        }
        
        registerPluginConditions();
        if (enabled){
            startConditionManager.registerEvents();
        }
    }

    /**
     * Creates a scheduled event with a one-time trigger at a specific time
     * 
     * @param pluginName The plugin name
     * @param triggerTime The time when the plugin should trigger once
     * @param enabled Whether the schedule is enabled
     * @return A new PluginScheduleEntry configured to trigger once at the specified time
     */
    public static PluginScheduleEntry createOneTimeSchedule(String pluginName, ZonedDateTime triggerTime, boolean enabled) {
        SingleTriggerTimeCondition condition = new SingleTriggerTimeCondition(triggerTime);
        PluginScheduleEntry entry = new PluginScheduleEntry(
            pluginName, 
            condition, 
            enabled, 
            false); // One-time events are typically not randomized
        
        return entry;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return; // No change in enabled state
        }
        this.enabled = enabled;
        if (!enabled) {
            stopConditionManager.unregisterEvents();
            startConditionManager.unregisterEvents();
            runCount = 0;
        } else {
            stopConditionManager.registerEvents();
            startConditionManager.registerEvents();
            //log  this object id-> memory hashcode
            log.info("PluginScheduleEntry {} - {} - {} - {} - {}", this.hashCode(), this.name, this.cleanName, this.enabled, this.allowRandomScheduling);
            registerPluginConditions();                        
            this.finished = false; // Reset finished state when re-enabled
            this.setLastStopReason("");
            this.setLastRunSuccessful(false);
            this.setStopReasonType(PluginScheduleEntry.StopReason.NONE);
        }
    }
    public Plugin getPlugin() {
        if (this.plugin == null) {
            this.plugin = Microbot.getPluginManager().getPlugins().stream()
                    .filter(p -> Objects.equals(p.getName(), name))
                    .findFirst()
                    .orElse(null);
        }
        return plugin;
    }

    public boolean start() {
        if (getPlugin() == null) {
            return false;
        }

        try {
            registerPluginStoppingConditions();
            // Log defined conditions when starting
            logStartCondtions();
            logStopConditions();                        
            // Reset stop conditions before starting
            updateStopConditions();
            this.setLastStopReason("");
            this.setLastRunSuccessful(false);
            this.setStopReasonType(PluginScheduleEntry.StopReason.NONE);
            this.finished = false; // Reset finished state when starting
            Microbot.getClientThread().runOnSeperateThread(() -> {
                Plugin plugin = getPlugin();
                if (plugin == null) {
                    log.error("Plugin '{}' not found -> can't start plugin", name);
                    return false;
                }
                Microbot.startPlugin(plugin);
                return false;
            });
            stopInitiated = false;
            hasStarted = true;
            
            // Register/unregister appropriate event handlers
            stopConditionManager.registerEvents();
            startConditionManager.unregisterEvents();            
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void softStop(boolean successfulRun) {
        if (getPlugin() == null) {
            return;
        }

        try {
            // Reset start conditions
            startConditionManager.reset();
            startConditionManager.registerEvents();
            stopConditionManager.unregisterEvents();
            
            Microbot.getClientThread().runOnSeperateThread(() -> {
                ZonedDateTime current_time = ZonedDateTime.now(ZoneId.systemDefault());
                Microbot.getEventBus().post(new PluginScheduleEntrySoftStopEvent(plugin, current_time));
                return false;                
            });
            
            stopInitiated = true;
            stopInitiatedTime = ZonedDateTime.now();
            lastStopAttemptTime = ZonedDateTime.now();
            
            // Start monitoring for successful stop
            startStopMonitoringThread(successfulRun);
            
            if (getPlugin() instanceof ConditionProvider) {
                log.info("Unregistering stopping conditions for plugin '{}'", name);
            }
            return;
        } catch (Exception e) {
            return;
        }
    }

    public void hardStop(boolean successfulRun) {
        if (getPlugin() == null) {
            return;
        }

        try {
            
            
            Microbot.getClientThread().runOnSeperateThread(() -> {
                log.info("Hard stopping plugin '{}'", name);
                Plugin stopPlugin = Microbot.getPlugin(plugin.getClass().getName());
                Microbot.stopPlugin(stopPlugin);
                return false;
            });
            stopInitiated = true;
            stopInitiatedTime = ZonedDateTime.now();
            lastStopAttemptTime = ZonedDateTime.now();
            // Start monitoring for successful stop
            startStopMonitoringThread(successfulRun);
            
            return;
        } catch (Exception e) {
            return;
        }
    }

     /**
     * Starts a monitoring thread that tracks the stopping process of a plugin.
     * <p>
     * This method creates a daemon thread that periodically checks if a plugin
     * that is in the process of stopping has completed its shutdown. When the plugin
     * successfully stops, this method updates the next scheduled run time and clears
     * all stopping-related state flags.
     * <p>
     * The monitoring thread will only be started if one is not already running
     * (controlled by the isMonitoringStop flag). It checks the plugin's running state
     * every 500ms until the plugin stops or monitoring is canceled.
     * <p>
     * The thread is created as a daemon thread to prevent it from blocking JVM shutdown.
     */
    private void startStopMonitoringThread(boolean successfulRun) {
        // Don't start a new thread if one is already running
        if (isMonitoringStop) {
            return;
        }
        
        isMonitoringStop = true;
        
        stopMonitorThread = new Thread(() -> {
            try {
                log.info("Stop monitoring thread started for plugin '{}'", name);
                
                // Keep checking until the stop completes or is abandoned
                while (stopInitiated && isMonitoringStop) {
                    // Check if plugin has stopped running
                    if (!isRunning()) {
                        
                        log.info("\nPlugin '{}' has successfully stopped - updating state - successfulRun {}", name, successfulRun);
                        
                        // Update lastRunTime and start conditions for next run
                        if (successfulRun) {
                            updateStartConditions();
                            // Increment the run count since we completed a full run
                            incrementRunCount();
                        }
                        
                        
                        
                        finished = false; // Reset finished state
                        // Reset stop state
                        stopInitiated = false;
                        hasStarted = false;
                        stopInitiatedTime = null;
                        lastStopAttemptTime = null;
                        break;
                    }
                    
                    // Check every 500ms to be responsive but not wasteful
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                // Thread was interrupted, just exit
                log.debug("Stop monitoring thread for '{}' was interrupted", name);
            } finally {
                isMonitoringStop = false;
                log.debug("Stop monitoring thread exited for plugin '{}'", name);
            }
        });
        
        stopMonitorThread.setName("StopMonitor-" + name);
        stopMonitorThread.setDaemon(true); // Use daemon thread to not prevent JVM exit
        stopMonitorThread.start();
    }

    /**
     * Stops the monitoring thread if it's running
     */
    private void stopMonitoringThread() {
        if (isMonitoringStop && stopMonitorThread != null) {
            log.info("Stopping monitoring thread for plugin '{}'", name);
            isMonitoringStop = false;
            stopMonitorThread.interrupt();
            stopMonitorThread = null;
        }
    }

    /**
     * Checks if this plugin schedule has any defined stop conditions
     * 
     * @return true if at least one stop condition is defined
     */
    public boolean hasAnyStopConditions() {
        return stopConditionManager != null && 
               !stopConditionManager.getConditions().isEmpty();
    }
    
    /**
     * Checks if this plugin has any one-time stop conditions that can only trigger once
     * 
     * @return true if at least one single-trigger condition exists in the stop conditions
     */
    public boolean hasAnyOneTimeStopConditions() {
        return stopConditionManager != null && 
               stopConditionManager.hasAnyOneTimeConditions();
    }
    
    /**
     * Checks if any stop conditions have already triggered and cannot trigger again
     * 
     * @return true if at least one stop condition has triggered and cannot trigger again
     */
    public boolean hasTriggeredOneTimeStopConditions() {
        return stopConditionManager != null && 
               stopConditionManager.hasTriggeredOneTimeConditions();
    }
    
    /**
     * Determines if the stop conditions can trigger again in the future
     * Considers the nested logical structure and one-time conditions
     * 
     * @return true if the stop condition structure can trigger again
     */
    public boolean canStopTriggerAgain() {
        return stopConditionManager != null && 
               stopConditionManager.canTriggerAgain();
    }
    
    /**
     * Gets the next time when any stop condition is expected to trigger
     * 
     * @return Optional containing the next stop trigger time, or empty if none exists
     */
    public Optional<ZonedDateTime> getNextStopTriggerTime() {
        if (stopConditionManager == null) {
            return Optional.empty();
        }
        return stopConditionManager.getCurrentTriggerTime();
    }
    
    /**
     * Gets a human-readable string representing when the next stop condition will trigger
     * 
     * @return String with the time until the next stop trigger, or a message if none exists
     */
    public String getNextStopTriggerTimeString() {
        if (stopConditionManager == null) {
            return "No stop conditions defined";
        }
        return stopConditionManager.getCurrentTriggerTimeString();
    }
    
    /**
     * Checks if the stop conditions are fulfillable based on their structure and state
     * A condition is considered unfulfillable if it contains one-time conditions that
     * have all already triggered in an OR structure, or if any have triggered in an AND structure
     * 
     * @return true if the stop conditions can still be fulfilled
     */
    public boolean hasFullfillableStopConditions() {
        if (!hasAnyStopConditions()) {
            return false;
        }
        
        // If we have any one-time conditions that can't trigger again
        // and the structure is such that it can't satisfy anymore, then it's not fulfillable
        if (hasAnyOneTimeStopConditions() && !canStopTriggerAgain()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Gets the remaining duration until the next stop condition trigger
     * 
     * @return Optional containing the duration until next stop trigger, or empty if none available
     */
    public Optional<Duration> getDurationUntilStopTrigger() {
        if (stopConditionManager == null) {
            return Optional.empty();
        }
        return stopConditionManager.getDurationUntilNextTrigger();
    }
    
       
    

    public boolean isRunning() {
        return getPlugin() != null && Microbot.getPluginManager().isPluginEnabled(plugin) && hasStarted;
    }

    /**
     * Round time to nearest minute (remove seconds and milliseconds)
     */
    private ZonedDateTime roundToMinutes(ZonedDateTime time) {
        return time.withSecond(0).withNano(0);
    }
    private void logStartCondtions() {
        List<Condition> conditionList = startConditionManager.getConditions();
        logConditionInfo(conditionList,"Defined Start Conditions", true);
    }
    private void logStartConditionsWithDetails() {
        List<Condition> conditionList = startConditionManager.getConditions();
        logConditionInfo(conditionList,"Defined Start Conditions", true);
    }

    /**
     * Checks if this plugin schedule has any defined start conditions
     * 
     * @return true if at least one start condition is defined
     */
    public boolean hasAnyStartConditions() {
        return startConditionManager != null && 
               !startConditionManager.getConditions().isEmpty();
    }
    
    /**
     * Checks if this plugin has any one-time start conditions that can only trigger once
     * 
     * @return true if at least one single-trigger condition exists in the start conditions
     */
    public boolean hasAnyOneTimeStartConditions() {
        return startConditionManager != null && 
               startConditionManager.hasAnyOneTimeConditions();
    }
    
    /**
     * Checks if any start conditions have already triggered and cannot trigger again
     * 
     * @return true if at least one start condition has triggered and cannot trigger again
     */
    public boolean hasTriggeredOneTimeStartConditions() {
        return startConditionManager != null && 
               startConditionManager.hasTriggeredOneTimeConditions();
    }
    
    /**
     * Determines if the start conditions can trigger again in the future
     * Considers the nested logical structure and one-time conditions
     * 
     * @return true if the start condition structure can trigger again
     */
    public boolean canStartTriggerAgain() {
        return startConditionManager != null && 
               startConditionManager.canTriggerAgain();
    }
    
    /**
     * Gets the next time when any start condition is expected to trigger
     * 
     * @return Optional containing the next start trigger time, or empty if none exists
     */
    public Optional<ZonedDateTime> getCurrentStartTriggerTime() {
        if (startConditionManager == null) {
            return Optional.empty();
        }
        return startConditionManager.getCurrentTriggerTime();
    }
    
    /**
     * Gets a human-readable string representing when the next start condition will trigger
     * 
     * @return String with the time until the next start trigger, or a message if none exists
     */
    public String getCurrentStartTriggerTimeString() {
        if (startConditionManager == null) {
            return "No start conditions defined";
        }
        return startConditionManager.getCurrentTriggerTimeString();
    }
    
    /**
     * Checks if the start conditions are fulfillable based on their structure and state
     * A condition is considered unfulfillable if it contains one-time conditions that
     * have all already triggered in an OR structure, or if any have triggered in an AND structure
     * 
     * @return true if the start conditions can still be fulfilled
     */
    public boolean hasFullfillableStartConditions() {
        if (!hasAnyStartConditions()) {
            return false;
        }
        
        // If we have any one-time conditions that can't trigger again
        // and the structure is such that it can't satisfy anymore, then it's not fulfillable
        if (hasAnyOneTimeStartConditions() && !canStartTriggerAgain()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Gets the remaining duration until the next start condition trigger
     * 
     * @return Optional containing the duration until next start trigger, or empty if none available
     */
    public Optional<Duration> getDurationUntilStartTrigger() {
        if (startConditionManager == null) {
            return Optional.empty();
        }
        return startConditionManager.getDurationUntilNextTrigger();
    }
    /**
     * Gets a detailed description of the stop conditions status
     * 
     * @return A string with detailed information about stop conditions
     */
    public String getDetailedStopConditionsStatus() {
        if (!hasAnyStopConditions()) {
            return "No stop conditions defined";
        }
        
        StringBuilder sb = new StringBuilder("Stop conditions: ");
        
        // Add logic type
        sb.append(stopConditionManager.requiresAll() ? "ALL must be met" : "ANY can be met");
        
        // Add fulfillability status
        if (!hasFullfillableStopConditions()) {
            sb.append(" (UNFULFILLABLE)");
        }
        
        // Add condition count
        int total = getTotalStopConditionCount();
        int satisfied = getSatisfiedStopConditionCount();
        sb.append(String.format(" - %d/%d conditions met", satisfied, total));
        
        // Add next trigger time if available
        Optional<ZonedDateTime> nextTrigger = getNextStopTriggerTime();
        if (nextTrigger.isPresent()) {
            sb.append(" - Next trigger: ").append(getNextStopTriggerTimeString());
        }
        
        return sb.toString();
    }
    /**
     * Gets a detailed description of the start conditions status
     * 
     * @return A string with detailed information about start conditions
     */
    public String getDetailedStartConditionsStatus() {
        if (!hasAnyStartConditions()) {
            return "No start conditions defined";
        }
        
        StringBuilder sb = new StringBuilder("Start conditions: ");
        
        // Add logic type
        sb.append(startConditionManager.requiresAll() ? "ALL must be met" : "ANY can be met");
        
        // Add fulfillability status
        if (!hasFullfillableStartConditions()) {
            sb.append(" (UNFULFILLABLE)");
        }
        
        // Add condition count and satisfaction status
        int totalStartConditions = startConditionManager.getConditions().size();
        long satisfiedStartConditions = startConditionManager.getConditions().stream()
                .filter(Condition::isSatisfied)
                .count();
        sb.append(String.format(" - %d/%d conditions met", satisfiedStartConditions, totalStartConditions));
        
        // Add next trigger time if available
        Optional<ZonedDateTime> nextTrigger = getCurrentStartTriggerTime();
        if (nextTrigger.isPresent()) {
            sb.append(" - Next trigger: ").append(getCurrentStartTriggerTimeString());
        }
        
        return sb.toString();
    }
    
    /**
     * Determines if the plugin should be started immediately based on its current
     * start condition status
     * 
     * @return true if the plugin should be started immediately
     */
    public boolean shouldStartImmediately() {
        // If no start conditions, don't start automatically
        if (!hasAnyStartConditions()) {
            return false;
        }
        
        // If start conditions are met, start the plugin
        if (startConditionManager.areConditionsMet()) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Logs the defined start conditions with their current states
     */
    private void logDefinedStartConditionWithStates() {
        logStartConditionsWithDetails();
        
        // If the conditions are unfulfillable, log a warning
        if (!hasFullfillableStartConditions()) {
            log.warn("Plugin {} has unfulfillable start conditions - may not start properly", name);
        }
        
        // Log progress percentage
        double progress = startConditionManager.getProgressTowardNextTrigger();
        log.info("Plugin {} start condition progress: {:.2f}%", name, progress);
    }
    
    /**
    * Updates the isDueToRun method to use the diagnostic helper for logging
    */
    public boolean isDueToRun() {
        // Check if we're already running
        if (isRunning()) {
            return false;
        }
        
        // For plugins with start conditions, check if those conditions are met
        if (!hasAnyStartConditions()) {
            log.info("No start conditions defined for plugin '{}'", name);
            return false;
        }
        
        
        
        // Log at appropriate levels
        if (Microbot.isDebug()) {
            // Build comprehensive log info using our diagnostic helper
            String diagnosticInfo = diagnoseStartConditions();
            // In debug mode, log the full detailed diagnostics
            log.debug("\n[isDueToRun] - \n"+diagnosticInfo);
        }
          
        
        // Check if start conditions are met
        return startConditionManager.areConditionsMet();
    }    

    /**
    * Updates the primary time condition for this plugin schedule entry.
    * This method replaces the original time condition that was added when the entry was created,
    * but preserves any additional conditions that might have been added later.
    * 
    * @param newTimeCondition The new time condition to use
    * @return true if a time condition was found and replaced, false otherwise
    */
    public boolean updatePrimaryTimeCondition(TimeCondition newTimeCondition) {
        if (startConditionManager == null || newTimeCondition == null) {
            return false;
        }        
        // First, find the existing time condition. We'll assume the first time condition 
        // we find is the primary one that was added at creation
        TimeCondition existingTimeCondition = this.mainTimeStartCondition;                
        
        // If we found a time condition, replace it
        if (existingTimeCondition != null) {
            Optional<ZonedDateTime> currentTrigDateTime = existingTimeCondition.getCurrentTriggerTime();
            Optional<ZonedDateTime> newTrigDateTime = newTimeCondition.getCurrentTriggerTime();
            log.debug("Replacing time condition {} with {}", 
                    existingTimeCondition.getDescription(), 
                    newTimeCondition.getDescription());
            
            // Check if current condition is a one-second interval (default)
            boolean isDefaultByScheduleType = false;
            if (existingTimeCondition instanceof IntervalCondition) {
                IntervalCondition intervalCondition = (IntervalCondition) existingTimeCondition;
                if (intervalCondition.getInterval().getSeconds() <= 1) {
                    isDefaultByScheduleType = true;
                }
            }
            
            // Check if new condition is a one-second interval (default)
            boolean willBeDefaultByScheduleType = false;
            if (newTimeCondition instanceof IntervalCondition) {
                IntervalCondition intervalCondition = (IntervalCondition) newTimeCondition;
                if (intervalCondition.getInterval().getSeconds() <= 1) {
                    willBeDefaultByScheduleType = true;
                }
            }
            
            // Remove the existing condition and add the new one
            if (startConditionManager.removeCondition(existingTimeCondition)) {
                if (!startConditionManager.containsCondition(newTimeCondition)) {
                    startConditionManager.addUserCondition(newTimeCondition);
                }
                
                // Update default status if needed
                if (willBeDefaultByScheduleType) {
                    this.setDefault(true);
                    this.setPriority(0);
                } else if (isDefaultByScheduleType && !willBeDefaultByScheduleType) {
                    // Only change from default if it was set automatically by condition type
                    this.setDefault(false);
                }                
                
                this.mainTimeStartCondition = newTimeCondition;                                
            }
            if (currentTrigDateTime.isPresent() && newTrigDateTime.isPresent()) {
                // Check if the new trigger time is different from the current one
                if (!currentTrigDateTime.get().equals(newTrigDateTime.get())) {
                    log.info("Updated start time for '{}' to {}", 
                            name, 
                            newTrigDateTime.get().format(DATE_TIME_FORMATTER));
                    // Update the start conditions
                    updateStartConditions();
                } else {
                    log.info("Start time for '{}' remains unchanged", name);
                }
            }else if (!newTrigDateTime.isPresent() && currentTrigDateTime.isPresent()){                
                updateStartConditions();// we have new condition ->  new start time ?
                
            }
        } else {
            // No existing time condition found, just add the new one
            log.info("No existing time condition found, adding new condition: {}", 
                    newTimeCondition.getDescription());
            // Check if the condition already exists before adding it
            if (startConditionManager.containsCondition(newTimeCondition)) {
                log.info("Condition {} already exists in the manager, not adding a duplicate", 
                newTimeCondition.getDescription());
                // Still need to update start conditions in case the existing one needs resetting                                                
            }else{
                startConditionManager.addUserCondition(newTimeCondition);
            }            
            this.mainTimeStartCondition = newTimeCondition;                 
            //updateStartConditions();// we have new condition ->  new start time ?
        }
        // Recalculate any internal state based on the new condition
        //if  (!isRunning()){
            
        //}
        return true;
    }

    /**
     * Update the lastRunTime to now and reset start conditions
     */
    private void updateStartConditions() {
        // Update last run time
        lastRunTime = roundToMinutes(ZonedDateTime.now(ZoneId.systemDefault()));
        
        // Handle time conditions
        if (startConditionManager != null) {
            log.info("\nUpdating start conditions for plugin '{}'", name);
            startConditionManager.reset();
            
            // Reset one-time conditions to prevent repeated triggering
            for (TimeCondition condition : startConditionManager.getTimeConditions()) {
                if (condition instanceof SingleTriggerTimeCondition) {
                    // Mark as triggered so it won't trigger again
                    if (condition.isSatisfied()){
                        ((SingleTriggerTimeCondition) condition).reset();
                        assert condition.isSatisfied() == false;
                    }
                }
                // For interval conditions, no need to reset as they'll naturally calculate
                // their next trigger time
            }
            
            // Update the nextRunTime for legacy compatibility if possible
            Optional<ZonedDateTime> nextTriggerTime = getCurrentStartTriggerTime();
            if (nextTriggerTime.isPresent()) {
                ZonedDateTime nextRunTime = nextTriggerTime.get();
                log.info("Updated next run time for '{}' to {}", 
                        name, 
                        nextRunTime.format(DATE_TIME_FORMATTER));
            } else {
                // No future trigger time found
                if (hasTriggeredOneTimeStartConditions() && !canStartTriggerAgain()) {
                    log.info("One-time conditions for {} triggered, not scheduling next run", name);
                }
            }
        }
    }

    /**
     * Reset stop conditions
     */
    private void updateStopConditions() {
        if (stopConditionManager != null) {
            stopConditionManager.reset();            
            // Log that stop conditions were reset
            log.debug("Reset stop conditions for plugin '{}'", name);
        }
    }

   
    
    /**
     * Get a formatted display of the scheduling interval
     */
    public String getIntervalDisplay() {
        if (!hasAnyStartConditions()) {
            return "No schedule defined";
        }
        
        List<TimeCondition> timeConditions = startConditionManager.getTimeConditions();
        if (timeConditions.isEmpty()) {
            return "Non-time conditions only";
        }
        
        // Check for common condition types
        if (timeConditions.size() == 1) {
            TimeCondition condition = timeConditions.get(0);
            
            if (condition instanceof SingleTriggerTimeCondition) {
                ZonedDateTime triggerTime = ((SingleTriggerTimeCondition) condition).getTargetTime();
                return "Once at " + triggerTime.format(DATE_TIME_FORMATTER);
            } 
            else if (condition instanceof IntervalCondition) {
                Duration interval = ((IntervalCondition) condition).getInterval();
                long hours = interval.toHours();
                long minutes = interval.toMinutes() % 60;
                
                if (hours > 0) {
                    return String.format("Every %d hour%s %s", 
                            hours, 
                            hours > 1 ? "s" : "",
                            minutes > 0 ? minutes + " min" : "");
                } else {
                    return String.format("Every %d minute%s", 
                            minutes, 
                            minutes > 1 ? "s" : "");
                }
            }
            else if (condition instanceof TimeWindowCondition) {
                TimeWindowCondition windowCondition = (TimeWindowCondition) condition;
                LocalTime startTime = windowCondition.getStartTime();
                LocalTime endTime = windowCondition.getEndTime();
                
                return String.format("Between %s and %s daily", 
                        startTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                        endTime.format(DateTimeFormatter.ofPattern("HH:mm")));
            }
        }
        
        // If we have multiple time conditions or other complex scenarios
        return "Complex time schedule";
    }

    /**
     * Get a formatted display of when this plugin will run next
     */
    public String getNextRunDisplay() {
        return getNextRunDisplay(System.currentTimeMillis());
    }

    /**
     * Get a formatted display of when this plugin will run next, including
     * condition information.
     * 
     * @param currentTimeMillis Current system time in milliseconds
     * @return Human-readable description of next run time or condition status
     */
    public String getNextRunDisplay(long currentTimeMillis) {
        if (!enabled) {
            return "Disabled";
        }

        // If plugin is running, show progress or status information
        if (isRunning()) {
            if (!stopConditionManager.getConditions().isEmpty()) {
                double progressPct = getStopConditionProgress();
                if (progressPct > 0 && progressPct < 100) {
                    return String.format("Running (%.1f%% complete)", progressPct);
                }
                return "Running with conditions";
            }
            return "Running";
        }
        
        // Check for start conditions
        if (hasAnyStartConditions()) {
            // Check if we can determine the next trigger time
            Optional<ZonedDateTime> nextTrigger = getCurrentStartTriggerTime();
            if (nextTrigger.isPresent()) {
                ZonedDateTime triggerTime = nextTrigger.get();
                ZonedDateTime currentTime = ZonedDateTime.ofInstant(
                        Instant.ofEpochMilli(currentTimeMillis),
                        ZoneId.systemDefault());
                
                // If it's due to run now
                if (!currentTime.isBefore(triggerTime)) {
                    return "Due to run";
                }
                
                // Calculate time until next run
                Duration timeUntil = Duration.between(currentTime, triggerTime);
                long hours = timeUntil.toHours();
                long minutes = timeUntil.toMinutes() % 60;
                long seconds = timeUntil.getSeconds() % 60;
                
                if (hours > 0) {
                    return String.format("In %dh %dm", hours, minutes);
                } else if (minutes > 0) {
                    return String.format("In %dm %ds", minutes, seconds);
                } else {
                    return String.format("In %ds", seconds);
                }
            } else if (shouldStartImmediately()) {
                return "Due to run";
            } else if (hasTriggeredOneTimeStartConditions() && !canStartTriggerAgain()) {
                return "Completed";
            }
            
            return "Waiting for conditions";
        }
        
        
        
        return "Schedule not set";
    }
    
    public void addStartCondition(Condition condition) {
        startConditionManager.addUserCondition(condition);
    }
    public void addStopCondition(Condition condition) {
        stopConditionManager.addUserCondition(condition);
    }

    public List<Condition> getStopConditions() {
        return stopConditionManager.getConditions();
    }
    public boolean hasStopConditions() {
        return stopConditionManager.hasConditions();
    }
    public boolean hasStartConditions() {
        return startConditionManager.hasConditions();
    }
    public List<Condition> getStartConditions() {
        return startConditionManager.getConditions();
    }

    // Determine if plugin should stop based on conditions and/or duration
    public boolean shouldStop() {
        if (finished) {
            return true; // Plugin has finished its run
        }
        if (isRunning()) {
            if (!isEnabled()){
                return true; //enabled was disabled -> stop the plugin gracefully -> soft stop should be trigged when possible
            }
        }
        // Check if conditions are met and we should stop when conditions are met
        if (areStopConditionsMet()) {
            return true;
        }

        return false;
    }

    public boolean areStopConditionsMet() {
        return stopConditionManager.areConditionsMet();
    }
    public boolean areStartConditionsMet() {
        return startConditionManager.areConditionsMet();
    }

    public String getConditionsDescription() {
        return stopConditionManager.getDescription();
    }

    
    public boolean checkConditionsAndStop(boolean successfulRun) {
        ZonedDateTime now = ZonedDateTime.now();
        
        if (shouldStop()) {
            // Initial stop attempt
            if (!stopInitiated) {
                logStopConditionsWithDetails();
                log.info("Stopping plugin {} due to conditions being met - initiating soft stop", name);
                this.softStop(true); // This will start the monitoring thread
            }
            // Plugin didn't stop after previous attempts
            else if (isRunning()) {
                Duration timeSinceFirstAttempt = Duration.between(stopInitiatedTime, now);
                Duration timeSinceLastAttempt = Duration.between(lastStopAttemptTime, now);
                
                // Force hard stop if we've waited too long
                if (timeSinceFirstAttempt.compareTo(hardStopTimeout) > 0 
                    && (getPlugin() instanceof ConditionProvider)
                    && ((ConditionProvider) getPlugin()).isHardStoppable()) {
                    log.warn("Plugin {} failed to respond to soft stop after {} seconds - forcing hard stop", 
                             name, timeSinceFirstAttempt.toSeconds());
                    
                    // Stop current monitoring and start new one for hard stop
                    stopMonitoringThread();
                    this.hardStop(true);
                }
                // Retry soft stop at configured intervals
                else if (timeSinceLastAttempt.compareTo(softStopRetryInterval) > 0) {
                    log.info("Plugin {} still running after soft stop - retrying (attempt time: {} seconds)", 
                             name, timeSinceFirstAttempt.toSeconds());
                    lastStopAttemptTime = now;
                    this.softStop(true);
                }else if (timeSinceLastAttempt.compareTo(hardStopTimeout) > 0) {                    
                    log.error("Forcibly shutting down the client due to unresponsive plugin: {}", name);
    
                    // Schedule client shutdown on the client thread to ensure it happens safely
                    Microbot.getClientThread().invoke(() -> {
                        try {
                            // Log that we're shutting down
                            log.warn("Initiating emergency client shutdown due to plugin: {} cant be stopped", name);
                            
                            // Give a short delay for logging to complete
                            Thread.sleep(1000);
                            
                            // Forcibly exit the JVM with a non-zero status code to indicate abnormal termination
                            System.exit(1);
                        } catch (Exception e) {
                            log.error("Failed to shut down client", e);
                            // Ultimate fallback
                            Runtime.getRuntime().halt(1);
                        }
                        return true;
                    });  
                }
            }
            // Monitor thread will handle the successful stop case
        }
        // Reset stop tracking if conditions no longer require stopping
        else if (stopInitiated) {
            log.info("Plugin {} conditions no longer require stopping - resetting stop state", name);
            this.stopInitiated = false;
            this.stopInitiatedTime = null;
            this.lastStopAttemptTime = null;
            stopMonitoringThread();
        }
        return this.stopInitiated;
        
    }

    /**
     * Logs all defined conditions when plugin starts
     */
    private void logStopConditions() {
        List<Condition> conditionList = stopConditionManager.getConditions();
        logConditionInfo(conditionList,"Defined Stop Conditions", true);
    }

    /**
     * Logs which conditions are met and which aren't when plugin stops
     */
    private void logStopConditionsWithDetails() {
        List<Condition> conditionList = stopConditionManager.getConditions();
        logConditionInfo(conditionList,"Defined Stop Conditions", true);
    }

    
    

    /**
     * Creates a consolidated log of all condition-related information
     * @param logINFOHeader The header to use for the log message
     * @param includeDetails Whether to include full details of conditions
     */
    public void logConditionInfo(List<Condition> conditionList, String logINFOHeader, boolean includeDetails) {
        
        StringBuilder sb = new StringBuilder();
        
        sb.append("\nPlugin '").append(cleanName).append("' [").append(logINFOHeader).append("]: ");

        if (conditionList.isEmpty()) {
            sb.append("No stop conditions defined");
            log.info(sb.toString());
            return;
        }
        
        // Basic condition count and logic
        sb.append(conditionList.size()).append(" condition(s) using ")
          .append(stopConditionManager.requiresAll() ? "AND" : "OR").append(" logic\n");
        
        if (!includeDetails) {
            log.info(sb.toString());
            return;
        }
        
        // Detailed condition listing with status
        
        int metCount = 0;
        
        for (int i = 0; i < conditionList.size(); i++) {
            Condition condition = conditionList.get(i);
            boolean isSatisfied = condition.isSatisfied();
            if (isSatisfied) metCount++;
            
            // Use the new getStatusInfo method for detailed status
            sb.append("  ").append(i + 1).append(". ")
              .append(condition.getStatusInfo(0, includeDetails).replace("\n", "\n    "));
            
            sb.append("\n");
        }
        
        if (includeDetails) {
            sb.append("Summary: ").append(metCount).append("/").append(conditionList.size())
              .append(" conditions met");
        }
        
        log.info(sb.toString());
    }




    /**
     * Updates or adds a condition at runtime.
     * This can be used by plugins to dynamically update their stopping conditions.
     * 
     * @param condition The condition to add or update
     * @return This ScheduledPlugin instance for method chaining
     */
    public PluginScheduleEntry updateStopCondition(Condition condition) {
        // Check if we already have a condition of the same type
        boolean found = false;
        for (int i = 0; i < stopConditionManager.getConditions().size(); i++) {
            Condition existing = stopConditionManager.getConditions().get(i);
            if (existing.getClass().equals(condition.getClass())) {
                // Replace the existing condition
                stopConditionManager.getConditions().set(i, condition);
                found = true;
                break;
            }
        }

        // If not found, add it
        if (!found) {
            stopConditionManager.addUserCondition(condition);
        }

        return this;
    }

    /**
     * Registers any custom stopping conditions provided by the plugin.
     * These conditions are combined with existing conditions using AND logic
     * to ensure plugin-defined conditions have the highest priority.
     * 
     * @param plugin    The plugin that might provide conditions
     * @param scheduled The scheduled instance managing the plugin
     */
    public void registerPluginStoppingConditions() {
        if (this.plugin == null) {
            this.plugin = getPlugin();
        }
        log.info("Registering stopping conditions for plugin '{}'", name);
        if (this.plugin instanceof ConditionProvider) {
            ConditionProvider provider = (ConditionProvider) plugin;

            // Get conditions from the provider
            
            List<Condition> pluginConditions = provider.getStopCondition().getConditions();
            if (pluginConditions != null && !pluginConditions.isEmpty()) {                
                // Get or create plugin's logical structure
                
                LogicalCondition pluginLogic = provider.getStopCondition();                                
                // Set the new root condition                
                getStopConditionManager().setPluginCondition(pluginLogic);
                
                // Log with the consolidated method
                logStopConditionsWithDetails();
            } else {
                log.info("Plugin '{}' implements StoppingConditionProvider but provided no conditions",
                                        plugin.getName());
            }
        }
    }
    private void registerPluginStartingConditions(){
        if (this.plugin == null) {
            this.plugin = getPlugin();
        }
        log.info("Registering start conditions for plugin '{}'", name);
        if (this.plugin instanceof ConditionProvider) {
            ConditionProvider provider = (ConditionProvider) plugin;

            // Get conditions from the provider
            if (provider.getStartCondition() == null) {
                log.warn("Plugin '{}' implements ConditionProvider but provided no start conditions", plugin.getName());
                return;
            }
            List<Condition> pluginConditions = provider.getStartCondition().getConditions();
            if (pluginConditions != null && !pluginConditions.isEmpty()) {
                // Create a new AND condition as the root

                

                // Get or create plugin's logical structure
                LogicalCondition pluginLogic = provider.getStartCondition();

                if (pluginLogic != null) {
                    for (Condition condition : pluginConditions) {
                        if(pluginLogic.contains(condition)){
                            continue;
                        }
                    }
                    
                }else{
                    throw new RuntimeException("Plugin '"+name+"' implements ConditionProvider but provided no conditions");
                }
                                
                // Set the new root condition
                getStartConditionManager().setPluginCondition(pluginLogic);
                
                // Log with the consolidated method
                logStartConditionsWithDetails();
            } else {
                log.info("Plugin '{}' implements condition Provider but provided no explicit start conditions defined",
                        plugin.getName());
            }
        }

    }
    public void registerPluginConditions(){

        log.info("Registering plugin conditions for plugin '{}'", name);
        registerPluginStartingConditions();
        registerPluginStoppingConditions();
        log.info("registered plugin conditions for plugin '{}'", name);
    }


  

    /**
     * Calculates overall progress percentage across all conditions.
     * This respects the logical structure of conditions.
     * Returns 0 if progress cannot be determined.
     */
    public double getStopConditionProgress() {
        // If there are no conditions, no progress to report
        if (stopConditionManager == null || stopConditionManager.getConditions().isEmpty()) {
            return 0;
        }
        
        // If using logical root condition, respect its logical structure
        LogicalCondition rootLogical = stopConditionManager.getFullLogicalCondition();
        if (rootLogical != null) {
            return rootLogical.getProgressPercentage();
        }
        
        // Fallback for direct condition list: calculate based on AND/OR logic
        boolean requireAll = stopConditionManager.requiresAll();
        List<Condition> conditions = stopConditionManager.getConditions();
        
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
     * Gets the total number of conditions being tracked.
     */
    public int getTotalStopConditionCount() {
        if (stopConditionManager == null) {
            return 0;
        }
        
        LogicalCondition rootLogical = stopConditionManager.getFullLogicalCondition();
        if (rootLogical != null) {
            return rootLogical.getTotalConditionCount();
        }
        
        return stopConditionManager.getConditions().stream()
            .mapToInt(Condition::getTotalConditionCount)
            .sum();
    }

    /**
     * Gets the number of conditions that are currently met.
     */
    public int getSatisfiedStopConditionCount() {
        if (stopConditionManager == null) {
            return 0;
        }
        
        LogicalCondition rootLogical = stopConditionManager.getFullLogicalCondition();
        if (rootLogical != null) {
            return rootLogical.getMetConditionCount();
        }
        
        return stopConditionManager.getConditions().stream()
            .mapToInt(Condition::getMetConditionCount)
            .sum();
    }
    public LogicalCondition getLogicalStopCondition() {
        return stopConditionManager.getFullLogicalCondition();
    }


    // Add getter/setter for the new fields
    public boolean isAllowRandomScheduling() {
        return allowRandomScheduling;
    }

    public void setAllowRandomScheduling(boolean allowRandomScheduling) {
        this.allowRandomScheduling = allowRandomScheduling;
    }

    public int getRunCount() {
        return runCount;
    }

    private void incrementRunCount() {
        this.runCount++;
    }

    // Setter methods for the configurable timeouts
    public void setSoftStopRetryInterval(Duration interval) {
        this.softStopRetryInterval = interval;
    }

    public void setHardStopTimeout(Duration timeout) {
        this.hardStopTimeout = timeout;
    }

 

    /**
     * Convert a list of ScheduledPlugin objects to JSON
     */
    public static String toJson(List<PluginScheduleEntry> plugins) {
        return ScheduledSerializer.toJson(plugins);
    }


        /**
     * Parse JSON into a list of ScheduledPlugin objects
     */
    public static List<PluginScheduleEntry> fromJson(String json) {
        return ScheduledSerializer.fromJson(json);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        PluginScheduleEntry that = (PluginScheduleEntry) o;
        
        // Two entries are equal if:
        // 1. They have the same name AND
        // 2. They have the same start conditions and stop conditions
        //    OR they are the same object reference
        
        if (!Objects.equals(name, that.name)) return false;
        
        // If they're the same name, we need to distinguish by conditions
        if (startConditionManager != null && that.startConditionManager != null) {
            if (!startConditionManager.getConditions().equals(that.startConditionManager.getConditions())) {
                return false;
            }
        } else if (startConditionManager != null || that.startConditionManager != null) {
            return false;
        }
        
        if (stopConditionManager != null && that.stopConditionManager != null) {
            return stopConditionManager.getConditions().equals(that.stopConditionManager.getConditions());
        } else {
            return stopConditionManager == null && that.stopConditionManager == null;
        }
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (startConditionManager != null ? startConditionManager.getConditions().hashCode() : 0);
        result = 31 * result + (stopConditionManager != null ? stopConditionManager.getConditions().hashCode() : 0);
        return result;
    }

    public int getPriority() {
        return priority;
    }
    
    public void setPriority(int priority) {
        this.priority = priority;
    }
    
    public boolean isDefault() {
        return isDefault;
    }
    
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
    /**
    * Generic helper method to build condition diagnostics for both start and stop conditions
    * 
    * @param isStartCondition Whether to diagnose start conditions (true) or stop conditions (false)
    * @return A detailed diagnostic string
    */
    private String buildConditionDiagnostics(boolean isStartCondition) {
        StringBuilder sb = new StringBuilder();
        String conditionType = isStartCondition ? "Start" : "Stop";
        ConditionManager conditionManager = isStartCondition ? startConditionManager : stopConditionManager;
        List<Condition> conditions = isStartCondition ? getStartConditions() : getStopConditions();
        
        // Header with plugin name
        sb.append("[").append(cleanName).append("] ").append(conditionType).append(" condition diagnostics:\n");
        
        // Check if running (only relevant for start conditions)
        if (isStartCondition && isRunning()) {
            sb.append("- Plugin is already running (will not start again until stopped)\n");
            return sb.toString();
        }
        
        // Check for conditions
        if (conditions.isEmpty()) {
            sb.append("- No ").append(conditionType.toLowerCase()).append(" conditions defined\n");
            return sb.toString();
        }
        
        // Condition logic type
        sb.append("- Logic: ")
        .append(conditionManager.requiresAll() ? "ALL conditions must be met" : "ANY condition can be met")
        .append("\n");
        
        // Condition description
        sb.append("- Conditions: ")
        .append(conditionManager.getDescription())
        .append("\n");
        
        // Check if they can be fulfilled
        boolean canBeFulfilled = isStartCondition ? 
                hasFullfillableStartConditions() : 
                hasFullfillableStopConditions();
        
        if (!canBeFulfilled) {
            sb.append("- Conditions cannot be fulfilled (e.g., one-time conditions already triggered)\n");
        }
        
        // Progress
        double progress = isStartCondition ? 
                conditionManager.getProgressTowardNextTrigger() : 
                getStopConditionProgress();
        sb.append("- Progress: ")
        .append(String.format("%.1f%%", progress))
        .append("\n");
        
        // Next trigger time
        Optional<ZonedDateTime> nextTrigger = isStartCondition ? 
                getCurrentStartTriggerTime() : 
                getNextStopTriggerTime();
        
        sb.append("- Next trigger: ");
        if (nextTrigger.isPresent()) {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
            ZonedDateTime triggerTime = nextTrigger.get();
            
            sb.append(triggerTime).append("\n");
            sb.append("- Current time: ").append(now).append("\n");
            
            if (triggerTime.isBefore(now)) {
                sb.append("- Trigger time is in the past but conditions not met - may need reset\n");
            } else {
                Duration timeUntil = Duration.between(now, triggerTime);
                sb.append("- Time until trigger: ").append(formatDuration(timeUntil)).append("\n");
            }
        } else {
            sb.append("No future trigger time determined\n");
        }
        
        // Overall condition status
        boolean areConditionsMet = isStartCondition ? 
                startConditionManager.areConditionsMet() : 
                areStopConditionsMet();
        
        sb.append("- Status: ")
        .append(areConditionsMet ? 
                "CONDITIONS MET - Plugin is " + (isStartCondition ? "due to run" : "due to stop") : 
                "CONDITIONS NOT MET - Plugin " + (isStartCondition ? "will not run" : "will continue running"))
        .append("\n");
        
        // Individual condition status
        sb.append("- Individual conditions:\n");
        for (int i = 0; i < conditions.size(); i++) {
            Condition condition = conditions.get(i);
            sb.append("  ").append(i+1).append(". ")
            .append(condition.getDescription())
            .append(": ")
            .append(condition.isSatisfied() ? "SATISFIED" : "NOT SATISFIED");
            
            // Add progress if available
            double condProgress = condition.getProgressPercentage();
            if (condProgress > 0 && condProgress < 100) {
                sb.append(String.format(" (%.1f%%)", condProgress));
            }
            
            // For time conditions, show next trigger time
            if (condition instanceof TimeCondition) {
                Optional<ZonedDateTime> condTrigger = condition.getCurrentTriggerTime();
                if (condTrigger.isPresent()) {
                    sb.append(" (next trigger: ").append(condTrigger.get()).append(")");
                }
            }
            
            sb.append("\n");
        }
        
        return sb.toString();
    }

    /**
     * Performs a diagnostic check on start conditions and returns detailed information
     * about why a plugin might not be due to run
     * 
     * @return A string containing diagnostic information
     */
    public String diagnoseStartConditions() {
        return buildConditionDiagnostics(true);
    }

    /**
     * Performs a diagnostic check on stop conditions and returns detailed information
     * about why a plugin might or might not be due to stop
     * 
     * @return A string containing diagnostic information
     */
    public String diagnoseStopConditions() {
        return buildConditionDiagnostics(false);
    }

    /**
     * Formats a duration in a human-readable way
     */
    private String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        if (seconds < 60) {
            return seconds + " seconds";
        } else if (seconds < 3600) {
            return String.format("%dm %ds", seconds / 60, seconds % 60);
        } else if (seconds < 86400) {
            return String.format("%dh %dm %ds", seconds / 3600, (seconds % 3600) / 60, seconds % 60);
        } else {
            return String.format("%dd %dh %dm", seconds / 86400, (seconds % 86400) / 3600, (seconds % 3600) / 60);
        }
    }
}
