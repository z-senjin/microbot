package net.runelite.client.plugins.microbot.pluginscheduler.condition.time.ui;
import java.time.ZoneId;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import net.runelite.client.plugins.microbot.pluginscheduler.ui.components.DateRangePanel;
import net.runelite.client.plugins.microbot.pluginscheduler.ui.components.IntervalPickerPanel;
import net.runelite.client.plugins.microbot.pluginscheduler.ui.components.SingleDateTimePickerPanel;
import net.runelite.client.plugins.microbot.pluginscheduler.ui.components.TimeRangePanel;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.time.ZonedDateTime;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;



import net.runelite.client.plugins.microbot.pluginscheduler.condition.Condition;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.time.DayOfWeekCondition;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.time.IntervalCondition;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.time.TimeWindowCondition;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.time.enums.RepeatCycle;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.time.SingleTriggerTimeCondition;

public class TimeConditionPanelUtil {
    public static void createIntervalConfigPanel(JPanel panel, GridBagConstraints gbc, JPanel configPanel) {
        // Title and initial setup
        JLabel titleLabel = new JLabel("Time Interval Configuration:");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(FontManager.getRunescapeSmallFont().deriveFont(Font.BOLD));
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        // Create and add interval picker component
        gbc.gridy++;
        IntervalPickerPanel intervalPicker = new IntervalPickerPanel(true);
        panel.add(intervalPicker, gbc);
        
        // Add a helpful description
        gbc.gridy++;
        JLabel descriptionLabel = new JLabel("Plugin will stop after specified time interval");
        descriptionLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        descriptionLabel.setFont(FontManager.getRunescapeSmallFont());
        panel.add(descriptionLabel, gbc);
        
        // Add additional info about randomization
        gbc.gridy++;
        JLabel randomInfoLabel = new JLabel("Random intervals make your bot behavior less predictable");
        randomInfoLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        randomInfoLabel.setFont(FontManager.getRunescapeSmallFont());
        panel.add(randomInfoLabel, gbc);
        
        // Store component for later access
        configPanel.putClientProperty("intervalPicker", intervalPicker);
    }
    
    /**
     * Helper method to validate min and max intervals ensure min <= max
     */
    private static void validateMinMaxIntervals(
            JSpinner minHoursSpinner, JSpinner minMinutesSpinner, 
            JSpinner maxHoursSpinner, JSpinner maxMinutesSpinner, 
            boolean isMinUpdated) {
        
        int minHours = (Integer) minHoursSpinner.getValue();
        int minMinutes = (Integer) minMinutesSpinner.getValue();
        int maxHours = (Integer) maxHoursSpinner.getValue();
        int maxMinutes = (Integer) maxMinutesSpinner.getValue();
        
        int minTotalMinutes = minHours * 60 + minMinutes;
        int maxTotalMinutes = maxHours * 60 + maxMinutes;
        
        if (isMinUpdated) {
            // If min was updated and exceeds max, adjust max
            if (minTotalMinutes > maxTotalMinutes) {
                maxHoursSpinner.setValue(minHours);
                maxMinutesSpinner.setValue(minMinutes);
            }
        } else {
            // If max was updated and is less than min, adjust min
            if (maxTotalMinutes < minTotalMinutes) {
                minHoursSpinner.setValue(maxHours);
                minMinutesSpinner.setValue(maxMinutes);
            }
        }
    }
    
    /**
     * Creates an IntervalCondition from the config panel.
     * This replaces the createTimeCondition method.
     */
    public static IntervalCondition createIntervalCondition(JPanel configPanel) {
        IntervalPickerPanel intervalPicker = (IntervalPickerPanel) configPanel.getClientProperty("intervalPicker");
        
        if (intervalPicker == null) {
            throw new IllegalStateException("Interval picker component not found");
        }
        
        // Get the interval condition from the picker component
        return intervalPicker.createIntervalCondition();
    }
    
