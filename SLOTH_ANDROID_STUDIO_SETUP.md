# 🎯 Sloth Android Studio Configuration Guide

This guide walks you through setting up Sloth tasks in Android Studio with visual descriptions.

## Overview

You need to create 2 new Gradle tasks and modify 1 existing task:

1. **deploySloth** - Your main fast deployment task (use this 99% of the time)
2. **removeSlothRemote** - Cleans Sloth cache (for troubleshooting)
3. **TeamCode** - Modify to auto-clean cache on full installs

---

## Task 1: Create deploySloth

This is your **primary development task** - use it for all code changes!

### Steps:

1. **Open Configuration Menu**
   - Click dropdown next to Run button (top toolbar)
   - Select "Edit Configurations..."

2. **Add New Gradle Configuration**
   - Click the **+** button (top-left)
   - Select **Gradle** from the dropdown

3. **Configure deploySloth**
   - **Name**: `deploySloth`
   - **Gradle project**: Click folder icon → Select **:TeamCode**
   - **Tasks**: `deploySloth`
   - **Arguments**: (leave blank)
   - Click **Apply**

### What This Looks Like:

```
┌─────────────────────────────────────────┐
│ Name: deploySloth                       │
│                                         │
│ Gradle project: :TeamCode               │
│                                         │
│ Tasks: deploySloth                      │
│                                         │
│ Arguments: (empty)                      │
└─────────────────────────────────────────┘
```

---

## Task 2: Create removeSlothRemote

This task **cleans Sloth's cache** - use when switching branches or troubleshooting.

### Steps:

1. **Add New Gradle Configuration** (same as above)
   - Click **+** button
   - Select **Gradle**

2. **Configure removeSlothRemote**
   - **Name**: `removeSlothRemote`
   - **Gradle project**: Click folder icon → Select **:TeamCode**
   - **Tasks**: `removeSlothRemote`
   - **Arguments**: (leave blank)
   - Click **Apply**

### What This Looks Like:

```
┌─────────────────────────────────────────┐
│ Name: removeSlothRemote                 │
│                                         │
│ Gradle project: :TeamCode               │
│                                         │
│ Tasks: removeSlothRemote                │
│                                         │
│ Arguments: (empty)                      │
└─────────────────────────────────────────┘
```

---

## Task 3: Modify TeamCode Configuration

This ensures **Sloth cache is cleared** during full installs.

### Steps:

1. **Select TeamCode Configuration**
   - In "Edit Configurations" menu
   - Find and click **TeamCode** in the left sidebar

2. **Add Before Launch Task**
   - Scroll to **"Before launch"** section (bottom)
   - Click **+** button
   - Select **"Run Gradle task"**

3. **Configure Gradle Task**
   - In popup dialog:
     - **Gradle project**: Type `:TeamCode` (manually type this)
     - **Tasks**: `removeSlothRemote`
   - Click **OK**

4. **Reorder Tasks**
   - In "Before launch" list, you should see:
     - Gradle-aware Make
     - removeSlothRemote (newly added)
   - Use ⬆️ **up arrow** to move `removeSlothRemote` **above** "Gradle-aware Make"

5. **Final Order Should Be:**
   ```
   Before launch:
   1. removeSlothRemote
   2. Gradle-aware Make
   ```

6. Click **Apply** and **OK**

### What This Achieves:

When you run a full TeamCode install, Sloth's cache is automatically cleared first, ensuring a clean deployment.

---

## Verification

After setup, your Run configuration dropdown should show:

```
┌─────────────────────────┐
│ ▶ TeamCode              │  ← Full install (use weekly)
│ ▶ deploySloth           │  ← Fast deploy (use daily!)
│ ▶ removeSlothRemote     │  ← Cache clear (troubleshooting)
│ Edit Configurations...  │
└─────────────────────────┘
```

---

## Testing Your Setup

1. **Make a small code change**
   - Example: Add a comment to StarterRobotManager.java

2. **Run deploySloth**
   - Select `deploySloth` from dropdown
   - Click green play button
   - Should complete in < 1 second!

3. **Verify on Robot**
   - Stop your OpMode (if running)
   - Restart OpMode
   - Your change should be visible!

---

## Common Issues

### "Task 'deploySloth' not found"

**Solution:**
1. File → Sync Project with Gradle Files
2. Wait for sync to complete
3. Try again

### "Gradle project not found"

**Solution:**
- When typing `:TeamCode`, type it **exactly** as shown
- Don't try to browse for it - manually type `:TeamCode`

### "Changes not showing after deploySloth"

**Solution:**
- Remember: You must **stop the OpMode** before changes take effect
- Sloth processes changes when OpMode ends

---

## Quick Tips

💡 **Keyboard Shortcut**: After setup, use `Shift+F10` to run the currently selected configuration

💡 **Fast Switching**: Click the dropdown next to Run button to quickly switch between tasks

💡 **Deploy Often**: With < 1s deploy times, deploy after every small change!

💡 **Weekly Full Install**: Run full TeamCode install once per week to stay in sync

---

## Next Steps

✅ Configuration complete!  
🚀 Start using `deploySloth` for lightning-fast development  
📖 See [SLOTH_SETUP.md](SLOTH_SETUP.md) for detailed usage guide  
📋 Bookmark [SLOTH_QUICK_REFERENCE.md](SLOTH_QUICK_REFERENCE.md) for quick tips  

---

**Happy coding with Sloth! ⚡**

