package net.runelite.client.plugins.microbot.pluginscheduler.condition.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

import javax.swing.JSplitPane;

import javax.swing.JTree;
import javax.swing.ListSelectionModel;

import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import lombok.extern.slf4j.Slf4j;

import net.runelite.client.plugins.microbot.pluginscheduler.condition.Condition;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.ConditionManager;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.location.AreaCondition;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.location.LocationCondition;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.location.PositionCondition;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.location.RegionCondition;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.location.ui.LocationConditionUtil;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.logical.AndCondition;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.logical.LogicalCondition;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.logical.NotCondition;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.logical.OrCondition;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.resource.BankItemCountCondition;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.resource.GatheredResourceCondition;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.resource.InventoryItemCountCondition;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.resource.LootItemCondition;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.resource.ProcessItemCondition;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.resource.ui.ResourceConditionPanelUtil;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.skill.SkillLevelCondition;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.skill.SkillXpCondition;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.skill.ui.SkillConditionPanelUtil;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.time.DayOfWeekCondition;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.time.IntervalCondition;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.time.SingleTriggerTimeCondition;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.time.TimeCondition;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.time.TimeWindowCondition;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.time.ui.TimeConditionPanelUtil;
import net.runelite.client.plugins.microbot.pluginscheduler.model.PluginScheduleEntry;
import net.runelite.client.plugins.microbot.pluginscheduler.ui.SchedulerUIUtils;
import net.runelite.client.plugins.microbot.pluginscheduler.condition.ui.renderer.ConditionTreeCellRenderer;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

// Import the utility class
import net.runelite.client.plugins.microbot.pluginscheduler.condition.ui.util.ConditionConfigPanelUtil;

@Slf4j
public class ConditionConfigPanel extends JPanel {
    public static final Color BRAND_BLUE = new Color(25, 130, 196);
    private final JComboBox<String> conditionCategoryComboBox;
    private final JComboBox<String> conditionTypeComboBox;
    private JPanel configPanel;
    
    private ConditionTreeCellRenderer conditionTreeCellRenderer;
    
    // Tree visualization components
    private DefaultMutableTreeNode rootNode;
    private DefaultTreeModel treeModel;
    private JTree conditionTree;
    private JSplitPane splitPane;
    
    // Condition list components
    private DefaultListModel<String> conditionListModel;
    private JList<String> conditionList;
    
    private Consumer<LogicalCondition> userConditionUpdateCallback;
    private Consumer<Boolean> requireAllCallback;
        
    
    private PluginScheduleEntry selectScheduledPlugin;
    // UI Controls
    private JButton saveButton;
    private JButton loadButton;
    private JButton resetButton;
    