    public static void createEnhancedTimeWindowConfigPanel(JPanel panel, GridBagConstraints gbc, JPanel configPanel) {
        // Section Title
        JLabel titleLabel = new JLabel("Time Window Configuration:");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(FontManager.getRunescapeSmallFont().deriveFont(Font.BOLD));
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        
        // Use our new custom components
        gbc.gridy++;
        
        // Create date range panel
        DateRangePanel dateRangePanel = new DateRangePanel();
        panel.add(dateRangePanel, gbc);
        
        // Add small vertical space
        gbc.gridy++;
        panel.add(Box.createVerticalStrut(10), gbc);
        
        // Create time range panel
        gbc.gridy++;
        TimeRangePanel timeRangePanel = new TimeRangePanel();
        panel.add(timeRangePanel, gbc);
        
        // Repeat Cycle Panel
        gbc.gridy++;
        JPanel repeatPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        repeatPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        
        JLabel repeatLabel = new JLabel("Repeat Cycle:");
        repeatLabel.setForeground(Color.WHITE);
        repeatPanel.add(repeatLabel);
        
        // Create combo box with repeat cycle options
        String[] repeatOptions = {"Every Day", "Every X Days", "Every X Hours", "Every X Minutes", "Every X Weeks", "One Time Only"};
        JComboBox<String> repeatComboBox = new JComboBox<>(repeatOptions);
        repeatPanel.add(repeatComboBox);
        
        JLabel intervalLabel = new JLabel("Interval:");
        intervalLabel.setForeground(Color.WHITE);
        repeatPanel.add(intervalLabel);
        
        // Spinner for interval value (1-100)
        SpinnerNumberModel intervalModel = new SpinnerNumberModel(1, 1, 100, 1);
        JSpinner intervalSpinner = new JSpinner(intervalModel);
        intervalSpinner.setPreferredSize(new Dimension(60, intervalSpinner.getPreferredSize().height));
        repeatPanel.add(intervalSpinner);
        
        // Initially disable interval spinner for "Every Day" option
        intervalSpinner.setEnabled(false);
        
        // Enable/disable interval spinner based on selection
        repeatComboBox.addActionListener(e -> {
            String selected = (String) repeatComboBox.getSelectedItem();
            intervalSpinner.setEnabled(!selected.equals("Every Day") && !selected.equals("One Time Only"));
        });
        
        panel.add(repeatPanel, gbc);
        
        // Randomization Panel
        gbc.gridy++;
        JPanel randomizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        randomizePanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        
        JCheckBox randomizeCheckBox = new JCheckBox("Randomize window times");
        randomizeCheckBox.setForeground(Color.WHITE);
        randomizeCheckBox.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        randomizePanel.add(randomizeCheckBox);
        
        JLabel randomizeAmountLabel = new JLabel("± Minutes:");
        randomizeAmountLabel.setForeground(Color.WHITE);
        randomizePanel.add(randomizeAmountLabel);
        
        // Spinner for randomization amount (0-60 minutes)
        SpinnerNumberModel randomizeModel = new SpinnerNumberModel(15, 1, 60, 1);
        JSpinner randomizeSpinner = new JSpinner(randomizeModel);
        randomizeSpinner.setPreferredSize(new Dimension(60, randomizeSpinner.getPreferredSize().height));
        randomizeSpinner.setEnabled(false);
        randomizePanel.add(randomizeSpinner);
        
        // Enable/disable randomize spinner based on checkbox
        randomizeCheckBox.addActionListener(e -> 
            randomizeSpinner.setEnabled(randomizeCheckBox.isSelected())
        );
        
        panel.add(randomizePanel, gbc);
        
        // Add a helpful description
        gbc.gridy++;
        JLabel descriptionLabel = new JLabel("Plugin will only run during the specified time window");
        descriptionLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        descriptionLabel.setFont(FontManager.getRunescapeSmallFont());
        panel.add(descriptionLabel, gbc);
        
        gbc.gridy++;
        JLabel crossDayLabel = new JLabel("Note: If start time > end time, window crosses midnight");
        crossDayLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        crossDayLabel.setFont(FontManager.getRunescapeSmallFont());
        panel.add(crossDayLabel, gbc);
        
        gbc.gridy++;
        JPanel timezonePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timezonePanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        JLabel timezoneLabel = new JLabel("Current timezone: " + ZoneId.systemDefault().getId());
        timezoneLabel.setForeground(Color.YELLOW);
        timezoneLabel.setFont(FontManager.getRunescapeSmallFont());
        timezonePanel.add(timezoneLabel);
        
        panel.add(timezonePanel, gbc);
        
        // Store components for later access
        configPanel.putClientProperty("dateRangePanel", dateRangePanel); 
        configPanel.putClientProperty("timeRangePanel", timeRangePanel);
        configPanel.putClientProperty("repeatComboBox", repeatComboBox);
        configPanel.putClientProperty("intervalSpinner", intervalSpinner);
        configPanel.putClientProperty("randomizeCheckBox", randomizeCheckBox);
        configPanel.putClientProperty("randomizeSpinner", randomizeSpinner);
    }

