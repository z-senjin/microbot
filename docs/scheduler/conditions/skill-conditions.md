# Skill Conditions

Skill conditions in the Plugin Scheduler system allow plugins to be controlled based on the player's skill levels and experience points.

## Overview

Skill conditions monitor the player's progress in various skills, allowing plugins to respond to skill-related milestones and achievements. These conditions can be used to automate skill training, set goals, and manage plugin schedules based on skill progress.

## Available Skill Conditions

### SkillLevelCondition

The `SkillLevelCondition` monitors the player's actual or effective level in a specific skill.

**Usage:**
```java
// Satisfied when player has at least level 70 Mining
SkillLevelCondition condition = new SkillLevelCondition(
    Skill.MINING,       // The skill to monitor
    70,                 // Target level
      // Comparison operator
);
```

**Key features:**
- Monitors any skill in the game
- Can track actual or effective/boosted level
- Supports various comparison types (equals, greater than, less than, etc.)
- Updates dynamically as skill levels change
- Provides progress tracking toward target levels

### SkillXpCondition

The `SkillXpCondition` monitors the player's experience points in a specific skill.

**Usage:**
```java
// Satisfied when player has at least 1,000,000 XP in Woodcutting
SkillXpCondition condition = new SkillXpCondition(
    Skill.WOODCUTTING,  // The skill to monitor
    1_000_000,          // Target XP
      // Comparison operator
);
```

**Key features:**
- Monitors precise XP values rather than levels
- Useful for tracking progress between levels
- Can be used to set specific XP goals
- Provides accurate progress percentage toward XP targets

## Common Features of Skill Conditions

All skill conditions implement the `SkillCondition` interface, which extends the base `Condition` interface and provides additional functionality:

- `getProgressPercentage()`: Returns the progress toward the target level or XP as a percentage
- `reset()`: Resets any cached skill data
- `getSkill()`: Returns the skill being monitored
- `getTargetValue()`: Returns the target level or XP value

## Using Skill Conditions as Start Conditions

When used as start conditions, skill conditions can trigger plugins based on skill achievements:

```java
PluginScheduleEntry entry = new PluginScheduleEntry("MyPlugin", true);

// Start the plugin when the player reaches level 70 in Mining
entry.addStartCondition(new SkillLevelCondition(
    Skill.MINING,
    70,
    
));
```

## Using Skill Conditions as Stop Conditions

Skill conditions can be used as stop conditions to end a plugin's execution when a skill goal is reached:

```java
// Stop when the player reaches level 80 in Mining
entry.addStopCondition(new SkillLevelCondition(
    Skill.MINING,
    80,
    
));

// OR stop when the player gains 100,000 XP in Mining
entry.addStopCondition(new SkillXpCondition(
    Skill.MINING,
    100_000,
    ComparisonType.RELATIVE_CHANGE_GREATER_THAN_OR_EQUAL
));
```

## Tracking Relative Changes

`SkillXpCondition` supports tracking relative changes in XP, which is useful for setting goals based on XP gained rather than absolute values:

```java
// Satisfied when the player gains 50,000 XP in any skill from when the condition was created
SkillXpCondition condition = new SkillXpCondition(
    Skill.OVERALL,
    50_000,
    ComparisonType.RELATIVE_CHANGE_GREATER_THAN_OR_EQUAL
);
```

## Combining with Logical Conditions

Skill conditions can be combined with logical conditions to create complex skill-based rules:

```java
// Create a logical AND condition
AndCondition skillGoals = new AndCondition();

// Require level 70 in Mining
skillGoals.addCondition(new SkillLevelCondition(
    Skill.MINING,
    70,
    
));

// AND level 70 in Smithing
skillGoals.addCondition(new SkillLevelCondition(
    Skill.SMITHING,
    70,
    
));

// Add these combined requirements as a start condition
entry.addStartCondition(skillGoals);
```

## Multi-Skill Training Scenarios

For multi-skill training scenarios, skill conditions can be configured to monitor several skills:

```java
// Create a logical OR condition for alternative training paths
OrCondition trainingGoals = new OrCondition();

// Path 1: Mining to level 80
trainingGoals.addCondition(new SkillLevelCondition(
    Skill.MINING,
    80,
    
));

// Path 2: Fishing to level 80
trainingGoals.addCondition(new SkillLevelCondition(
    Skill.FISHING,
    80,
    
));

// Path 3: Woodcutting to level 80
trainingGoals.addCondition(new SkillLevelCondition(
    Skill.WOODCUTTING,
    80,
    
));

// Add these alternative goals as a stop condition
entry.addStopCondition(trainingGoals);
```

## Event Integration

Skill conditions integrate with the RuneLite event system to track changes in real-time:

- `StatChanged`: Updates skill levels and XP values when they change
- `GameTick`: Periodically validates condition state