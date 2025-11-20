# 🚀 Sloth Hot Reload Integration Guide

## What is Sloth?

**Sloth is the fastest hot code reload library for FTC**, enabling lightning-fast development cycles with code deployment in **under 1 second** (compared to 40+ seconds for traditional builds).

### Key Benefits

✅ **Ultra-Fast Deployment**: Get your code on the robot in less than 1 second  
✅ **Persistent Changes**: Code changes survive robot restarts and power cycles  
✅ **Safe Hot Reload**: Only processes changes when your OpMode ends  
✅ **Smart Runtime**: Automatically sets up Dairy and updates SDK components  
✅ **Library Compatible**: Works with Pedro Pathing, and optionally with FTC Dashboard via Slothboard  

### Improvements Over fastload

- **5-7x faster**: Sloth achieves < 1s upload vs fastload's ~7s
- **Persistent changes** across robot restarts
- **Safe deployment** while other code is running
- **More capable runtime** with Dairy support and classpath scanning
- **Better library support** for hot reloading

---

## ⚙️ Installation Status

### ✅ Already Integrated

This project has **Sloth pre-configured** for you! The following setup is already complete:

1. ✅ Sloth library added to TeamCode (`build.gradle`)
2. ✅ Load plugin configured
3. ✅ Pedro Pathing configured to exclude Dashboard (for optional Slothboard compatibility)
4. ✅ Proper package structure (`org.firstinspires.ftc.teamcode`)

### 🎯 What You Need to Do