    public static TimeWindowCondition createEnhancedTimeWindowCondition(JPanel configPanel) {
        DateRangePanel dateRangePanel = (DateRangePanel) configPanel.getClientProperty("dateRangePanel");
        TimeRangePanel timeRangePanel = (TimeRangePanel) configPanel.getClientProperty("timeRangePanel");
        JComboBox<String> repeatComboBox = (JComboBox<String>) configPanel.getClientProperty("repeatComboBox");
        JSpinner intervalSpinner = (JSpinner) configPanel.getClientProperty("intervalSpinner");
        JCheckBox randomizeCheckBox = (JCheckBox) configPanel.getClientProperty("randomizeCheckBox");
        JSpinner randomizeSpinner = (JSpinner) configPanel.getClientProperty("randomizeSpinner");
        
        if (dateRangePanel == null || timeRangePanel == null) {
            throw new IllegalStateException("Time window configuration components not found");
        }
        
        // Get date values
        LocalDate startDate = dateRangePanel.getStartDate();
        LocalDate endDate = dateRangePanel.getEndDate();
        
        // Get time values 
        LocalTime startTime = timeRangePanel.getStartTime();
        LocalTime endTime = timeRangePanel.getEndTime();
        
        // Get repeat cycle configuration
        String repeatOption = (String) repeatComboBox.getSelectedItem();
        RepeatCycle repeatCycle;
        int interval = (Integer) intervalSpinner.getValue();
        long maximumNumberOfRepeats = 0; // Default to infinite repeats
        switch (repeatOption) {
            case "Every Day":
                repeatCycle = RepeatCycle.DAYS;
                interval = 1;
                break;
            case "Every X Days":
                repeatCycle = RepeatCycle.DAYS;
                break;
            case "Every X Hours":
                repeatCycle = RepeatCycle.HOURS;
                break;
            case "Every X Minutes":
                repeatCycle = RepeatCycle.MINUTES;
                break;
            case "Every X Weeks":
                repeatCycle = RepeatCycle.WEEKS;
                break;
            case "One Time Only":
                repeatCycle = RepeatCycle.ONE_TIME;
                interval = 1;
                maximumNumberOfRepeats = 1;
                break;
            default:
                repeatCycle = RepeatCycle.DAYS;
                interval = 1;
        }
        
        // Create the condition
        TimeWindowCondition condition = new TimeWindowCondition(
            startTime,
            endTime,
            startDate,
            endDate,
            repeatCycle,
            interval,
            maximumNumberOfRepeats
            
        );
        
        // Apply randomization if enabled
        if (randomizeCheckBox.isSelected()) {
            int randomizeMinutes = (Integer) randomizeSpinner.getValue();
            condition.setRandomization(true, randomizeMinutes);
        }
        
        return condition;
    }


