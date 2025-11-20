# 🚀 Sloth Quick Reference Card

## Quick Start (First Time Only)

1. **Sync Gradle**: File → Sync Project with Gradle Files
2. **Full Install**: Run TeamCode configuration (standard install)
3. **Setup Tasks**: [Follow detailed steps in SLOTH_SETUP.md](SLOTH_SETUP.md#-configure-android-studio-tasks-one-time)

## Daily Workflow

```
1. Make code changes
2. Run: deploySloth  (< 1 second!)
3. Stop your OpMode
4. Restart OpMode - see changes!
```

## Commands

| Task | Use Case | Speed |
|------|----------|-------|
| `deploySloth` | 99% of development | ⚡ < 1s |
| `TeamCode` (install) | Libraries, drivers, troubleshooting | 🐌 ~40s |
| `removeSlothRemote` | Clean cache, switch branches | ⚡ < 5s |

## When to Use Full Install

❌ After library updates (Pedro Pathing, Panels, etc.)  
❌ When adding hardware drivers  
❌ First install after git pull/clone  
❌ Before competitions (to ensure clean state)  
❌ When debugging weird hot reload issues  

## Troubleshooting

**Changes not showing?**
1. Did you stop the OpMode after deploying?
2. Run `removeSlothRemote` → Full Install

**Task not found?**
1. File → Sync Project with Gradle Files
2. File → Invalidate Caches → Restart

**Build errors?**
1. Ensure first install was full TeamCode install
2. Build → Clean Project

## Rules of Thumb

✅ **Use deploySloth** for all code changes in TeamCode  
✅ **Always stop OpMode** before expecting changes  
✅ **Full install weekly** to stay in sync  
✅ **Test with full install** before competitions  

## What Hot Reloads?

✅ All Java in `org.firstinspires.ftc.teamcode`  
✅ OpModes, subsystems, utilities  
❌ Libraries, SDK files, hardware configs  

---

📖 **Full Documentation**: [SLOTH_SETUP.md](SLOTH_SETUP.md)