    private JButton editButton;
    private JButton addButton;
    private JButton removeButton;    
    private JButton negateButton;    
    private JButton convertToAndButton;
    private JButton convertToOrButton;
    private JButton ungroupButton;
    private JPanel titlePanel;
    private JLabel titleLabel;
    private final boolean stopConditionPanel;
    private boolean[] updatingSelectionFlag = new boolean[1];
    List<Condition> lastRefreshConditions = new CopyOnWriteArrayList<>();
    public ConditionConfigPanel( boolean stopConditionPanel) {        
        this.stopConditionPanel = stopConditionPanel;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(1, 1, 1, 1, ColorScheme.DARK_GRAY_COLOR),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ),
                stopConditionPanel ? "Stop Conditions" : "Start Conditions",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                FontManager.getRunescapeBoldFont(),
                Color.WHITE
        ));
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        
        // Initialize title panel
        titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        titlePanel.setName("titlePanel");
        
        titleLabel = new JLabel("No plugin selected");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(FontManager.getRunescapeBoldFont());
        titlePanel.add(titleLabel);
        
        // Initialize buttons but hide them for now
        initializeSaveButton();
        initializeLoadButton();
        initializeResetButton();
        
        // Disable buttons initially
        saveButton.setEnabled(false);
        loadButton.setEnabled(false);
        resetButton.setEnabled(false);
        
        // Create a panel for the top buttons, aligned to the right
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        buttonPanel.add(loadButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(resetButton);
        
        // Add the title and buttons to the top panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        topPanel.add(titlePanel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);
        
       // Define condition categories based on ConditionType enum
        String[] conditionCategories = new String[]{
            "Time",
            "Skill",
            "Resource",
            "Location",
            "NPC"
        };
        
        conditionCategoryComboBox = new JComboBox<>(conditionCategories);
        
        // Initialize with empty condition types - will be populated based on category
        conditionTypeComboBox = new JComboBox<>();
        
        // Set initial condition types based on first category
        updateConditionTypes(conditionCategories[0]);
            
        // Create split pane for main content
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.6); // Give more space to the top components
        splitPane.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        
        // Initialize condition list
        JPanel listPanel = createConditionListPanel();
        
        // Initialize condition tree
        JPanel treePanel = createLogicalTreePanel();
        
        // Create a panel for the list and tree components
        JPanel conditionsPanel = new JPanel(new BorderLayout());
        conditionsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        
        // Remove split pane and just use the tree panel directly
        conditionsPanel.add(treePanel, BorderLayout.CENTER);
        
        // If you want to keep the list panel in the code but hidden for now,
        // you can add this commented code:
        // JSplitPane conditionsSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        // conditionsSplitPane.setTopComponent(treePanel);
        // conditionsSplitPane.setBottomComponent(listPanel);
        // conditionsSplitPane.setResizeWeight(0.9); // Give almost all space to the tree
        // conditionsSplitPane.setBorder(null);
        // conditionsSplitPane.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        // conditionsPanel.add(conditionsSplitPane, BorderLayout.CENTER);
        
        // Create the condition config panel
        JPanel addConditionPanel = createAddConditionPanel();
        
        // Add both panels to the main split pane
        splitPane.setTopComponent(conditionsPanel);
        splitPane.setBottomComponent(addConditionPanel);
        
        add(splitPane, BorderLayout.CENTER);
        
        // Initialize the config panel
        updateConfigPanel();
        
        // Set up tree and list selection synchronization
        fixSelectionPersistence(false);
    }

    /**
     * Updates the condition types dropdown based on the selected category
     */
    private void updateConditionTypes(String category) {
        conditionTypeComboBox.removeAllItems();
        
        switch (category) {
            case "Time":
                if (stopConditionPanel) {
                    conditionTypeComboBox.addItem("Time Duration");
                    conditionTypeComboBox.addItem("Time Window");
                    conditionTypeComboBox.addItem("Not In Time Window");
                } else {
                    conditionTypeComboBox.addItem("Time Window");
                    conditionTypeComboBox.addItem("Outside Time Window");
                    conditionTypeComboBox.addItem("Day of Week");
                }
                break;
            case "Skill":
                if (stopConditionPanel) {
                    conditionTypeComboBox.addItem("Skill Level");
                    conditionTypeComboBox.addItem("Skill XP Goal");
                } else {
                    conditionTypeComboBox.addItem("Skill Level Required");
                }
                break;
            case "Resource":
                if (stopConditionPanel) {                    
                    conditionTypeComboBox.addItem("Item Collection");
                    conditionTypeComboBox.addItem("Process Items");
                    conditionTypeComboBox.addItem("Gather Resources");
                } else {
                    conditionTypeComboBox.addItem("Item Required");
                }
                break;
            case "Location":
                conditionTypeComboBox.addItem("Position");
                conditionTypeComboBox.addItem("Area");
                conditionTypeComboBox.addItem("Region");
                break;
            case "NPC":
                conditionTypeComboBox.addItem("NPC In Range");
                conditionTypeComboBox.addItem("NPC Dialog");
                break;
        }
    }
    /**
     * Fixes selection persistence in the tree and list view with improved event blocking
     */
    private void fixSelectionPersistence( boolean syncWithList) {
        if(! syncWithList){
            // Create a tree selection listener that only updates button states
            conditionTree.addTreeSelectionListener(e -> {
                if (!updatingSelectionFlag[0]) {
                    updateLogicalButtonStates();
                    // Update the condition editor when a condition is selected
                    updateConditionPanelForSelectedNode();
                }
            });     
            return; 
        }         
        // Store in a class field to allow other methods to access it            
        // Create a tree selection listener that doesn't trigger when programmatically updating
        conditionTree.addTreeSelectionListener(e -> {
            if (updatingSelectionFlag[0]) return;
            
            updateLogicalButtonStates();
            // Update the condition editor when a condition is selected
            updateConditionPanelForSelectedNode();
            
            // Sync with list - only if there's a valid selection
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) conditionTree.getLastSelectedPathComponent();
            if (node != null && node.getUserObject() instanceof Condition) {
                Condition condition = (Condition) node.getUserObject();
                int index = getCurrentConditions().indexOf(condition);
                if (index >= 0) {
                    try {
                        updatingSelectionFlag[0] = true;
                        conditionList.setSelectedIndex(index);
                    } finally {
                        updatingSelectionFlag[0] = false;
                    }
                }
            }
        });
        
        // Create a list selection listener that doesn't trigger when programmatically updating
        conditionList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || updatingSelectionFlag[0]) return;
            
            int index = conditionList.getSelectedIndex();
            if (index >= 0 && index < getCurrentConditions().size()) {
                try {
                    updatingSelectionFlag[0] = true;
                    selectNodeForCondition(getCurrentConditions().get(index));
                } finally {
                    updatingSelectionFlag[0] = false;
                }
            }
        });
    }
    /**
     * Gets the current conditions from the selected plugin
     * @return List of conditions, or empty list if no plugin selected
     */
    private List<Condition> getCurrentConditions() {
        if (selectScheduledPlugin == null) {
            return new ArrayList<>();
        }else if (this.stopConditionPanel && selectScheduledPlugin.getStopConditionManager() != null) {
            return selectScheduledPlugin.getStopConditions();
        }else if (!this.stopConditionPanel && selectScheduledPlugin.getStartConditionManager() != null) {
            return selectScheduledPlugin.getStartConditions();
        }
        return new ArrayList<>();
    }
    
   
    
  /**
 * Refreshes the UI to display the current plugin conditions
 * while preserving selection and expansion state
 */
    private void refreshDisplay() {      
        if (selectScheduledPlugin == null) {
            log.debug("refreshDisplay: No plugin selected, skipping refresh");
            return;
        }
        
        List<Condition> currentConditions = getCurrentConditions();
        log.debug("refreshDisplay: Found {} conditions in plugin", currentConditions.size());
        
        // Store both list and tree selection states with better debugging
        int selectedListIndex = conditionList.getSelectedIndex();
        log.debug("refreshDisplay: Current list selection index: {}", selectedListIndex);
        
        // list selection tracking
        Condition selectedListCondition = null;
        if (selectedListIndex >= 0 && selectedListIndex < currentConditions.size()) {
            selectedListCondition = currentConditions.get(selectedListIndex);
            log.debug("refreshDisplay: List selection mapped to condition: {}", 
                    selectedListCondition.getDescription());
        }
        
        // Remember tree selection with better logging
        Set<Condition> selectedTreeConditions = new HashSet<>();
        TreePath[] selectedTreePaths = conditionTree.getSelectionPaths();
        if (selectedTreePaths != null && selectedTreePaths.length > 0) {
            log.debug("refreshDisplay: Found {} selected tree paths", selectedTreePaths.length);
            for (TreePath path : selectedTreePaths) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                if (node != null && node.getUserObject() instanceof Condition) {
                    Condition condition = (Condition) node.getUserObject();
                    selectedTreeConditions.add(condition);
                    log.debug("refreshDisplay: Added selected tree condition: {}", 
                            condition.getDescription());
                }
            }
        } else {
            log.debug("refreshDisplay: No tree paths selected");
        }
        
        // robust expansion state tracking with better debugging
        Set<Condition> expandedConditions = new HashSet<>();
        Map<Condition, TreePath> expandedPathMap = new HashMap<>(); // Store path for easier restoration
        
        // First check if root node exists
        if (rootNode != null) {
            TreePath rootPath = new TreePath(rootNode.getPath());
            log.debug("refreshDisplay: Getting expanded nodes from root path: {}", rootPath);
            
            Enumeration<TreePath> expandedPaths = conditionTree.getExpandedDescendants(rootPath);
            if (expandedPaths != null && expandedPaths.hasMoreElements()) {
                log.debug("refreshDisplay: Found expanded paths");
                int expandedCount = 0;
                while (expandedPaths.hasMoreElements()) {
                    TreePath path = expandedPaths.nextElement();
                    expandedCount++;
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                    if (node != null && node.getUserObject() instanceof Condition) {
                        Condition condition = (Condition) node.getUserObject();
                        expandedConditions.add(condition);
                        expandedPathMap.put(condition, path);
                        log.debug("refreshDisplay: Added expanded condition: {}", 
                                condition.getDescription());
                    }
                }
                log.debug("refreshDisplay: Found {} expanded paths, {} are conditions", 
                        expandedCount, expandedConditions.size());
            } else {
                log.debug("refreshDisplay: No expanded paths found");
            }
        } else {
            log.debug("refreshDisplay: Root node is null, can't get expanded paths");
        }
        
        // Flag to track update types needed
        boolean needsStructureUpdate = false;  // Complete rebuild needed
        boolean needsTextUpdate = false;       // Just text needs refreshing
        
        // Check if structure has changed
        if (lastRefreshConditions.size() != currentConditions.size()) {
            log.debug("refreshDisplay: Condition count changed from {} to {}, structure update needed", 
                    lastRefreshConditions.size(), currentConditions.size());
            needsStructureUpdate = true;
        } else {
            // Check if conditions have changed or reordered
            for (int i = 0; i < lastRefreshConditions.size(); i++) {
                if (!lastRefreshConditions.get(i).equals(currentConditions.get(i))) {
                    log.debug("refreshDisplay: Condition at index {} changed, structure update needed", i);
                    needsStructureUpdate = true;
                    break;
                }
            }
            
            // If structure unchanged, check if descriptions need updating
            if (!needsStructureUpdate) {
                for (int i = 0; i < currentConditions.size(); i++) {
                    String existingDesc = conditionListModel.getElementAt(i);
                    String newDesc = descriptionForCondition(currentConditions.get(i));
                    if (!existingDesc.equals(newDesc)) {
                        log.debug("refreshDisplay: Description at index {} changed from '{}' to '{}', text update needed", 
                                i, existingDesc, newDesc);
                        needsTextUpdate = true;
                        break;
                    }
                }
            }
        }
        
        // Use a flag to prevent selection events during refresh
        updatingSelectionFlag[0] = true;
        log.debug("refreshDisplay: Setting updatingSelectionFlag to prevent event feedback");
        
        try {
            // Case 1: Full structure update needed
            if (needsStructureUpdate) {
                log.debug("refreshDisplay: Performing full structure update");
                lastRefreshConditions = new CopyOnWriteArrayList<>(currentConditions);
                
                // Update list model
                conditionListModel.clear();
                for (Condition condition : currentConditions) {
                    conditionListModel.addElement(descriptionForCondition(condition));
                }
                
                // Update tree
                updateTreeFromConditions();
                
                // Expand all category nodes by default
                for (int i = 0; i < conditionTree.getRowCount(); i++) {
                    TreePath path = conditionTree.getPathForRow(i);
                    if (path == null) continue;
                    
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                    if (node.getUserObject() instanceof String) {
                        log.debug("refreshDisplay: Auto-expanding category node: {}", node.getUserObject());
                        conditionTree.expandPath(path);
                    }
                }
                
                // Restore expansion state for condition nodes
                if (!expandedConditions.isEmpty()) {
                    log.debug("refreshDisplay: Restoring {} expanded conditions", expandedConditions.size());
                    
                    for (int i = 0; i < conditionTree.getRowCount(); i++) {
                        TreePath path = conditionTree.getPathForRow(i);
                        if (path == null) continue;
                        
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                        if (node.getUserObject() instanceof Condition) {
                            Condition condition = (Condition) node.getUserObject();
                            if (expandedConditions.contains(condition)) {
                                log.debug("refreshDisplay: Expanding condition node: {}", condition.getDescription());
                                conditionTree.expandPath(path);
                            }
                        }
                    }
                } else {
                    log.debug("refreshDisplay: No expanded conditions to restore");
                }
                
                // Restore selection state
                if (!selectedTreeConditions.isEmpty()) {
                    log.debug("refreshDisplay: Restoring {} selected tree conditions", selectedTreeConditions.size());
                    List<TreePath> pathsToSelect = new ArrayList<>();
                    
                    // Find paths to all selected conditions
                    for (int i = 0; i < conditionTree.getRowCount(); i++) {
                        TreePath path = conditionTree.getPathForRow(i);
                        if (path == null) continue;
                        
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                        if (node.getUserObject() instanceof Condition) {
                            Condition condition = (Condition) node.getUserObject();
                            if (selectedTreeConditions.contains(condition)) {
                                pathsToSelect.add(path);
                                log.debug("refreshDisplay: Found path for selected condition: {}", 
                                        condition.getDescription());
                            }
                        }
                    }
                    
                    if (!pathsToSelect.isEmpty()) {
                        log.debug("refreshDisplay: Setting {} tree selection paths", pathsToSelect.size());
                        conditionTree.setSelectionPaths(pathsToSelect.toArray(new TreePath[0]));
                    } else {
                        log.debug("refreshDisplay: Could not find any paths for selected conditions");
                    }
                } else {
                    log.debug("refreshDisplay: No tree selections to restore");
                }
                
                // Restore list selection
                if (selectedListCondition != null) {
                    int newIndex = currentConditions.indexOf(selectedListCondition);
                    if (newIndex >= 0) {
                        log.debug("refreshDisplay: Restoring list selection to index {}", newIndex);
                        conditionList.setSelectedIndex(newIndex);
                    } else {
                        log.debug("refreshDisplay: Could not find list selection in current conditions");
                    }
                }
            }
            // Case 2: Only text descriptions need updating
            else if (needsTextUpdate) {
                log.debug("refreshDisplay: Performing text-only update");
                
                // Update just the text in the list model without rebuilding
                for (int i = 0; i < currentConditions.size(); i++) {
                    String newDesc = descriptionForCondition(currentConditions.get(i));
                    if (!conditionListModel.getElementAt(i).equals(newDesc)) {
                        log.debug("refreshDisplay: Updating description at index {} to '{}'", i, newDesc);
                        conditionListModel.setElementAt(newDesc, i);
                    }
                }
                
                // Update tree nodes' text by forcing renderer refresh without rebuilding
                log.debug("refreshDisplay: Repainting tree to refresh node text");
                conditionTree.repaint();
            } else {
                log.debug("refreshDisplay: No updates needed");
            }
        } finally {
            // Re-enable selection events
            updatingSelectionFlag[0] = false;
            log.debug("refreshDisplay: Resetting updatingSelectionFlag to allow events");
        }
    }

    /**
     * Helper method to get consistent description for a condition
     */
    private String descriptionForCondition(Condition condition) {
        // Check if this is a plugin-defined condition
        boolean isPluginDefined = false;
    
        if (this.selectScheduledPlugin!= null && getConditionManger() != null) {

            isPluginDefined = getConditionManger().isPluginDefinedCondition(condition);
        }
        
        
        // Add with appropriate tag for plugin-defined conditions
        String description = condition.getDescription();
        if (isPluginDefined) {
            description = "[Plugin] " + description;
        }
        
        return description;
    }
    /**
     * Updates the panel when a new plugin is selected
     * 
     * @param selectedPlugin The newly selected plugin, or null if selection cleared
     */
    public void setSelectScheduledPlugin(PluginScheduleEntry selectedPlugin) {
        if (selectedPlugin == this.selectScheduledPlugin) {            
            return;
        }else{
            log.info("setSelectScheduledPlugin: Changing selected plugin from {} to {} - reload list and tree", 
                    this.selectScheduledPlugin==null ? "null": this.selectScheduledPlugin.getCleanName() , selectedPlugin==null ? "null" : selectedPlugin.getCleanName());  
        }
                    
        // Store the selected plugin
        this.selectScheduledPlugin = selectedPlugin;
        
        // Enable/disable controls based on whether a plugin is selected
        boolean hasPlugin = (selectedPlugin != null);
        
        saveButton.setEnabled(hasPlugin);
        loadButton.setEnabled(hasPlugin);
        resetButton.setEnabled(hasPlugin);
        editButton.setEnabled(hasPlugin);
        addButton.setEnabled(hasPlugin);
        removeButton.setEnabled(hasPlugin);        
        conditionTypeComboBox.setEnabled(hasPlugin);
        conditionList.setEnabled(hasPlugin);
        conditionTree.setEnabled(hasPlugin);
        
        // Update the plugin name display
        setScheduledPluginNameLabel();
        
        // If a plugin is selected, load its conditions
        if (hasPlugin) {
            // Set the logic type combo box based on the plugin's condition manager
            boolean requireAll = selectedPlugin.getStopConditionManager().requiresAll();
            
            // Load the conditions from the plugin
            if (selectedPlugin.getStopConditionManager() != null) {
                // Load conditions from the plugin's condition manager
                loadConditions(selectedPlugin.getStopConditions(), requireAll);
            } else {
                // Load conditions directly from the plugin
                loadConditions(selectedPlugin.getStartConditions(), requireAll);
            }            
        } else {
            // Clear conditions if no plugin selected
            loadConditions(new ArrayList<>(), true);
        }
        
        // Update the tree and list displays
        refreshDisplay();
    }
    
      
    private JPanel createConditionListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR),
                "Condition List",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                FontManager.getRunescapeBoldFont(), // Changed to bold font
                Color.WHITE
        ));
        
        conditionListModel = new DefaultListModel<>();
        conditionList = new JList<>(conditionListModel);
        conditionList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, 
                                                         boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                
                List<Condition> currentConditions = getCurrentConditions();
                if (index >= 0 && index < currentConditions.size()) {
                    Condition condition = currentConditions.get(index);
                    
                        if (selectScheduledPlugin != null && 
                            getConditionManger() != null &&
                            getConditionManger().isPluginDefinedCondition(condition)) {
                            
                            if (!isSelected) {
                                setForeground(new Color(0, 128, 255)); // Blue for plugin conditions
                            }
                            setFont(getFont().deriveFont(Font.ITALIC)); // Italic for plugin conditions
                        }                    
                }
                
                return c;
            }
        });
        conditionList.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        conditionList.setForeground(Color.WHITE);
        conditionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        /*conditionList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int index = conditionList.getSelectedIndex();
                if (index >= 0 && index < getCurrentConditions().size()) {
                    // Select corresponding node in tree
                    selectNodeForCondition(getCurrentConditions().get(index));
                }
            }
        });*/
        
        JScrollPane scrollPane = new JScrollPane(conditionList);
        scrollPane.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        scrollPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // For smoother scrolling
        
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }
    /**
    * Creates the add condition panel with condition type selector and controls
    */
    private JPanel createAddConditionPanel() {
        // Create a panel with border to clearly separate this section
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR),
                "Add New Condition",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                FontManager.getRunescapeBoldFont(),
                Color.WHITE
        ));

        // Create a main content panel that will be scrollable
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        // Add condition type selector with a more descriptive label
        JPanel selectorPanel = new JPanel(new BorderLayout(5, 0));
        selectorPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        selectorPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        // Create a panel for both dropdowns
        JPanel dropdownsPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        dropdownsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        
        // Add category selector
        JLabel categoryLabel = new JLabel("Condition Category:");
        categoryLabel.setForeground(Color.WHITE);
        categoryLabel.setFont(FontManager.getRunescapeSmallFont());
        dropdownsPanel.add(categoryLabel);
        
        // Style the category combobox
        SchedulerUIUtils.styleComboBox(conditionCategoryComboBox);
        conditionCategoryComboBox.addActionListener(e -> {
            String selectedCategory = (String) conditionCategoryComboBox.getSelectedItem();
            if (selectedCategory != null) {
                updateConditionTypes(selectedCategory);
                updateConfigPanel();
            }
        });
        dropdownsPanel.add(conditionCategoryComboBox);
        
        // Add type selector
        JLabel typeLabel = new JLabel("Condition Type:");
        typeLabel.setForeground(Color.WHITE);
        typeLabel.setFont(FontManager.getRunescapeSmallFont());
        dropdownsPanel.add(typeLabel);
        
        // Style the type combobox
        SchedulerUIUtils.styleComboBox(conditionTypeComboBox);
        conditionTypeComboBox.addActionListener(e -> updateConfigPanel());
        dropdownsPanel.add(conditionTypeComboBox);
        
        selectorPanel.add(dropdownsPanel, BorderLayout.CENTER);
        
        // Config panel with scroll pane for better visibility
        configPanel = new JPanel(new BorderLayout());
        configPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        configPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JScrollPane configScrollPane = new JScrollPane(configPanel);
        configScrollPane.setBorder(BorderFactory.createEmptyBorder());
        configScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        configScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        configScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Button panel with improved spacing
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 5, 0));
        buttonPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        buttonPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        this.addButton = ConditionConfigPanelUtil.createButton("Add", ColorScheme.PROGRESS_COMPLETE_COLOR);
        addButton.addActionListener(e -> addCurrentCondition());
        buttonPanel.add(addButton);

        this.editButton = ConditionConfigPanelUtil.createButton("Edit", ColorScheme.BRAND_ORANGE);
        editButton.addActionListener(e -> editSelectedCondition());
        buttonPanel.add(editButton);

        this.removeButton = ConditionConfigPanelUtil.createButton("Remove", ColorScheme.PROGRESS_ERROR_COLOR);
        removeButton.addActionListener(e -> removeSelectedCondition());
        buttonPanel.add(removeButton);

        // Add components to content panel
        contentPanel.add(selectorPanel, BorderLayout.NORTH);
        contentPanel.add(configScrollPane, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add content panel to main panel
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }
        /**
     * Creates the logical condition tree panel with controls
     */
    private JPanel createLogicalTreePanel() {
    // Use the utility method instead of duplicating code
    JPanel panel = ConditionConfigPanelUtil.createTitledPanel("Condition Structure");
    panel.setLayout(new BorderLayout());
    
    // Initialize tree
    initializeConditionTree(panel);
    
    // Add logical operations toolbar
    JPanel logicalOpPanel = createLogicalOperationsToolbar();
    panel.add(logicalOpPanel, BorderLayout.SOUTH);
    
    return panel;
}

    private JPanel createLogicalOperationsToolbar() {
        // Create the panel
        JPanel logicalOpPanel = new JPanel();
        logicalOpPanel.setLayout(new BoxLayout(logicalOpPanel, BoxLayout.X_AXIS));
        logicalOpPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        logicalOpPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        
        // Group operations section
        JButton createAndButton = ConditionConfigPanelUtil.createButton("Group as AND", ColorScheme.BRAND_ORANGE);
        createAndButton.setToolTipText("Group selected conditions with AND logic");
        createAndButton.addActionListener(e -> createLogicalGroup(true));
        
        JButton createOrButton = ConditionConfigPanelUtil.createButton("Group as OR", BRAND_BLUE);
        createOrButton.setToolTipText("Group selected conditions with OR logic");
        createOrButton.addActionListener(e -> createLogicalGroup(false));
        
        // Negation button
        JButton negateButton = ConditionConfigPanelUtil.createButton("Negate", new Color(220, 50, 50));
        negateButton.setToolTipText("Negate the selected condition (toggle NOT)");
        negateButton.addActionListener(e -> negateSelectedCondition());
        
        
        // Convert operation buttons
        JButton convertToAndButton = ConditionConfigPanelUtil.createButton("Convert to AND", ColorScheme.BRAND_ORANGE);
        convertToAndButton.setToolTipText("Convert selected logical group to AND type");
        convertToAndButton.addActionListener(e -> convertLogicalType(true));
        
        
        JButton convertToOrButton = ConditionConfigPanelUtil.createButton("Convert to OR", BRAND_BLUE);
        convertToOrButton.setToolTipText("Convert selected logical group to OR type");
        convertToOrButton.addActionListener(e -> convertLogicalType(false));
        
        
        // Ungroup button
        JButton ungroupButton = ConditionConfigPanelUtil.createButton("Ungroup", ColorScheme.LIGHT_GRAY_COLOR);
        ungroupButton.setToolTipText("Remove the logical group but keep its conditions");
        ungroupButton.addActionListener(e -> ungroupSelectedLogical());
        
        
        // Add buttons to panel with separators
        logicalOpPanel.add(createAndButton);
        logicalOpPanel.add(Box.createHorizontalStrut(5));
        logicalOpPanel.add(createOrButton);
        logicalOpPanel.add(Box.createHorizontalStrut(10));
        logicalOpPanel.add(new JSeparator(SwingConstants.VERTICAL));
        logicalOpPanel.add(Box.createHorizontalStrut(10));
        logicalOpPanel.add(negateButton);
        logicalOpPanel.add(Box.createHorizontalStrut(10));
        logicalOpPanel.add(new JSeparator(SwingConstants.VERTICAL));
        logicalOpPanel.add(Box.createHorizontalStrut(10));
        logicalOpPanel.add(convertToAndButton);
        logicalOpPanel.add(Box.createHorizontalStrut(5));
        logicalOpPanel.add(convertToOrButton);
        logicalOpPanel.add(Box.createHorizontalStrut(10));
        logicalOpPanel.add(new JSeparator(SwingConstants.VERTICAL));
        logicalOpPanel.add(Box.createHorizontalStrut(10));
        logicalOpPanel.add(ungroupButton);
        
        // Store references to buttons that need context-sensitive enabling
        this.negateButton = negateButton;
        this.convertToAndButton = convertToAndButton;
        this.convertToOrButton = convertToOrButton;
        this.ungroupButton = ungroupButton;
        
        // Initial state - disable all by default
        negateButton.setEnabled(false);
        convertToAndButton.setEnabled(false);
        convertToOrButton.setEnabled(false);
        ungroupButton.setEnabled(false);
        
        return logicalOpPanel;
    }

    private void initializeConditionTree(JPanel panel) {
        rootNode = new DefaultMutableTreeNode("Conditions");
        treeModel = new DefaultTreeModel(rootNode);
        conditionTree = new JTree(treeModel);
        conditionTree.setRootVisible(false);
        conditionTree.setShowsRootHandles(true);
        this.conditionTreeCellRenderer = new ConditionTreeCellRenderer(getConditionManger());
        conditionTree.setCellRenderer(this.conditionTreeCellRenderer);
        
        conditionTree.getSelectionModel().setSelectionMode(
            TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION); // Enable multi-select
        
        // Add popup menu for right-click operations
        JPopupMenu popupMenu = createTreePopupMenu();
        conditionTree.setComponentPopupMenu(popupMenu);
        
        // Add selection listener to update button states and handle selection synchronization
        fixSelectionPersistence(false);
        
        // Create scroll pane with the utility method
        JScrollPane treeScrollPane = ConditionConfigPanelUtil.createScrollPane(conditionTree);
        treeScrollPane.setPreferredSize(new Dimension(400, 300));
        
        panel.add(treeScrollPane, BorderLayout.CENTER);
    }

    

    private void updateConfigPanel() {
        configPanel.removeAll();
    
        // Create a main panel with GridBagLayout for flexibility
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Get selected category and type
        String selectedCategory = (String) conditionCategoryComboBox.getSelectedItem();
        String selectedType = (String) conditionTypeComboBox.getSelectedItem();
        
        if (selectedCategory == null || selectedType == null) {
            // No selection, show empty panel
            configPanel.add(panel, BorderLayout.NORTH);
            return;
        }
        
        // Add condition type header
        JLabel typeHeaderLabel = new JLabel("Configure " + selectedType + " Condition");
        typeHeaderLabel.setForeground(Color.WHITE);
        typeHeaderLabel.setFont(FontManager.getRunescapeBoldFont());
        panel.add(typeHeaderLabel, gbc);
        gbc.gridy++;
        
        // Add a separator for visual clarity
        JSeparator separator = new JSeparator();
        separator.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        panel.add(separator, gbc);
        gbc.gridy++;
        
        // Create the appropriate config panel based on the selected category and type
        if ("Location".equals(selectedCategory)) {
            // Use LocationConditionUtil for location-based conditions
            LocationConditionUtil.createLocationConditionPanel(panel, gbc, configPanel);
        }else if (stopConditionPanel) {
            switch (selectedType) {
                case "Time Duration":
                    TimeConditionPanelUtil.createIntervalConfigPanel(panel, gbc, panel);
                    break;
                case "Time Window":
                    TimeConditionPanelUtil.createEnhancedTimeWindowConfigPanel(panel, gbc, panel);
                    break;
                case "Not In Time Window":
                    TimeConditionPanelUtil.createEnhancedTimeWindowConfigPanel(panel, gbc, panel);
                    // Store whether we want inside or outside the window
                    configPanel.putClientProperty("withInWindow", false);
                    break; 
                case "Skill Level":
                    SkillConditionPanelUtil.createSkillLevelConfigPanel(panel, gbc, panel, true);
                    break;
                case "Skill XP Goal":
                    SkillConditionPanelUtil.createSkillXpConfigPanel(panel, gbc, panel);
                    break;
                case "Item Collection":
                    ResourceConditionPanelUtil.createItemConfigPanel(panel, gbc, panel, true);
                    break;
                case "Process Items":
                    ResourceConditionPanelUtil.createProcessItemPanel(panel, gbc, configPanel);
                    break;
                case "Gather Resources":
                    ResourceConditionPanelUtil.createGatheredResourcePanel(panel, gbc, configPanel);
                    break;               

                default:
                    JLabel notImplementedLabel = new JLabel("This Stop condition type is not yet implemented");
                    notImplementedLabel.setForeground(Color.RED);
                    panel.add(notImplementedLabel, gbc);
                    break;
            }
        } else {
            // This is for start conditions 
            switch (selectedType) {
                case "Time Window":
                    TimeConditionPanelUtil.createEnhancedTimeWindowConfigPanel(panel, gbc, panel);
                    break;
                case "Outside Time Window":
                    TimeConditionPanelUtil.createEnhancedTimeWindowConfigPanel(panel, gbc, panel);
                    // Store whether we want inside or outside the window
                    panel.putClientProperty("withInWindow", false);
                    break; 
                case "Day of Week":
                    TimeConditionPanelUtil.createDayOfWeekConfigPanel(panel, gbc, panel);
                    break;
                case "Skill Level Required":
                    SkillConditionPanelUtil.createSkillLevelConfigPanel(panel, gbc, panel, false);
                    break;
                case "Item Required":
                    ResourceConditionPanelUtil.createInventoryItemCountPanel(panel, gbc, panel);
                    break;
                default:
                    JLabel notImplementedLabel = new JLabel("This Start condition type is not yet implemented");
                    notImplementedLabel.setForeground(Color.RED);
                    panel.add(notImplementedLabel, gbc);
                    break;
            }
        }
        
        // Add the panel to the config panel
        configPanel.add(panel, BorderLayout.NORTH);
        
        // Add a filler panel to push everything to the top
        JPanel fillerPanel = new JPanel();
        fillerPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        configPanel.add(fillerPanel, BorderLayout.CENTER);
        
        configPanel.revalidate();
        configPanel.repaint();
        configPanel.putClientProperty("localConditionPanel", panel);
    }
    
    public void setUserConditionUpdateCallback(Consumer<LogicalCondition> callback) {
        this.userConditionUpdateCallback = callback;
    }
        
        
    
   
    private void editSelectedCondition() {
        int selectedIndex = conditionList.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= getCurrentConditions().size()) {
            return;
        }
               
        removeSelectedCondition();
    }
       
    
    private void loadConditions(List<Condition> conditionList, boolean requireAll) {
        boolean needsUpdate = false;
        for (Condition condition : conditionList) {
            if (! lastRefreshConditions.contains(condition)){
                needsUpdate = true;
            }
        }
        conditionListModel.clear();
        
        if (conditionList != null) {
            for (Condition condition : conditionList) {
                // Check if this is a plugin-defined condition
                boolean isPluginDefined = false;
                           
                if (selectScheduledPlugin != null && getConditionManger() != null) {
                    isPluginDefined = getConditionManger().isPluginDefinedCondition(condition);
                }
                
                
                // Add with appropriate tag for plugin-defined conditions
                String description = condition.getDescription();
                if (isPluginDefined) {
                    description = "[Plugin] " + description;
                }
                
                conditionListModel.addElement(description);
            }
        }
                        
        updateTreeFromConditions();
    }
    /**
    * Updates the tree from conditions while preserving selection and expansion state
    */
    private void updateTreeFromConditions() {
        // Store selected conditions and expanded state before rebuilding
        Set<Condition> selectedConditions = new HashSet<>();
        Set<Condition> expandedConditions = new HashSet<>();
        
        // Remember selected conditions
        TreePath[] selectedPaths = conditionTree.getSelectionPaths();
        if (selectedPaths != null) {
            for (TreePath path : selectedPaths) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                if (node.getUserObject() instanceof Condition) {
                    selectedConditions.add((Condition) node.getUserObject());
                }
            }
        }
        
        // Remember expanded nodes
        Enumeration<TreePath> expandedPaths = conditionTree.getExpandedDescendants(new TreePath(rootNode.getPath()));
        if (expandedPaths != null) {
            while (expandedPaths.hasMoreElements()) {
                TreePath path = expandedPaths.nextElement();
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                if (node.getUserObject() instanceof Condition) {
                    expandedConditions.add((Condition) node.getUserObject());
                }
            }
        }
        
        // Clear and rebuild tree
        rootNode.removeAllChildren();
        
        if (selectScheduledPlugin == null) {
            treeModel.nodeStructureChanged(rootNode);
            return;
        }
        
        // Build the tree from the condition manager
        ConditionManager manager = getConditionManger();
        log.info ("updateTreeFromConditions: Building tree for plugin {} with {} conditions", 
                selectScheduledPlugin.getCleanName(), manager.getConditions().size());
        // If there is a plugin condition, show plugin and user sections separately
        if (manager.getPluginCondition() != null && !manager.getPluginCondition().getConditions().isEmpty()) {
            // Add plugin section
            DefaultMutableTreeNode pluginNode = new DefaultMutableTreeNode("Plugin Conditions");
            rootNode.add(pluginNode);
            buildConditionTree(pluginNode, manager.getPluginCondition());
            
            // Add user section if it has conditions
            if (manager.getUserLogicalCondition() != null && 
                !manager.getUserLogicalCondition().getConditions().isEmpty()) {
                
                DefaultMutableTreeNode userNode = new DefaultMutableTreeNode("User Conditions");
                rootNode.add(userNode);
                buildConditionTree(userNode, manager.getUserLogicalCondition());
            }
        } 
        // Otherwise just build from the root logical or flat conditions
        else if (manager.getUserLogicalCondition() != null) {
            LogicalCondition rootLogical = manager.getUserLogicalCondition();
            
            // For the root logical, show its children directly if it matches the selected type                    
            buildConditionTree(rootNode, rootLogical);
            
        }
        else {
            // This handles the case where we have flat conditions without logical structure
            for (Condition condition : getCurrentConditions()) {
                buildConditionTree(rootNode, condition);
            }
        }
        
        // Update tree model
        treeModel.nodeStructureChanged(rootNode);
        
        // First expand all nodes that were previously expanded
        for (int i = 0; i < conditionTree.getRowCount(); i++) {
            TreePath path = conditionTree.getPathForRow(i);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            
            if (node.getUserObject() instanceof Condition) {
                Condition condition = (Condition) node.getUserObject();
                if (expandedConditions.contains(condition)) {
                    conditionTree.expandPath(path);
                }
            } else if (node.getUserObject() instanceof String) {
                // Always expand category headers
                conditionTree.expandPath(path);
            }
        }
        
        // Then restore selection
        List<TreePath> pathsToSelect = new ArrayList<>();
        for (int i = 0; i < conditionTree.getRowCount(); i++) {
            TreePath path = conditionTree.getPathForRow(i);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            
            if (node.getUserObject() instanceof Condition) {
                Condition condition = (Condition) node.getUserObject();
                if (selectedConditions.contains(condition)) {
                    pathsToSelect.add(path);
                }
            }
        }
        
        if (!pathsToSelect.isEmpty()) {
            conditionTree.setSelectionPaths(pathsToSelect.toArray(new TreePath[0]));
        }
    }
   
    private void buildConditionTree(DefaultMutableTreeNode parent, Condition condition) {
        if (condition instanceof AndCondition) {
            AndCondition andCondition = (AndCondition) condition;
            DefaultMutableTreeNode andNode = new DefaultMutableTreeNode(andCondition);
            parent.add(andNode);
            
            for (Condition child : andCondition.getConditions()) {
                buildConditionTree(andNode, child);
            }
        } else if (condition instanceof OrCondition) {
            OrCondition orCondition = (OrCondition) condition;
            DefaultMutableTreeNode orNode = new DefaultMutableTreeNode(orCondition);
            parent.add(orNode);
            
            for (Condition child : orCondition.getConditions()) {
                buildConditionTree(orNode, child);
            }
        } else if (condition instanceof NotCondition) {
            NotCondition notCondition = (NotCondition) condition;
            DefaultMutableTreeNode notNode = new DefaultMutableTreeNode(notCondition);
            parent.add(notNode);
            
            buildConditionTree(notNode, notCondition.getCondition());
        } else {
            // Add leaf condition
            parent.add(new DefaultMutableTreeNode(condition));
        }
    }
    /**
     * Recursively searches for a tree node containing the specified condition
     */
    private DefaultMutableTreeNode findTreeNodeForCondition(DefaultMutableTreeNode parent, Condition target) {
        // Check if this node contains our target
        if (parent.getUserObject() == target) {
            return parent;
        }
        
        // Check all children
        for (int i = 0; i < parent.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(i);
            DefaultMutableTreeNode result = findTreeNodeForCondition(child, target);
            if (result != null) {
                return result;
            }
        }
        
        return null;
    }
    private void groupSelectedWithLogical(LogicalCondition logicalCondition) {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) conditionTree.getLastSelectedPathComponent();
        if (selectedNode == null || selectedNode == rootNode) {
            return;
        }
        
        // Get the parent node
        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) selectedNode.getParent();
        if (parentNode == null) {
            return;
        }
        
        // Get the selected condition
        Object userObject = selectedNode.getUserObject();
        if (!(userObject instanceof Condition)) {
            return;
        }
        
        Condition condition = (Condition) userObject;
        
        // Remove the condition from its current position
        int index = getCurrentConditions().indexOf(condition);
        if (index >= 0) {
            getCurrentConditions().remove(index);
            conditionListModel.remove(index);
        }
        
        // Add it to the new logical condition
        logicalCondition.addCondition(condition);
        
        // Add the logical condition to the list
        getCurrentConditions().add(logicalCondition);
        conditionListModel.addElement(logicalCondition.getDescription());
        
        updateTreeFromConditions();
        notifyConditionUpdate();
    }
    private void negateSelected() {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) conditionTree.getLastSelectedPathComponent();
        if (selectedNode == null || selectedNode == rootNode) {
            return;
        }
        
        // Get the selected condition
        Object userObject = selectedNode.getUserObject();
        if (!(userObject instanceof Condition)) {
            return;
        }
        
        Condition condition = (Condition) userObject;
        
        // Remove the condition from its current position
        int index = getCurrentConditions().indexOf(condition);
        if (index >= 0) {
            getCurrentConditions().remove(index);
            conditionListModel.remove(index);
        }
        
        // Create a NOT condition
        NotCondition notCondition = new NotCondition(condition);
        
        // Add the NOT condition to the list
        getCurrentConditions().add(notCondition);
        conditionListModel.addElement(notCondition.getDescription());
        
        updateTreeFromConditions();
        notifyConditionUpdate();
    }
    /**
     * Removes the selected condition, properly handling nested logic conditions
     */
    private void removeSelectedFromTree() {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) conditionTree.getLastSelectedPathComponent();
        if (selectedNode == null || selectedNode == rootNode) {
            return;
        }
        
        // Get the selected condition
        Object userObject = selectedNode.getUserObject();
        if (!(userObject instanceof Condition)) {
            return;
        }
        
        Condition condition = (Condition) userObject;
        
        // Check if this is a plugin-defined condition that shouldn't be removed
        if (selectScheduledPlugin != null && 
            getConditionManger().isPluginDefinedCondition(condition)) {
            JOptionPane.showMessageDialog(this,
                    "This condition is defined by the plugin and cannot be removed.",
                    "Plugin Condition",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
       

        
        // Get parent logical condition if any
        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) selectedNode.getParent();
        if (parentNode != rootNode && parentNode.getUserObject() instanceof LogicalCondition) {
            LogicalCondition parentLogical = (LogicalCondition) parentNode.getUserObject();
            parentLogical.removeCondition(condition);
            
            // If logical condition is now empty and not the root, remove it too
            if (parentLogical.getConditions().isEmpty() && 
                parentNode.getParent() != rootNode && 
                parentNode.getParent() instanceof DefaultMutableTreeNode) {
                
                DefaultMutableTreeNode grandparentNode = (DefaultMutableTreeNode) parentNode.getParent();
                if (grandparentNode.getUserObject() instanceof LogicalCondition) {
                    LogicalCondition grandparentLogical = (LogicalCondition) grandparentNode.getUserObject();
                    grandparentLogical.removeCondition(parentLogical);
                }
            }
        } else {
            // Direct removal from condition manager
            getConditionManger().removeCondition(condition);
        }
        
        updateTreeFromConditions();
        notifyConditionUpdate();
    }
        /**
     * Selects a tree node corresponding to the condition, preserving expansion state
     */
    private void selectNodeForCondition(Condition condition) {
        if (condition == null) {
            log.debug("selectNodeForCondition: Cannot select null condition");
            return;
        }
        
        log.debug("selectNodeForCondition: Attempting to select condition: {}", condition.getDescription());
        
        // Store current expansion state
        Set<TreePath> expandedPaths = new HashSet<>();
        Enumeration<TreePath> expanded = conditionTree.getExpandedDescendants(new TreePath(rootNode.getPath()));
        if (expanded != null) {
            while (expanded.hasMoreElements()) {
                expandedPaths.add(expanded.nextElement());
            }
            log.debug("selectNodeForCondition: Saved {} expanded paths", expandedPaths.size());
        } else {
            log.debug("selectNodeForCondition: No expanded paths to save");
        }
        
        // Find the node corresponding to the condition
        DefaultMutableTreeNode targetNode = null;
        Enumeration<TreeNode> e = rootNode.breadthFirstEnumeration();
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
            if (node.getUserObject() == condition) {
                targetNode = node;
                break;
            }
        }
        
        if (targetNode != null) {
            log.debug("selectNodeForCondition: Found node for condition: {}", condition.getDescription());
            TreePath path = new TreePath(targetNode.getPath());
            
            // Make all parent nodes visible - expanding the path as needed
            TreePath parentPath = path.getParentPath();
            if (parentPath != null) {
                log.debug("selectNodeForCondition: Expanding parent path");
                conditionTree.expandPath(parentPath);
            }
            
            // Set selection
            log.debug("selectNodeForCondition: Setting selection path");
            conditionTree.setSelectionPath(path);
            
            // Ensure the selected node is visible
            log.debug("selectNodeForCondition: Scrolling to make path visible");
            conditionTree.scrollPathToVisible(path);
            
            // Restore previously expanded paths
            log.debug("selectNodeForCondition: Restoring {} expanded paths", expandedPaths.size());
            for (TreePath expandedPath : expandedPaths) {
                conditionTree.expandPath(expandedPath);
            }
        } else {
            log.debug("selectNodeForCondition: Could not find node for condition: {}", condition.getDescription());
        }
    }
    
  
    private void initializeSaveButton() {
        saveButton = ConditionConfigPanelUtil.createButton("Save Conditions", ColorScheme.PROGRESS_COMPLETE_COLOR);
        saveButton.addActionListener(e -> saveConditionsToScheduledPlugin());
        saveButton.setEnabled(selectScheduledPlugin != null);
    }
    private void initializeLoadButton() {
        loadButton = ConditionConfigPanelUtil.createButton("Load Current Conditions", ColorScheme.PROGRESS_COMPLETE_COLOR);
        loadButton.addActionListener(e -> setSelectScheduledPlugin(selectScheduledPlugin));
        loadButton.setEnabled(selectScheduledPlugin != null);
    }
    private void initializeResetButton() {
        resetButton = ConditionConfigPanelUtil.createButton("Reset Conditions", ColorScheme.PROGRESS_ERROR_COLOR);

        resetButton.addActionListener(e -> {
                    loadConditions(new ArrayList<>(), true);
                    saveConditionsToScheduledPlugin();  
                }
            );
        
    }
    
    private void saveConditionsToScheduledPlugin() {
        if (selectScheduledPlugin == null) return;                     
        // Save to config                
        setScheduledPluginNameLabel(); // Update label
    }
    /**
     * Updates the title label with the selected plugin name using color for better visibility
    */
    private void setScheduledPluginNameLabel() {
    if (selectScheduledPlugin != null) {
        String pluginName = selectScheduledPlugin.getCleanName();
        boolean isRunning = selectScheduledPlugin.isRunning();
        boolean isEnabled = selectScheduledPlugin.isEnabled();
        
        titleLabel.setText(ConditionConfigPanelUtil.formatPluginTitle(isRunning, isEnabled, pluginName));
    } else {
        titleLabel.setText(ConditionConfigPanelUtil.formatPluginTitle(false, false, null));
    }
}

    /**
     * Notifies any external components of condition changes
     * and ensures changes are saved to the config.
     */
    private void notifyConditionUpdate() {
        if (selectScheduledPlugin == null) {
            return;
        }
        // Call any registered callback (if implementing callback pattern)
        if (userConditionUpdateCallback != null) {
            userConditionUpdateCallback.accept( getConditionManger().getUserLogicalCondition());
        }                
    }
    /**
     * Refreshes the condition list and tree if conditions have changed in the selected plugin.
     * This should be called periodically to keep the UI in sync with the plugin state.
     * 
     * @return true if conditions were refreshed, false if no changes were detected
     */
    public boolean refreshConditions() {
        if (selectScheduledPlugin == null) {
            return false;
        }
        
        refreshDisplay();
        return true;
    }
   

    private void addCurrentCondition() {
        if (selectScheduledPlugin == null) {
            JOptionPane.showMessageDialog(this, "No plugin selected", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JPanel localConfigPanel = (JPanel) configPanel.getClientProperty("localConditionPanel");
        if (localConfigPanel == null) {
            log.debug("No config panel found");
            return;
        }
        
        String selectedCategory = (String) conditionCategoryComboBox.getSelectedItem();
        String selectedType = (String) conditionTypeComboBox.getSelectedItem();
        
        if (selectedCategory == null || selectedType == null) {
            return;
        }
        
        Condition condition = null;
        try {
            // Create appropriate condition based on the type
            if ("Location".equals(selectedCategory)) {
                condition = LocationConditionUtil.createLocationCondition(localConfigPanel);
            } else if (stopConditionPanel) {
                 // Stop conditions
                switch (selectedType) {
                    case "Time Duration":
                        condition = TimeConditionPanelUtil.createIntervalCondition(localConfigPanel);
                        break;
                    case "Not In Time Window":
                        condition = TimeConditionPanelUtil.createEnhancedTimeWindowCondition(localConfigPanel);
                        break; 
                    case "Skill Level":
                        condition = SkillConditionPanelUtil.createSkillLevelCondition(localConfigPanel);
                        break;
                    case "Skill XP Goal":
                        condition = SkillConditionPanelUtil.createSkillXpCondition(localConfigPanel);
                        break;
                                      
                    case "Item Collection":
                        condition = ResourceConditionPanelUtil.createItemCondition(localConfigPanel);                                                                    
                        break;
                    case "Process Items":
                        condition = ResourceConditionPanelUtil.createProcessItemCondition(localConfigPanel);
                        break;
                    case "Gather Resources":
                        condition = ResourceConditionPanelUtil.createGatheredResourceCondition(localConfigPanel);
                        break;                   
                    case "Inventory Item Count"://TODO these are not working right now. have to update the logic-> change these here to gaathered items 
                        condition = ResourceConditionPanelUtil.createInventoryItemCountCondition(localConfigPanel);
                        break;
                    default:
                        JOptionPane.showMessageDialog(this, "Condition type not implemented", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                }
               
            } else {
                switch (selectedType) {
                    case "Time Window":
                        condition = TimeConditionPanelUtil.createEnhancedTimeWindowCondition(localConfigPanel);
                        break;
                    case "Day of Week":
                        condition = TimeConditionPanelUtil.createDayOfWeekCondition(localConfigPanel);
                        break;
                    case "Skill Level Required":
                        condition = SkillConditionPanelUtil.createSkillLevelCondition(localConfigPanel);
                        break;
                    case "Item Required":
                        //condition = ResourceConditionPanelUtil.createItemRequiredCondition(localConfigPanel);                        
                        condition = ResourceConditionPanelUtil.createInventoryItemCountCondition(localConfigPanel);                                            
                        break;     
                    default:
                        JOptionPane.showMessageDialog(this, "Condition type not implemented", "Error", JOptionPane.ERROR_MESSAGE);
                        return;              
                }
            }
            if (condition != null) {
                addConditionToPlugin(condition);
                refreshDisplay();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to create condition. Check your inputs.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error creating condition: " + e.getMessage());
        }
    }
    private ConditionManager getConditionManger(){
        if (selectScheduledPlugin == null) {
            return null;
        }
        if (stopConditionPanel){
            return selectScheduledPlugin.getStopConditionManager();
        }else{
            return selectScheduledPlugin.getStartConditionManager();
        }
    }
    /**
     * Finds the logical condition that should be the target for adding a new condition
     * based on the current tree selection
     */
    private LogicalCondition findTargetLogicalForAddition() {
        if (stopConditionPanel){
            ConditionManager manager = getConditionManger();
        }
        if (selectScheduledPlugin == null) {
            return null;
        }
        
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) conditionTree.getLastSelectedPathComponent();
        if (selectedNode == null) {
            // No selection, use root logical
            return getConditionManger().getUserLogicalCondition();
        }
        
        Object userObject = selectedNode.getUserObject();
        
        // If selected node is a logical condition, use it directly
        if (userObject instanceof LogicalCondition) {
            // Check if this is a plugin-defined condition
            if (getConditionManger().isPluginDefinedCondition((LogicalCondition)userObject)) {               
                return getConditionManger().getUserLogicalCondition();
            }
            return (LogicalCondition) userObject;
        }
        
        // If selected node is a regular condition, find its parent logical
        if (userObject instanceof Condition && 
            selectedNode.getParent() != null && 
            selectedNode.getParent() != rootNode) {
            
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) selectedNode.getParent();
            if (parentNode.getUserObject() instanceof LogicalCondition) {
                // Check if this is a plugin-defined condition
                
                if (getConditionManger().isPluginDefinedCondition((LogicalCondition)parentNode.getUserObject())) {               
                    return getConditionManger().getUserLogicalCondition();
                }
                return (LogicalCondition) parentNode.getUserObject();
            }
        }
        
        // Default to user logical condition
        return getConditionManger().getUserLogicalCondition();
    }

    /**
     * Adds a condition to the appropriate logical structure based on selection
     */
    private void addConditionToPlugin(Condition condition) {
        if (selectScheduledPlugin == null || condition == null) {
            log.error("addConditionToPlugin: Cannot add condition - plugin or condition is null");
            return;
        }
        
        log.info("addConditionToPlugin: Adding condition: {}", condition.getDescription());
        
        ConditionManager manager = getConditionManger();
        if (manager == null) {
            log.error("addConditionToPlugin: Condition manager is null");
            return;
        }
        
        // Find target logical condition based on selection
        LogicalCondition targetLogical = findTargetLogicalForAddition();
        if (targetLogical == null) {
            log.error("addConditionToPlugin: Target logical is null");
            return;
        }
        
        log.info("addConditionToPlugin: Using target logical: {}", targetLogical.getDescription());
        
        // Add the condition
        manager.addConditionToLogical(condition, targetLogical);
        
        log.info("addConditionToPlugin: Condition added to manager");
        
        // Update UI
        updateTreeFromConditions();        
        
        // Notify listeners
        notifyConditionUpdate();
        
        // Select the newly added condition
        selectNodeForCondition(condition);
    }

    /**
     * Removes the selected condition from the logical structure
     */
    private void removeSelectedCondition() {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) conditionTree.getLastSelectedPathComponent();
        if (selectedNode == null || selectedNode == rootNode) {
            return;
        }
        
        Object userObject = selectedNode.getUserObject();
        if (!(userObject instanceof Condition)) {
            return;
        }
        
        Condition condition = (Condition) userObject;
        ConditionManager manager = getConditionManger();
        
        // Check if this is a plugin-defined condition
        if (manager.isPluginDefinedCondition(condition)) {
            JOptionPane.showMessageDialog(this,
                    "This condition is defined by the plugin and cannot be removed.",
                    "Plugin Condition",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Remove the condition from its logical structure
        boolean removed = manager.removeCondition(condition);
        
        if (!removed) {
            log.warn("Failed to remove condition: {}", condition.getDescription());
        }
        
        // Update UI
        updateTreeFromConditions();
        notifyConditionUpdate();
    }

   

    /**
     * Finds the common parent logical condition for a set of tree nodes
     */
    private LogicalCondition findCommonParent(DefaultMutableTreeNode[] nodes) {
        if (nodes.length == 0) {
            return null;
        }
        
        // Get the parent of the first node
        DefaultMutableTreeNode firstParent = (DefaultMutableTreeNode) nodes[0].getParent();
        if (firstParent == null || firstParent == rootNode) {
            return getConditionManger().getUserLogicalCondition();
        }
        
        if (!(firstParent.getUserObject() instanceof LogicalCondition)) {
            return null;
        }
        
        LogicalCondition parentLogical = (LogicalCondition) firstParent.getUserObject();
        
        // Check if all nodes have the same parent
        for (int i = 1; i < nodes.length; i++) {
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) nodes[i].getParent();
            if (parent != firstParent) {
                return null;
            }
        }
        
        return parentLogical;
    }

    /**
     * Negates the selected condition
     */
    private void negateSelectedCondition() {
    DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) conditionTree.getLastSelectedPathComponent();
    if (selectedNode == null || !(selectedNode.getUserObject() instanceof Condition)) {
        return;
    }
    
    Condition selectedCondition = (Condition) selectedNode.getUserObject();
    
    // Use the utility method for negation
    boolean success = ConditionConfigPanelUtil.negateCondition(
        selectedCondition, 
        getConditionManger(), 
        this
    );
    
    if (success) {
        // Update UI
        updateTreeFromConditions();
        notifyConditionUpdate();
    }
}

    /**
     * Initializes the logical operations toolbar
     */
    private void initializeLogicalOperationsToolbar(JPanel configPanel) {
     
        JPanel logicalOpPanel = new JPanel();
        logicalOpPanel.setLayout(new BoxLayout(logicalOpPanel, BoxLayout.X_AXIS));
        logicalOpPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        logicalOpPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        
        // Group operations section
        JButton createAndButton = ConditionConfigPanelUtil.createButton("Group as AND", ColorScheme.BRAND_ORANGE);
        createAndButton.setToolTipText("Group selected conditions with AND logic");
        createAndButton.addActionListener(e -> createLogicalGroup(true));
        
        JButton createOrButton = ConditionConfigPanelUtil.createButton("Group as OR", BRAND_BLUE);
        createOrButton.setToolTipText("Group selected conditions with OR logic");
        createOrButton.addActionListener(e -> createLogicalGroup(false));
        
        // Negation button
        JButton negateButton = ConditionConfigPanelUtil.createButton("Negate", new Color(220, 50, 50));
        negateButton.setToolTipText("Negate the selected condition (toggle NOT)");
        negateButton.addActionListener(e -> negateSelectedCondition());
        
        // Convert operation buttons
        JButton convertToAndButton = ConditionConfigPanelUtil.createButton("Convert to AND", ColorScheme.BRAND_ORANGE);
        convertToAndButton.setToolTipText("Convert selected logical group to AND type");
        convertToAndButton.addActionListener(e -> convertLogicalType(true));
        
        JButton convertToOrButton = ConditionConfigPanelUtil.createButton("Convert to OR", BRAND_BLUE);
        convertToOrButton.setToolTipText("Convert selected logical group to OR type");
        convertToOrButton.addActionListener(e -> convertLogicalType(false));
        
        // Ungroup button
        JButton ungroupButton = ConditionConfigPanelUtil.createButton("Ungroup", ColorScheme.LIGHT_GRAY_COLOR);
        ungroupButton.setToolTipText("Remove the logical group but keep its conditions");
        ungroupButton.addActionListener(e -> ungroupSelectedLogical());
        
        // Add buttons to panel with separators
        logicalOpPanel.add(createAndButton);
        logicalOpPanel.add(Box.createHorizontalStrut(5));
        logicalOpPanel.add(createOrButton);
        logicalOpPanel.add(Box.createHorizontalStrut(10));
        logicalOpPanel.add(new JSeparator(SwingConstants.VERTICAL));
        logicalOpPanel.add(Box.createHorizontalStrut(10));
        logicalOpPanel.add(negateButton);
        logicalOpPanel.add(Box.createHorizontalStrut(10));
        logicalOpPanel.add(new JSeparator(SwingConstants.VERTICAL));
        logicalOpPanel.add(Box.createHorizontalStrut(10));
        logicalOpPanel.add(convertToAndButton);
        logicalOpPanel.add(Box.createHorizontalStrut(5));
        logicalOpPanel.add(convertToOrButton);
        logicalOpPanel.add(Box.createHorizontalStrut(10));
        logicalOpPanel.add(new JSeparator(SwingConstants.VERTICAL));
        logicalOpPanel.add(Box.createHorizontalStrut(10));
        logicalOpPanel.add(ungroupButton);
        
        // Store references to buttons that need context-sensitive enabling
        this.negateButton = negateButton;
        this.convertToAndButton = convertToAndButton;
        this.convertToOrButton = convertToOrButton;
        this.ungroupButton = ungroupButton;
        
        // Add selection listener to enable/disable buttons based on context
        conditionTree.addTreeSelectionListener(e -> updateLogicalButtonStates());
        
        // Initial state
        updateLogicalButtonStates();
        if (configPanel == null) {                    
            add(logicalOpPanel, BorderLayout.NORTH);
        }else {
            configPanel.add(logicalOpPanel, BorderLayout.NORTH);
        }
        

    }

    /**
     * Updates button states based on current selection
     */
    private void updateLogicalButtonStates() {
        DefaultMutableTreeNode[] selectedNodes = getSelectedConditionNodes();
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) conditionTree.getLastSelectedPathComponent();
        
        // Disable all buttons by default
        negateButton.setEnabled(false);
        convertToAndButton.setEnabled(false);
        convertToOrButton.setEnabled(false);
        ungroupButton.setEnabled(false);
        
        // If nothing selected, we're done
        if (selectedNode == null) {
            return;
        }
        
        // Special handling for logical groups
        if (selectedNode.getUserObject() instanceof LogicalCondition) {
            LogicalCondition logical = (LogicalCondition) selectedNode.getUserObject();
            
            // Enable convert buttons based on current type
            boolean isAnd = logical instanceof AndCondition;
            convertToAndButton.setEnabled(!isAnd);
            convertToOrButton.setEnabled(isAnd);
            
            // Enable ungroup button if this isn't the root logical
            ungroupButton.setEnabled(selectedNode.getParent() != rootNode);
            
            return;
        }
        
        // Enable negate button for regular conditions (not logical groups)
        if (selectedNode.getUserObject() instanceof Condition && 
            !(selectedNode.getUserObject() instanceof LogicalCondition)) {
            
            // But not for plugin-defined conditions
            Condition condition = (Condition) selectedNode.getUserObject();
            boolean isPluginDefined = getConditionManger()
                                      .isPluginDefinedCondition(condition);
            
            negateButton.setEnabled(!isPluginDefined);
        }
    }
    

    /**
     * Creates a popup menu for the condition tree
     */
    private JPopupMenu createTreePopupMenu() {
        JPopupMenu menu = new JPopupMenu();
        
        // Negate option
        JMenuItem negateItem = new JMenuItem("Negate");
        negateItem.addActionListener(e -> negateSelectedCondition());
        
        // Group options
        JMenuItem groupAndItem = new JMenuItem("Group as AND");
        groupAndItem.addActionListener(e -> createLogicalGroup(true));
        
        JMenuItem groupOrItem = new JMenuItem("Group as OR");
        groupOrItem.addActionListener(e -> createLogicalGroup(false));
        
        // Convert options
        JMenuItem convertToAndItem = new JMenuItem("Convert to AND");
        convertToAndItem.addActionListener(e -> convertLogicalType(true));
        
        JMenuItem convertToOrItem = new JMenuItem("Convert to OR");
        convertToOrItem.addActionListener(e -> convertLogicalType(false));
        
        // Ungroup option
        JMenuItem ungroupItem = new JMenuItem("Ungroup");
        ungroupItem.addActionListener(e -> ungroupSelectedLogical());
        
        // Remove option
        JMenuItem removeItem = new JMenuItem("Remove");
        removeItem.addActionListener(e -> removeSelectedCondition());
        
        // Add all items
        menu.add(negateItem);
        menu.addSeparator();
        menu.add(groupAndItem);
        menu.add(groupOrItem);
        menu.addSeparator();
        menu.add(convertToAndItem);
        menu.add(convertToOrItem);
        menu.add(ungroupItem);
        menu.addSeparator();
        menu.add(removeItem);
        
        // Add popup listener to control enabled state of menu items
        menu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                DefaultMutableTreeNode[] selectedNodes = getSelectedConditionNodes();
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) conditionTree.getLastSelectedPathComponent();
                boolean isLogical = selectedNode != null && selectedNode.getUserObject() instanceof LogicalCondition;
                boolean isAnd = isLogical && selectedNode.getUserObject() instanceof AndCondition;
                boolean isPluginDefined = selectedNode != null && 
                                         selectedNode.getUserObject() instanceof Condition &&
                                         getConditionManger()
                                            .isPluginDefinedCondition((Condition)selectedNode.getUserObject());
                
                // Enable/disable items based on context
                negateItem.setEnabled(selectedNode != null && !isLogical && !isPluginDefined);
                groupAndItem.setEnabled(selectedNodes.length >= 2);
                groupOrItem.setEnabled(selectedNodes.length >= 2);
                convertToAndItem.setEnabled(isLogical && !isAnd);
                convertToOrItem.setEnabled(isLogical && isAnd);
                ungroupItem.setEnabled(isLogical && selectedNode.getParent() != rootNode);
                removeItem.setEnabled(selectedNode != null && !isPluginDefined);
            }
            
            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
            
            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {}
        });
        
        return menu;
    }

    /**
     * Gets an array of tree nodes representing the selected conditions
     */
    private DefaultMutableTreeNode[] getSelectedConditionNodes() {
        TreePath[] paths = conditionTree.getSelectionPaths();
        if (paths == null) {
            return new DefaultMutableTreeNode[0];
        }
        
        List<DefaultMutableTreeNode> nodes = new ArrayList<>();
        for (TreePath path : paths) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (node != rootNode && node.getUserObject() instanceof Condition) {
                nodes.add(node);
            }
        }
        
        return nodes.toArray(new DefaultMutableTreeNode[0]);
    }

    /**
     * Converts a logical group from one type to another (AND <-> OR)
     */
    private void convertLogicalType(boolean toAnd) {
    DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) conditionTree.getLastSelectedPathComponent();
    if (selectedNode == null || !(selectedNode.getUserObject() instanceof LogicalCondition)) {
        return;
    }
    
    LogicalCondition logicalCondition = (LogicalCondition) selectedNode.getUserObject();
    
    // Use the utility method for conversion
    boolean success = ConditionConfigPanelUtil.convertLogicalType(
        logicalCondition,
        toAnd,
        getConditionManger(),
        this
    );
    
    if (success) {
        // Update UI
        updateTreeFromConditions();
        selectNodeForCondition(toAnd ? 
            getConditionManger().getUserLogicalCondition() : 
            getConditionManger().getUserLogicalCondition());
        notifyConditionUpdate();
    }
}

    /**
     * Removes a logical group but keeps its conditions
     */
    private void ungroupSelectedLogical() {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) conditionTree.getLastSelectedPathComponent();
        if (selectedNode == null || !(selectedNode.getUserObject() instanceof LogicalCondition)) {
            return;
        }
        
        // Don't allow ungrouping the root logical
        if (selectedNode.getParent() == rootNode) {
            JOptionPane.showMessageDialog(this,
                    "Cannot ungroup the root logical condition.",
                    "Operation Not Allowed",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        LogicalCondition logicalToUngroup = (LogicalCondition) selectedNode.getUserObject();

        // Check if this is a plugin-defined logical group
        if (selectScheduledPlugin != null && 
            selectScheduledPlugin.getStopConditionManager().isPluginDefinedCondition(logicalToUngroup)) {
            
            JOptionPane.showMessageDialog(this,
                    "Cannot ungroup plugin-defined condition groups. These conditions are protected.",
                    "Protected Conditions",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) selectedNode.getParent();
        
        // Only proceed if parent is a logical condition
        if (!(parentNode.getUserObject() instanceof LogicalCondition)) {
            return;
        }
        
        LogicalCondition parentLogical = (LogicalCondition) parentNode.getUserObject();
        
        // Find position in parent
        int index = parentLogical.getConditions().indexOf(logicalToUngroup);
        if (index < 0) {
            return;
        }
        
        // Remove the logical from its parent
        parentLogical.getConditions().remove(index);
        
        // Add all of its conditions to the parent at the same position
        int currentIndex = index;
        for (Condition condition : new ArrayList<>(logicalToUngroup.getConditions())) {
            parentLogical.addConditionAt(currentIndex++, condition);
        }
        
        // Update UI
        updateTreeFromConditions();
        notifyConditionUpdate();
    }

    /**
     * Creates a new logical group from the selected conditions
     */
    private void createLogicalGroup(boolean isAnd) {
        DefaultMutableTreeNode[] selectedNodes = getSelectedConditionNodes();
        if (selectedNodes.length < 2) {
            JOptionPane.showMessageDialog(this,
                    "Please select at least two conditions to group",
                    "Selection Required",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Verify all nodes have the same parent
        DefaultMutableTreeNode firstParent = (DefaultMutableTreeNode) selectedNodes[0].getParent();
        for (int i = 1; i < selectedNodes.length; i++) {
            if (selectedNodes[i].getParent() != firstParent) {
                JOptionPane.showMessageDialog(this,
                        "All conditions must have the same parent to group them",
                        "Invalid Selection",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        // Check for plugin-defined conditions
        for (DefaultMutableTreeNode node : selectedNodes) {
            if (node.getUserObject() instanceof Condition) {
                Condition condition = (Condition) node.getUserObject();                
                // Don't allow modifying plugin-defined conditions
                if (selectScheduledPlugin != null && 
                    getConditionManger().isPluginDefinedCondition(condition)) {
                    
                    JOptionPane.showMessageDialog(this,
                            "Cannot group plugin-defined conditions. These conditions are protected.",
                            "Protected Conditions",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
        }
        // Create new logical condition
        LogicalCondition newLogical = isAnd ? new AndCondition() : new OrCondition();
        
        // Determine parent logical
        LogicalCondition parentLogical;
        if (firstParent == rootNode) {
            parentLogical = getConditionManger().getUserLogicalCondition();
        } else if (firstParent.getUserObject() instanceof LogicalCondition) {
            parentLogical = (LogicalCondition) firstParent.getUserObject();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Cannot determine parent logical group",
                    "Operation Failed",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Collect all selected conditions
        List<Condition> conditionsToGroup = new ArrayList<>();
        for (DefaultMutableTreeNode node : selectedNodes) {
            if (node.getUserObject() instanceof Condition) {
                conditionsToGroup.add((Condition) node.getUserObject());
            }
        }
        
        // Remove conditions from parent
        for (Condition condition : conditionsToGroup) {
            parentLogical.getConditions().remove(condition);
        }
        
        // Add conditions to new logical
        for (Condition condition : conditionsToGroup) {
            newLogical.addCondition(condition);
        }
        
        // Add new logical to parent
        parentLogical.addCondition(newLogical);
        
        // Update UI
        updateTreeFromConditions();
        selectNodeForCondition(newLogical);
        notifyConditionUpdate();
    }

    /**
     * Updates the condition editor panel when a condition is selected in the tree
     */
    private void updateConditionPanelForSelectedNode() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) conditionTree.getLastSelectedPathComponent();
        if (node == null || !(node.getUserObject() instanceof Condition)) {
            return;
        }
        
        Condition condition = (Condition) node.getUserObject();
        if (condition instanceof LogicalCondition || condition instanceof NotCondition) {
            return; // Skip logical conditions as they don't have direct UI editors
        }
        
        try {
            updatingSelectionFlag[0] = true;
            
            // Determine condition type and setup appropriate UI
            if (condition instanceof LocationCondition) {
                conditionCategoryComboBox.setSelectedItem("Location");
                updateConditionTypes("Location");
                
                if (condition instanceof PositionCondition) {
                    conditionTypeComboBox.setSelectedItem("Position");
                } else if (condition instanceof AreaCondition) {
                    conditionTypeComboBox.setSelectedItem("Area");
                } else if (condition instanceof RegionCondition) {
                    conditionTypeComboBox.setSelectedItem("Region");
                }
                
                updateConfigPanel();
                JPanel localConfigPanel = (JPanel) configPanel.getClientProperty("localConditionPanel");
                if (localConfigPanel != null) {
                    LocationConditionUtil.setupLocationCondition(localConfigPanel, condition);
                }
            } 
            else if (condition instanceof SkillLevelCondition || condition instanceof SkillXpCondition) {
                conditionCategoryComboBox.setSelectedItem("Skill");
                updateConditionTypes("Skill");
                
                if (condition instanceof SkillLevelCondition) {
                    if (stopConditionPanel) {
                        conditionTypeComboBox.setSelectedItem("Skill Level");
                    } else {
                        conditionTypeComboBox.setSelectedItem("Skill Level Required");
                    }
                } else if (condition instanceof SkillXpCondition) {
                    conditionTypeComboBox.setSelectedItem("Skill XP Goal");
                }
                
                updateConfigPanel();
                JPanel localConfigPanel = (JPanel) configPanel.getClientProperty("localConditionPanel");
                if (localConfigPanel != null) {
                    SkillConditionPanelUtil.setupSkillCondition(localConfigPanel, condition);
                }
            }
            else if (condition instanceof TimeCondition) {
                conditionCategoryComboBox.setSelectedItem("Time");
                updateConditionTypes("Time");
                
                if (condition instanceof IntervalCondition) {
                    conditionTypeComboBox.setSelectedItem("Time Duration");
                } else if (condition instanceof TimeWindowCondition) {
                    TimeWindowCondition windowCondition = (TimeWindowCondition) condition;                 
                    conditionTypeComboBox.setSelectedItem("Time Window");                 
                }else if( condition instanceof NotCondition && 
                        ((NotCondition) condition).getCondition() instanceof TimeWindowCondition) {
                    // This is a negated time window condition
                    conditionTypeComboBox.setSelectedItem(stopConditionPanel ? "Not In Time Window" : "Outside Time Window");
                } else if (condition instanceof SingleTriggerTimeCondition) {
                    conditionTypeComboBox.setSelectedItem("Single Trigger");
                } else if (condition instanceof IntervalCondition) {
                    conditionTypeComboBox.setSelectedItem("Interval");
                
                }else if (condition instanceof DayOfWeekCondition) {
                    conditionTypeComboBox.setSelectedItem("Day of Week");
                }
                
                updateConfigPanel();
                JPanel localConfigPanel = (JPanel) configPanel.getClientProperty("localConditionPanel");
                if (localConfigPanel != null) {
                    TimeConditionPanelUtil.setupTimeCondition(localConfigPanel, condition);
                }
            }
            else if (condition instanceof InventoryItemCountCondition || 
                     condition instanceof BankItemCountCondition ||
                     condition instanceof LootItemCondition ||
                     condition instanceof ProcessItemCondition ||
                     condition instanceof GatheredResourceCondition 
                     || condition instanceof AndCondition|| condition instanceof OrCondition) {
                Condition baseResourceCondition = condition;  
                if (condition instanceof AndCondition || condition instanceof OrCondition) {
                    //check if all conditions are resource conditions,  and from the same type 
                    baseResourceCondition = (Condition) ((LogicalCondition)condition).getConditions().get(0);
                    for (Condition c : ((LogicalCondition) condition).getConditions()) {
                        //check if c is of the same typ as first condition
                        if (!c.getClass().equals(baseResourceCondition.getClass())) {
                            //not all from the same type ? 
                            return;
                        }   
                    }
                }
                conditionCategoryComboBox.setSelectedItem("Resource");
                updateConditionTypes("Resource");
                
                if (baseResourceCondition instanceof InventoryItemCountCondition) {
                    conditionTypeComboBox.setSelectedItem(stopConditionPanel ? "Item Collection" : "Item Required");
                } else if (baseResourceCondition instanceof ProcessItemCondition) {
                    conditionTypeComboBox.setSelectedItem("Process Items");
                } else if (baseResourceCondition instanceof GatheredResourceCondition) {
                    conditionTypeComboBox.setSelectedItem("Gather Resources");
                } else if (baseResourceCondition instanceof LootItemCondition) {
                    conditionTypeComboBox.setSelectedItem("Item Collection");
                }
                
                updateConfigPanel();
                JPanel localConfigPanel = (JPanel) configPanel.getClientProperty("localConditionPanel");
                if (localConfigPanel != null) {
                    ResourceConditionPanelUtil.setupResourceCondition(localConfigPanel, condition);
                }
            }
            
            // Update edit button state
            editButton.setText("Apply Changes");
        } finally {
            updatingSelectionFlag[0] = false;
        }
    }
}