        /**
     * Creates a panel for configuring SingleTriggerTimeCondition
     * Uses the enhanced SingleDateTimePickerPanel component
     */
    public static void createSingleTriggerConfigPanel(JPanel panel, GridBagConstraints gbc, JPanel configPanel) {
        // Section title
        JLabel titleLabel = new JLabel("One-Time Trigger Configuration:");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(FontManager.getRunescapeSmallFont().deriveFont(Font.BOLD));
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(titleLabel, gbc);
        
        // Create the date/time picker panel
        gbc.gridy++;
        SingleDateTimePickerPanel dateTimePicker = new SingleDateTimePickerPanel();
        panel.add(dateTimePicker, gbc);                     
        // Description
        gbc.gridy++;
        JLabel descriptionLabel = new JLabel("Plugin will be triggered once at the specified date and time");
        descriptionLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        descriptionLabel.setFont(FontManager.getRunescapeSmallFont());
        panel.add(descriptionLabel, gbc);
        
        // Current timezone info
        gbc.gridy++;
        JLabel timezoneLabel = new JLabel("Current timezone: " + ZoneId.systemDefault().getId());
        timezoneLabel.setForeground(Color.YELLOW);
        timezoneLabel.setFont(FontManager.getRunescapeSmallFont());
        panel.add(timezoneLabel, gbc);
        
        // Store components for later access
        configPanel.putClientProperty("dateTimePicker", dateTimePicker);        
        
    }
    /**
     * Creates a SingleTriggerTimeCondition from the config panel
     * Uses the enhanced SingleDateTimePickerPanel component
     */
    public static SingleTriggerTimeCondition createSingleTriggerCondition(JPanel configPanel) {
        SingleDateTimePickerPanel dateTimePicker = (SingleDateTimePickerPanel) configPanel.getClientProperty("dateTimePicker");        
        JRadioButton removeAfterRadio = (JRadioButton) configPanel.getClientProperty("singleTriggerRemoveAfterRadio");
        
        if (dateTimePicker == null) {
            throw new IllegalStateException("Date/time picker not found. Please check the panel configuration.");
        }
        
        // Get the selected date and time as LocalDateTime
        LocalDateTime selectedDateTime = dateTimePicker.getDateTime();
        
        // Convert to ZonedDateTime using the system default timezone
        ZonedDateTime triggerTime = selectedDateTime.atZone(ZoneId.systemDefault());
        
        
        // Get post-trigger behavior (whether to remove the condition after triggering)
        boolean removeAfterTrigger = removeAfterRadio == null || removeAfterRadio.isSelected();
        
        // Create the condition with the appropriate settings
        return new SingleTriggerTimeCondition(triggerTime);
    }
    public static void createDayOfWeekConfigPanel(JPanel panel, GridBagConstraints gbc, JPanel configPanel) {
    // Title and initial setup
    JLabel titleLabel = new JLabel("Day of Week Configuration:");
    titleLabel.setForeground(Color.WHITE);
    titleLabel.setFont(FontManager.getRunescapeSmallFont().deriveFont(Font.BOLD));
    panel.add(titleLabel, gbc);

    // Preset options
    gbc.gridy++;
    JPanel presetPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    presetPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
    
    JButton weekdaysButton = new JButton("Weekdays");
    weekdaysButton.setBackground(ColorScheme.DARKER_GRAY_COLOR);
    weekdaysButton.setForeground(Color.WHITE);
    
    JButton weekendsButton = new JButton("Weekends");
    weekendsButton.setBackground(ColorScheme.DARKER_GRAY_COLOR);
    weekendsButton.setForeground(Color.WHITE);
    
    JButton allDaysButton = new JButton("All Days");
    allDaysButton.setBackground(ColorScheme.DARKER_GRAY_COLOR);
    allDaysButton.setForeground(Color.WHITE);
    
    presetPanel.add(weekdaysButton);
    presetPanel.add(weekendsButton);
    presetPanel.add(allDaysButton);
    
    panel.add(presetPanel, gbc);
    
    // Day checkboxes
    gbc.gridy++;
    JPanel daysPanel = new JPanel(new GridLayout(0, 3));
    daysPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
    
    String[] dayNames = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    JCheckBox[] dayCheckboxes = new JCheckBox[7];
    
    for (int i = 0; i < dayNames.length; i++) {
        dayCheckboxes[i] = new JCheckBox(dayNames[i]);
        dayCheckboxes[i].setBackground(ColorScheme.DARKER_GRAY_COLOR);
        dayCheckboxes[i].setForeground(Color.WHITE);
        daysPanel.add(dayCheckboxes[i]);
    }
    
    // Set up weekdays button
    weekdaysButton.addActionListener(e -> {
        for (int i = 0; i < 5; i++) {
            dayCheckboxes[i].setSelected(true);
        }
        dayCheckboxes[5].setSelected(false);
        dayCheckboxes[6].setSelected(false);
    });
    
    // Set up weekends button
    weekendsButton.addActionListener(e -> {
        for (int i = 0; i < 5; i++) {
            dayCheckboxes[i].setSelected(false);
        }
        dayCheckboxes[5].setSelected(true);
        dayCheckboxes[6].setSelected(true);
    });
    
    // Set up all days button
    allDaysButton.addActionListener(e -> {
        for (JCheckBox checkbox : dayCheckboxes) {
            checkbox.setSelected(true);
        }
    });
    
    panel.add(daysPanel, gbc);
    
    // Add usage limits panel
    gbc.gridy++;
    JPanel usageLimitsPanel = new JPanel();
    usageLimitsPanel.setLayout(new BoxLayout(usageLimitsPanel, BoxLayout.Y_AXIS));
    usageLimitsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
    
    // Daily limit panel
    JPanel dailyLimitPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    dailyLimitPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
    
    JLabel dailyLimitLabel = new JLabel("Max repeats per day:");
    dailyLimitLabel.setForeground(Color.WHITE);
    dailyLimitPanel.add(dailyLimitLabel);
    
    SpinnerNumberModel dailyLimitModel = new SpinnerNumberModel(0, 0, 100, 1);
    JSpinner dailyLimitSpinner = new JSpinner(dailyLimitModel);
    dailyLimitSpinner.setPreferredSize(new Dimension(70, dailyLimitSpinner.getPreferredSize().height));
    dailyLimitPanel.add(dailyLimitSpinner);
    
    JLabel dailyUnlimitedLabel = new JLabel("(0 = unlimited)");
    dailyUnlimitedLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
    dailyUnlimitedLabel.setFont(FontManager.getRunescapeSmallFont());
    dailyLimitPanel.add(dailyUnlimitedLabel);
    
    usageLimitsPanel.add(dailyLimitPanel);
    
    // Weekly limit panel
    JPanel weeklyLimitPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    weeklyLimitPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
    
    JLabel weeklyLimitLabel = new JLabel("Max repeats per week:");
    weeklyLimitLabel.setForeground(Color.WHITE);
    weeklyLimitPanel.add(weeklyLimitLabel);
    
    SpinnerNumberModel weeklyLimitModel = new SpinnerNumberModel(0, 0, 100, 1);
    JSpinner weeklyLimitSpinner = new JSpinner(weeklyLimitModel);
    weeklyLimitSpinner.setPreferredSize(new Dimension(70, weeklyLimitSpinner.getPreferredSize().height));
    weeklyLimitPanel.add(weeklyLimitSpinner);
    
    JLabel weeklyUnlimitedLabel = new JLabel("(0 = unlimited)");
    weeklyUnlimitedLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
    weeklyUnlimitedLabel.setFont(FontManager.getRunescapeSmallFont());
    weeklyLimitPanel.add(weeklyUnlimitedLabel);
    
    usageLimitsPanel.add(weeklyLimitPanel);
    
    panel.add(usageLimitsPanel, gbc);
    
    // Add interval configuration using the reusable IntervalPickerPanel
    gbc.gridy++;
    JPanel intervalOptionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    intervalOptionPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
    
    JCheckBox useIntervalCheckBox = new JCheckBox("Use interval between triggers");
    useIntervalCheckBox.setBackground(ColorScheme.DARKER_GRAY_COLOR);
    useIntervalCheckBox.setForeground(Color.WHITE);
    intervalOptionPanel.add(useIntervalCheckBox);
    
    panel.add(intervalOptionPanel, gbc);
    
    // Add the interval picker panel (initially disabled)
    gbc.gridy++;
    IntervalPickerPanel intervalPicker = new IntervalPickerPanel(false); // No presets needed
    intervalPicker.setEnabled(false);
    panel.add(intervalPicker, gbc);
    
    // Toggle interval picker based on checkbox
    useIntervalCheckBox.addActionListener(e -> {
        boolean useInterval = useIntervalCheckBox.isSelected();
        intervalPicker.setEnabled(useInterval);
    });
    
    // Description
    gbc.gridy++;
    JLabel descriptionLabel = new JLabel("Plugin will only run on selected days of the week");
    descriptionLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
    descriptionLabel.setFont(FontManager.getRunescapeSmallFont());
    panel.add(descriptionLabel, gbc);
    
    // Add limits description
    gbc.gridy++;
    JLabel limitsLabel = new JLabel("Daily/weekly limits prevent excessive usage");
    limitsLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
    limitsLabel.setFont(FontManager.getRunescapeSmallFont());
    panel.add(limitsLabel, gbc);
    
    // Add interval description
    gbc.gridy++;
    JLabel intervalDescLabel = new JLabel("Intervals control time between triggers on the same day");
    intervalDescLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
    intervalDescLabel.setFont(FontManager.getRunescapeSmallFont());
    panel.add(intervalDescLabel, gbc);
    
    // Store components for later access
    configPanel.putClientProperty("dayCheckboxes", dayCheckboxes);
    configPanel.putClientProperty("dailyLimitSpinner", dailyLimitSpinner);
    configPanel.putClientProperty("weeklyLimitSpinner", weeklyLimitSpinner);
    configPanel.putClientProperty("useIntervalCheckBox", useIntervalCheckBox);
    configPanel.putClientProperty("intervalPicker", intervalPicker);
}
public static DayOfWeekCondition createDayOfWeekCondition(JPanel configPanel) {
    JCheckBox[] dayCheckboxes = (JCheckBox[]) configPanel.getClientProperty("dayCheckboxes");
    JSpinner dailyLimitSpinner = (JSpinner) configPanel.getClientProperty("dailyLimitSpinner");
    JSpinner weeklyLimitSpinner = (JSpinner) configPanel.getClientProperty("weeklyLimitSpinner");
    JCheckBox useIntervalCheckBox = (JCheckBox) configPanel.getClientProperty("useIntervalCheckBox");
    IntervalPickerPanel intervalPicker = (IntervalPickerPanel) configPanel.getClientProperty("intervalPicker");

    if (dayCheckboxes == null) {
        throw new IllegalStateException("Day of week configuration components not found");
    }
    
    // Collect the selected days
    Set<DayOfWeek> activeDays = EnumSet.noneOf(DayOfWeek.class);
    if (dayCheckboxes[0].isSelected()) activeDays.add(DayOfWeek.MONDAY);
    if (dayCheckboxes[1].isSelected()) activeDays.add(DayOfWeek.TUESDAY);
    if (dayCheckboxes[2].isSelected()) activeDays.add(DayOfWeek.WEDNESDAY);
    if (dayCheckboxes[3].isSelected()) activeDays.add(DayOfWeek.THURSDAY);
    if (dayCheckboxes[4].isSelected()) activeDays.add(DayOfWeek.FRIDAY);
    if (dayCheckboxes[5].isSelected()) activeDays.add(DayOfWeek.SATURDAY);
    if (dayCheckboxes[6].isSelected()) activeDays.add(DayOfWeek.SUNDAY);
    
    // If no days selected, default to all days
    if (activeDays.isEmpty()) {
        activeDays.add(DayOfWeek.MONDAY);
        activeDays.add(DayOfWeek.TUESDAY);
        activeDays.add(DayOfWeek.WEDNESDAY);
        activeDays.add(DayOfWeek.THURSDAY);
        activeDays.add(DayOfWeek.FRIDAY);
        activeDays.add(DayOfWeek.SATURDAY);
        activeDays.add(DayOfWeek.SUNDAY);
    }
    
    // Get daily and weekly limits
    long maxRepeatsPerDay = dailyLimitSpinner != null ? (Integer) dailyLimitSpinner.getValue() : 0;
    long maxRepeatsPerWeek = weeklyLimitSpinner != null ? (Integer) weeklyLimitSpinner.getValue() : 0;
    
    // Create the base condition with appropriate limits
    DayOfWeekCondition condition = new DayOfWeekCondition(0, maxRepeatsPerDay, maxRepeatsPerWeek, activeDays);
    
    // If using interval, add interval condition from the interval picker
    if (useIntervalCheckBox != null && useIntervalCheckBox.isSelected() && intervalPicker != null) {
        IntervalCondition intervalCondition = intervalPicker.createIntervalCondition();
        condition.setIntervalCondition(intervalCondition);
    }
    
    return condition;
}

    
    /**
     * Sets up the panel with values from an existing time condition
     * 
     * @param panel The panel containing the UI components
     * @param condition The time condition to read values from
     */
    public static void setupTimeCondition(JPanel panel, Condition condition) {
        if (condition == null) {
            return;
        }
        
        if (condition instanceof IntervalCondition) {
            setupIntervalCondition(panel, (IntervalCondition) condition);
        } else if (condition instanceof TimeWindowCondition) {
            setupTimeWindowCondition(panel, (TimeWindowCondition) condition);
        } else if (condition instanceof DayOfWeekCondition) {
            setupDayOfWeekCondition(panel, (DayOfWeekCondition) condition);
        } else if (condition instanceof SingleTriggerTimeCondition) {
            setupSingleTriggerCondition(panel, (SingleTriggerTimeCondition) condition);
        }
    }


