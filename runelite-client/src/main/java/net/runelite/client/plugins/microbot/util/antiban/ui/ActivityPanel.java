package net.runelite.client.plugins.microbot.util.antiban.ui;

import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;

public class ActivityPanel extends JPanel {
    private final JCheckBox usePlayStyle = new JCheckBox("Use Play Style");
    private final JCheckBox useRandomIntervals = new JCheckBox("Use Random Intervals");
    private final JCheckBox simulateFatigue = new JCheckBox("Simulate Fatigue");
    private final JCheckBox simulateAttentionSpan = new JCheckBox("Simulate Attention Span");
    private final JCheckBox useBehavioralVariability = new JCheckBox("Use Behavioral Variability");
    private final JCheckBox useNonLinearIntervals = new JCheckBox("Use Non-Linear Intervals");
    private final JCheckBox dynamicActivityIntensity = new JCheckBox("Dynamic Activity Intensity");
    private final JCheckBox dynamicActivity = new JCheckBox("Dynamic Activity");

    public ActivityPanel() {

        usePlayStyle.setToolTipText("Main component of the activity system. Play styles is to simulate different types of play styles/attention spans.");
        useRandomIntervals.setToolTipText("Randomizes the action cooldown intervals. (Not recommended for human-like behavior)");
        simulateFatigue.setToolTipText("Simulates fatigue by slowing down the mouse movements the longer the player is logged in. (This is barely noticeable to the naked eye)");
        simulateAttentionSpan.setToolTipText("Simulates attention span by switching between different play styles. (Profile switching must be enabled for this to work)");
        useBehavioralVariability.setToolTipText("Randomizes the action cooldown intervals based on the current play style. (This is recommended for human-like behavior)");
        useNonLinearIntervals.setToolTipText("Anti-fingerprinting feature. Slightly drifts the action cooldown intervals in the current play style to avoid pattern profiling.");
        dynamicActivityIntensity.setToolTipText("Simulates dynamic intensity based on the current activity by adjusting the mouse speed and accuracy.");
        dynamicActivity.setToolTipText("Detects activity changes and adjusts settings accordingly. (Required for contextual variability to work)");

        setLayout(new GridBagLayout());
        setBackground(ColorScheme.DARK_GRAY_HOVER_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        add(usePlayStyle, gbc);
        add(useRandomIntervals, gbc);
        add(simulateFatigue, gbc);
        add(simulateAttentionSpan, gbc);
        add(useBehavioralVariability, gbc);
        add(useNonLinearIntervals, gbc);
        add(dynamicActivityIntensity, gbc);
        add(dynamicActivity, gbc);

        setupActionListeners();

    }

    private void setupActionListeners() {
        usePlayStyle.addActionListener(e -> Rs2AntibanSettings.usePlayStyle = usePlayStyle.isSelected());
        useRandomIntervals.addActionListener(e -> Rs2AntibanSettings.randomIntervals = useRandomIntervals.isSelected());
        simulateFatigue.addActionListener(e -> Rs2AntibanSettings.simulateFatigue = simulateFatigue.isSelected());
        simulateAttentionSpan.addActionListener(e -> Rs2AntibanSettings.simulateAttentionSpan = simulateAttentionSpan.isSelected());
        useBehavioralVariability.addActionListener(e -> Rs2AntibanSettings.behavioralVariability = useBehavioralVariability.isSelected());
        useNonLinearIntervals.addActionListener(e -> Rs2AntibanSettings.nonLinearIntervals = useNonLinearIntervals.isSelected());
        dynamicActivityIntensity.addActionListener(e -> Rs2AntibanSettings.dynamicIntensity = dynamicActivityIntensity.isSelected());
        dynamicActivity.addActionListener(e -> Rs2AntibanSettings.dynamicActivity = dynamicActivity.isSelected());

    }

    public void updateValues() {
        usePlayStyle.setSelected(Rs2AntibanSettings.usePlayStyle);
        useRandomIntervals.setSelected(Rs2AntibanSettings.randomIntervals);
        simulateFatigue.setSelected(Rs2AntibanSettings.simulateFatigue);
        simulateAttentionSpan.setSelected(Rs2AntibanSettings.simulateAttentionSpan);
        useBehavioralVariability.setSelected(Rs2AntibanSettings.behavioralVariability);
        useNonLinearIntervals.setSelected(Rs2AntibanSettings.nonLinearIntervals);
        dynamicActivityIntensity.setSelected(Rs2AntibanSettings.dynamicIntensity);
        dynamicActivity.setSelected(Rs2AntibanSettings.dynamicActivity);
    }

}