You just need to:
1. **Sync Gradle** (if you haven't already)
2. **Deploy Sloth** to your robot (one-time setup)
3. **Configure Android Studio tasks** (one-time setup)

---

## 📥 Initial Setup (One-Time)

### Step 1: Sync Gradle

1. Open this project in Android Studio
2. Click **File → Sync Project with Gradle Files**
3. Wait for sync to complete (may take a few minutes)

### Step 2: Deploy Sloth to Robot

**Important**: The first deployment MUST be a full install to get Sloth onto the robot.

1. Connect to your robot via WiFi
2. Build the project normally
3. Deploy to robot using standard installation:
   - Click **Run** button (green play icon), OR
   - Use Gradle task: `TeamCode → install`

This initial installation sets up Sloth on your robot. After this, you'll use the much faster `deploySloth` task.

---

## 🎮 Configure Android Studio Tasks (One-Time)

### Create deploySloth Task (Fast Deployment)

This is your main development task - use it every time you want to test code changes!

1. Click **Run → Edit Configurations**
2. Click **+ (Add New Configuration)**
3. Select **Gradle**
4. Configure:
   - **Name**: `deploySloth`
   - **Gradle Project**: Select `:TeamCode`
   - **Tasks**: `deploySloth`
   - **Run**: (leave default)
5. Click **Apply** and **OK**

### Create removeSlothRemote Task (Clean Up)

This removes Sloth's cached code - useful when switching branches or troubleshooting.

1. Click **Run → Edit Configurations**
2. Click **+ (Add New Configuration)**
3. Select **Gradle**
4. Configure:
   - **Name**: `removeSlothRemote`
   - **Gradle Project**: Select `:TeamCode`
   - **Tasks**: `removeSlothRemote`
   - **Run**: (leave default)
5. Click **Apply** and **OK**

### Update TeamCode Configuration (for Clean Deployment)

This ensures Sloth's cache is cleared during full reinstalls.

1. In **Run → Edit Configurations**, select **TeamCode**
2. Under **Before Launch** section, click **+**
3. Select **Run Gradle task**
4. Configure:
   - **Gradle Project**: Type `:TeamCode`
   - **Tasks**: `removeSlothRemote`
5. Move `removeSlothRemote` to the **top** of the Before Launch list
6. Click **Apply** and **OK**

---

## 🚀 Daily Usage

### Fast Development Cycle (< 1 second!)

Once set up, your workflow is:

1. **Make code changes** in TeamCode
2. **Run `deploySloth` configuration** (dropdown next to play button → deploySloth)
3. **Stop your OpMode** if it's running (Sloth processes changes when OpMode ends)
4. **Restart your OpMode** - changes are now live!

### When to Use Full Install

Use a **full install** (standard TeamCode configuration) when:

- 🔧 **Installing/updating libraries** (Pedro Pathing, Panels, etc.)
- 📁 **Changing files outside TeamCode package**
- 🏷️ **Modifying `@Pinned` annotations** on classes
- 🐛 **Troubleshooting weird behavior** after hot reloads
- 🔌 **First installation** after cloning/pulling project

After a full install, run `deploySloth` to continue fast development.

---

## ⚠️ Important Limitations & Best Practices

### What Sloth Can Hot Reload

✅ **All Java classes in `org.firstinspires.ftc.teamcode`** (and subpackages)  
✅ **OpModes** (TeleOp and Autonomous)  
✅ **Subsystems** (like StarterDrive, StarterShooter)  
✅ **Utilities and helper classes**  

### What Requires Full Install

❌ **Library updates** (Pedro Pathing, Dairy, etc.)  
❌ **SDK files outside TeamCode**  
❌ **Gradle configuration changes**  
❌ **Hardware drivers** added to TeamCode  
❌ **Changes to `@Pinned` classes**  

### Best Practices

1. **Always stop your OpMode** before expecting hot reload changes
2. **Use `deploySloth` for 99% of development** - it's instant!
3. **Full install weekly** to ensure everything stays in sync
4. **Run `removeSlothRemote` when switching git branches**
5. **Test with full install** before competitions

---

## 🔧 Troubleshooting

### "Code changes aren't showing up"

1. Did you **stop the OpMode** after deploying? (Sloth only updates when OpMode ends)
2. Try running **`removeSlothRemote`**, then do a **full install**
3. Check that your files are in `org.firstinspires.ftc.teamcode` package

### "deploySloth task not found"

1. Make sure you synced Gradle after adding Sloth
2. Check that `apply plugin: 'dev.frozenmilk.sinister.sloth.load'` is in `TeamCode/build.gradle`
3. Try **File → Invalidate Caches → Invalidate and Restart**

### "Build failed after adding Sloth"

1. Ensure you did initial **full install** before using `deploySloth`
2. Check that all dependencies are properly synced
3. Try cleaning: **Build → Clean Project**, then rebuild

### "Robot crashes after hot reload"

Some changes can't be hot reloaded safely. Do a **full install** when you:
- Change library versions
- Modify hardware configuration classes
- Add/remove `@Pinned` annotations

---

## 🎨 Optional: FTC Dashboard Integration (Slothboard)

If you want to use **FTC Dashboard** with Sloth's hot reload support, follow these steps:

### Add Slothboard Dependency

In `build.dependencies.gradle`, add:

```groovy
dependencies {
    // ... existing dependencies ...
    
    // Slothboard - FTC Dashboard with hot reload support
    implementation "com.acmerobotics.slothboard:dashboard:0.2.4+0.4.17"
}
```

### Configure Exclusions

Road Runner and other libraries that use Dashboard need exclusions added. In `build.dependencies.gradle`:

```groovy
// Example: If using Road Runner
implementation("com.acmerobotics.roadrunner:ftc:0.1.21") {
    exclude group: "com.acmerobotics.dashboard"
}
implementation("com.acmerobotics.roadrunner:actions:1.0.1") {
    exclude group: "com.acmerobotics.dashboard"
}
```

**Note**: Pedro Pathing is already configured with Dashboard exclusions in this project!

---

## 📚 Additional Resources

- **Sloth Documentation**: [Dairy Foundation Docs](https://docs.dairy.foundation/sinister/sloth)
- **Dairy Core**: [GitHub Repository](https://github.com/Dairy-Foundation/Core)
- **Load Plugin**: [Maven Repository](https://repo.dairy.foundation/releases)

---

## 🤝 Support & Contributing

### Get Help

- **FTC Discord**: Ask in programming channels
- **GitHub Issues**: Report bugs or request features for Sloth
- **Team Resources**: Check with your programming mentor

### Project-Specific Notes

This is the **DECODE FTC Starter Kit** with integrated Sloth support. The starter includes:

- ✅ **StarterRobotManager**: Central robot coordination hub
- ✅ **StarterDrive**: Mecanum/Tank drive system
- ✅ **StarterShooter**: Shooter subsystem with feeding mechanism
- ✅ **Pedro Pathing**: Pre-configured for autonomous
- ✅ **Sloth**: Ultra-fast hot reload

All of these work seamlessly with Sloth's hot reload!

---

## 📋 Quick Reference

| Task | Command | When to Use |
|------|---------|-------------|
| **Fast Deploy** | `deploySloth` | Every code change (99% of time) |
| **Full Install** | `TeamCode` (install) | Libraries, drivers, troubleshooting |
| **Clean Sloth Cache** | `removeSlothRemote` | Branch switches, weird behavior |

### Typical Development Session

```bash
# Morning: Full install to start fresh
Run TeamCode configuration (full install)

# Make code changes...
# Fast deploy - repeat as needed!
Run deploySloth configuration (< 1 second!)

# Stop OpMode, restart, test changes

# More changes...
Run deploySloth configuration again

# End of day: Optional full install for clean state
```

---

**Happy coding with lightning-fast deployment! ⚡**

*Generated for FTC DECODE Starter Kit - Now with Sloth integration*