    /**
     * Sets up the interval condition panel with values from an existing condition
     */
    private static void setupIntervalCondition(JPanel panel, IntervalCondition condition) {
        JRadioButton fixedRadioButton = (JRadioButton) panel.getClientProperty("fixedRadioButton");
        JSpinner hoursSpinner = (JSpinner) panel.getClientProperty("hoursSpinner");
        JSpinner minutesSpinner = (JSpinner) panel.getClientProperty("minutesSpinner");
        JSpinner minHoursSpinner = (JSpinner) panel.getClientProperty("minHoursSpinner");
        JSpinner minMinutesSpinner = (JSpinner) panel.getClientProperty("minMinutesSpinner");
        JSpinner maxHoursSpinner = (JSpinner) panel.getClientProperty("maxHoursSpinner");
        JSpinner maxMinutesSpinner = (JSpinner) panel.getClientProperty("maxMinutesSpinner");
        
        if (fixedRadioButton == null || hoursSpinner == null || minutesSpinner == null ||
            minHoursSpinner == null || minMinutesSpinner == null || 
            maxHoursSpinner == null || maxMinutesSpinner == null) {
            return; // Missing UI components
        }
        
        // Check if this is a randomized min-max interval or a fixed/factor interval
        if (condition.isRandomized()) {
            // Use randomized mode
            fixedRadioButton.setSelected(false);
            
            // Get duration values for min interval
            long minTotalMinutes = condition.getMinInterval().toMinutes();
            long minHours = minTotalMinutes / 60;
            long minMinutes = minTotalMinutes % 60;
            
            // Get duration values for max interval
            long maxTotalMinutes = condition.getMaxInterval().toMinutes();
            long maxHours = maxTotalMinutes / 60;
            long maxMinutes = maxTotalMinutes % 60;
            
            // Set values on spinners
            minHoursSpinner.setValue((int)minHours);
            minMinutesSpinner.setValue((int)minMinutes);
            maxHoursSpinner.setValue((int)maxHours);
            maxMinutesSpinner.setValue((int)maxMinutes);
            
            // Also update the fixed spinner with the average value
            long avgTotalMinutes = (minTotalMinutes + maxTotalMinutes) / 2;
            hoursSpinner.setValue((int)(avgTotalMinutes / 60));
            minutesSpinner.setValue((int)(avgTotalMinutes % 60));
        } else {
            // Use fixed mode
            fixedRadioButton.setSelected(true);
            
            // Get duration values from the base interval
            long totalMinutes = condition.getInterval().toMinutes();
            long hours = totalMinutes / 60;
            long minutes = totalMinutes % 60;
            
            // Set values on fixed spinners
            hoursSpinner.setValue((int)hours);
            minutesSpinner.setValue((int)minutes);
            
            // Calculate min/max values based on randomization factor
            if (condition.isRandomize() && condition.getRandomFactor() > 0) {
                double factor = condition.getRandomFactor();
                long minTotalMinutes = Math.max(1, (long)(totalMinutes * (1 - factor)));
                long maxTotalMinutes = (long)(totalMinutes * (1 + factor));
                
                minHoursSpinner.setValue((int)(minTotalMinutes / 60));
                minMinutesSpinner.setValue((int)(minTotalMinutes % 60));
                maxHoursSpinner.setValue((int)(maxTotalMinutes / 60));
                maxMinutesSpinner.setValue((int)(maxTotalMinutes % 60));
            } else {
                // No randomization - set min/max to same as fixed
                minHoursSpinner.setValue((int)hours);
                minMinutesSpinner.setValue((int)minutes);
                maxHoursSpinner.setValue((int)hours);
                maxMinutesSpinner.setValue((int)minutes);
            }
        }
    }

    /**
     * Sets up the time window condition panel with values from an existing condition
     */
    private static void setupTimeWindowCondition(JPanel panel, TimeWindowCondition condition) {
        // Get custom components from client properties
        DateRangePanel dateRangePanel = (DateRangePanel) panel.getClientProperty("dateRangePanel");
        TimeRangePanel timeRangePanel = (TimeRangePanel) panel.getClientProperty("timeRangePanel");
        JComboBox<String> repeatComboBox = (JComboBox<String>) panel.getClientProperty("repeatComboBox");
        JSpinner intervalSpinner = (JSpinner) panel.getClientProperty("intervalSpinner");
        JCheckBox randomizeCheckBox = (JCheckBox) panel.getClientProperty("randomizeCheckBox");
        JSpinner randomizeSpinner = (JSpinner) panel.getClientProperty("randomizeSpinner");
        
        // Set date range
        if (dateRangePanel != null) {
            if (condition.getStartDate() != null) {
                dateRangePanel.setStartDate(condition.getStartDate());
            }
            if (condition.getEndDate() != null) {
                dateRangePanel.setEndDate(condition.getEndDate());
            }
        }
        
        // Set time range
        if (timeRangePanel != null) {
            timeRangePanel.setStartTime(condition.getStartTime());
            timeRangePanel.setEndTime(condition.getEndTime());
        }
        
        // Set repeat cycle
        if (repeatComboBox != null) {
            RepeatCycle cycle = condition.getRepeatCycle();
            int interval = condition.getRepeatIntervalUnit();
            
            // Map RepeatCycle enum to combo box options
            switch (cycle) {
                case DAYS:
                    repeatComboBox.setSelectedItem(interval == 1 ? "Every Day" : "Every X Days");
                    break;
                case HOURS:
                    repeatComboBox.setSelectedItem("Every X Hours");
                    break;
                case MINUTES:
                    repeatComboBox.setSelectedItem("Every X Minutes");
                    break;
                case WEEKS:
                    repeatComboBox.setSelectedItem("Every X Weeks");
                    break;
                case ONE_TIME:
                    repeatComboBox.setSelectedItem("One Time Only");
                    break;
                default:
                    repeatComboBox.setSelectedItem("Every Day");
            }
            
            // Set interval and enable spinner if needed
            if (intervalSpinner != null) {
                intervalSpinner.setValue(interval);
                
                // Only enable spinner for options that use it
                String selected = (String) repeatComboBox.getSelectedItem();
                intervalSpinner.setEnabled(!selected.equals("Every Day") && !selected.equals("One Time Only"));
            }
        }
        
        // Set randomization options
        if (randomizeCheckBox != null) {
            randomizeCheckBox.setSelected(condition.isUseRandomization());
            
            if (randomizeSpinner != null) {
                randomizeSpinner.setValue(condition.getRandomizeMinutes());
                randomizeSpinner.setEnabled(condition.isUseRandomization());
            }
        }
    }

    /**
     * Sets up the day of week condition panel with values from an existing condition
     */
    private static void setupDayOfWeekCondition(JPanel panel, DayOfWeekCondition condition) {
        JCheckBox[] dayCheckboxes = (JCheckBox[]) panel.getClientProperty("dayCheckboxes");
        JSpinner dailyLimitSpinner = (JSpinner) panel.getClientProperty("dailyLimitSpinner");
        JSpinner weeklyLimitSpinner = (JSpinner) panel.getClientProperty("weeklyLimitSpinner");
        JCheckBox useIntervalCheckBox = (JCheckBox) panel.getClientProperty("useIntervalCheckBox");
        IntervalPickerPanel intervalPicker = (IntervalPickerPanel) panel.getClientProperty("intervalPicker");
        
        if (dayCheckboxes != null) {
            Set<DayOfWeek> activeDays = condition.getActiveDays();
            
            // Map DayOfWeek enum values to checkbox indices (0 = Monday)
            if (activeDays.contains(DayOfWeek.MONDAY)) dayCheckboxes[0].setSelected(true);
            if (activeDays.contains(DayOfWeek.TUESDAY)) dayCheckboxes[1].setSelected(true);
            if (activeDays.contains(DayOfWeek.WEDNESDAY)) dayCheckboxes[2].setSelected(true);
            if (activeDays.contains(DayOfWeek.THURSDAY)) dayCheckboxes[3].setSelected(true);
            if (activeDays.contains(DayOfWeek.FRIDAY)) dayCheckboxes[4].setSelected(true);
            if (activeDays.contains(DayOfWeek.SATURDAY)) dayCheckboxes[5].setSelected(true);
            if (activeDays.contains(DayOfWeek.SUNDAY)) dayCheckboxes[6].setSelected(true);
        }
        
        // Set daily and weekly limits
        if (dailyLimitSpinner != null) {
            dailyLimitSpinner.setValue((int)condition.getMaxRepeatsPerDay());
        }
        
        if (weeklyLimitSpinner != null) {
            weeklyLimitSpinner.setValue((int)condition.getMaxRepeatsPerWeek());
        }
        
        // Handle interval condition if present
        Optional<IntervalCondition> intervalConditionOpt = condition.getIntervalCondition();
        if (intervalConditionOpt.isPresent() && useIntervalCheckBox != null && intervalPicker != null) {
            // Enable the interval checkbox
            useIntervalCheckBox.setSelected(true);
            intervalPicker.setEnabled(true);
            
            // Configure the interval picker with the condition
            intervalPicker.setIntervalCondition(intervalConditionOpt.get());
        }
        
        // Refresh panel layout
        panel.revalidate();
        panel.repaint();
    }

    /**
     * Sets up the single trigger condition panel with values from an existing condition
     */
    private static void setupSingleTriggerCondition(JPanel panel, SingleTriggerTimeCondition condition) {
        SingleDateTimePickerPanel dateTimePicker = (SingleDateTimePickerPanel) panel.getClientProperty("dateTimePicker");        
        if (dateTimePicker != null) {
            // Convert ZonedDateTime to LocalDateTime
            dateTimePicker.setDateTime(condition.getTargetTime().toLocalDateTime());
        }
        
    }
}
