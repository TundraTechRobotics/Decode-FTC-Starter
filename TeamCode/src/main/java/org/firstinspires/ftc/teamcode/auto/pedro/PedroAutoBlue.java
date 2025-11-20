/* Copyright (c) 2025 FTC DECODE Starter Kit. All rights reserved.
 *
 * PedroAutoBlue - Advanced autonomous using Pedro Pathing (Blue Alliance)
 * 
 * This OpMode demonstrates Pedro Pathing for smooth, reliable autonomous movement.
 * It's more advanced than StarterAutoSkeleton but provides better path following.
 */

package org.firstinspires.ftc.teamcode.auto.pedro;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierCurve;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.core.StarterShooter;
import org.firstinspires.ftc.teamcode.dashboard.PanelsDashboardHelper;

/**
 * PedroAutoBlue - Blue Alliance autonomous with Pedro Pathing
 * 
 * Pedro Pathing provides:
 * - Smooth bezier curve paths (no jerky movements)
 * - Automatic path following (no manual PID tuning)
 * - Real-time pose tracking with odometry
 * - Field visualization on Panels dashboard
 * 
 * Strategy:
 * 1. Start at blue alliance LAUNCH LINE
 * 2. Follow smooth path away from line
 * 3. Turn to face GOAL
 * 4. Shoot preloaded artifacts
 * 5. Move toward BASE
 * 
 * Prerequisites:
 * - GoBilda Pinpoint odometry computer installed and configured
 * - Motor names match PedroConstants
 * - Panels dashboard running (optional but recommended)
 * 
 * How to Customize:
 * 1. Adjust starting pose (based on your field position)
 * 2. Modify path waypoints (see buildPaths() method)
 * 3. Change shooter preset (SHORT_RANGE vs LONG_RANGE)
 * 4. Add more paths for cycling or complex strategies
 */
@Autonomous(name="Pedro Auto Blue (Advanced)", group="Pedro Autonomous")
@Configurable  // Allows configuration via Panels dashboard
public class PedroAutoBlue extends OpMode {
    
    // ═══════════════════════════════════════════════════════════════
    // SUBSYSTEMS
    // ═══════════════════════════════════════════════════════════════
    
    private Follower follower;                      // Pedro path follower
    private StarterShooter shooter;                 // Shooter subsystem
    private PanelsDashboardHelper dashboard;        // Dashboard for telemetry and field view
    private PedroAutonomousBuilder autoBuilder;     // Autonomous state machine builder

    // ═══════════════════════════════════════════════════════════════
    // PATHS
    // ═══════════════════════════════════════════════════════════════
    
    private PathChain leaveLaunchLine;  // Path 1: Leave starting line
    private PathChain moveToBase;       // Path 2: Move toward base
    
    // ═══════════════════════════════════════════════════════════════
    // STARTING POSITION (Blue Alliance)
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Starting pose for blue alliance
     * 
     * Coordinate system:
     * - Origin (0, 0) at field center
     * - +X toward red wall, +Y toward blue audience
     * 
     * This pose assumes:
     * - Robot starts on blue LAUNCH LINE
     * - Facing toward field center (90° = toward audience side)
     * 
     * Adjust based on your actual starting position!
     */
    private static final Pose START_POSE = new Pose(56.5, 8.5, Math.toRadians(90));
    
    // ═══════════════════════════════════════════════════════════════
    // INITIALIZATION
    // ═══════════════════════════════════════════════════════════════
    
    @Override
    public void init() {
        // Initialize dashboard
        dashboard = new PanelsDashboardHelper(telemetry);
        dashboard.logLine("╔════════════════════════════════════╗");
        dashboard.logLine("║  PEDRO AUTO BLUE (ADVANCED)        ║");
        dashboard.logLine("╚════════════════════════════════════╝");
        dashboard.logLine("");
        
        // Initialize Pedro Pathing
        try {
            follower = Constants.createFollower(hardwareMap);
            follower.setStartingPose(START_POSE);
            dashboard.logLine("✅ Pedro Pathing initialized");
        } catch (Exception e) {
            follower = null;
            dashboard.logLine("❌ Pedro Pathing FAILED: " + e.getMessage());
            dashboard.logLine("   Check: odometry hardware 'odo'");
        }
        
        // Initialize shooter
        try {
            shooter = new StarterShooter(hardwareMap);
            dashboard.logLine("✅ Shooter initialized");
        } catch (Exception e) {
            shooter = null;
            dashboard.logLine("❌ Shooter FAILED: " + e.getMessage());
        }
        
        // Build paths
        if (follower != null) {
            buildPaths();
            dashboard.logLine("✅ Paths built");

            // Build autonomous sequence using builder pattern
            buildAutonomousSequence();
            dashboard.logLine("✅ Autonomous sequence configured");
        }
        
        dashboard.logLine("");
        dashboard.logLine("Strategy: Leave line → Turn → Shoot → Base");
        dashboard.logLine("");
        dashboard.logLine("✅ Ready! Press START");
        
        dashboard.update();
    }
    
    /**
     * Build the autonomous sequence using the builder pattern
     *
     * This creates a clean, readable sequence of actions:
     * 1. Leave LAUNCH LINE (path)
     * 2. Turn to face GOAL
     * 3. Shoot preloaded artifacts (2 shots)
     * 4. Move toward BASE (path)
     */
    private void buildAutonomousSequence() {
        autoBuilder = new PedroAutonomousBuilder(follower)
            .withShooter(shooter)
            // Step 1: Leave LAUNCH LINE
            .addPath(leaveLaunchLine, "Leave Launch Line")
            // Step 2: Turn to face GOAL
            .addTurnToHeading(Math.toRadians(114))  // Adjust angle for your field position
            // Step 3: Shoot preloaded artifacts
            .addShootAction(2, StarterShooter.ShootingPreset.SHORT_RANGE)
            // Step 4: Move toward BASE
            .addPath(moveToBase, "Move to Base");
    }

    /**
     * Build all paths for autonomous
     * 
     * Path 1: Leave LAUNCH LINE
     * - Smooth forward movement
     * - Clears the starting line for bonus points
     * 
     * Path 2: Move to BASE
     * - Positions robot near BASE for endgame
     * - Optional: Can add more complex paths here
     */
    private void buildPaths() {
        // Path 1: Leave LAUNCH LINE (straight forward 30 inches)
        leaveLaunchLine = follower.pathBuilder()
                .addPath(new BezierLine(
                    new Pose(START_POSE.getX(), START_POSE.getY(), START_POSE.getHeading()),
                    new Pose(START_POSE.getX(), START_POSE.getY() + 30, START_POSE.getHeading())
                ))
                .setLinearHeadingInterpolation(START_POSE.getHeading(), START_POSE.getHeading())
                .build();
        
        // Path 2: Move toward BASE (example - adjust for your field position)
        Pose afterLeaving = new Pose(START_POSE.getX(), START_POSE.getY() + 30, START_POSE.getHeading());
        moveToBase = follower.pathBuilder()
                .addPath(new BezierLine(
                    new Pose(afterLeaving.getX(), afterLeaving.getY(), afterLeaving.getHeading()),
                    new Pose(afterLeaving.getX() - 20, afterLeaving.getY() + 20, Math.toRadians(45))
                ))
                .setLinearHeadingInterpolation(afterLeaving.getHeading(), Math.toRadians(45))
                .build();
    }
    
    // ═══════════════════════════════════════════════════════════════
    // START
    // ═══════════════════════════════════════════════════════════════
    
    @Override
    public void start() {
        if (autoBuilder != null) {
            autoBuilder.start();
            dashboard.logLine("▶️  Autonomous started!");
            dashboard.update();
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // MAIN LOOP
    // ═══════════════════════════════════════════════════════════════
    
    @Override
    public void loop() {
        if (follower == null || autoBuilder == null) {
            dashboard.logLine("❌ Cannot run - initialization failed");
            dashboard.update();
            return;
        }
        
        // Update follower (REQUIRED - must be called before builder update!)
        follower.update();
        
        // Update shooter if available
        if (shooter != null) {
            shooter.update(gamepad1);  // gamepad not used in auto, but needed for RPM calc
        }
        
        // Update autonomous builder - handles all state transitions automatically
        String currentStep = autoBuilder.update();

        // Update telemetry and field view
        updateTelemetry(currentStep);
    }
    
    /**
     * Update telemetry and field visualization
     */
    private void updateTelemetry(String currentStep) {
        // Get current pose
        Pose currentPose = follower.getPose();
        
        // Clear previous drawings
        dashboard.clearFieldOverlays();
        
        // Draw robot on field
        dashboard.setRobotPose(currentPose);
        
        // Telemetry
        dashboard.log("Current Step", currentStep);
        dashboard.log("Progress", "%d/%d", autoBuilder.getCurrentStepIndex() + 1, autoBuilder.getTotalSteps());
        dashboard.log("X Position", "%.1f in", currentPose.getX());
        dashboard.log("Y Position", "%.1f in", currentPose.getY());
        dashboard.log("Heading", "%.1f°", Math.toDegrees(currentPose.getHeading()));
        dashboard.logLine("");
        
        if (shooter != null) {
            dashboard.log("Shooter", shooter.getStateString());
            dashboard.log("RPM", "%.0f / %.0f", shooter.getCurrentRPM(), shooter.getTargetRPM());
        }
        
        dashboard.logLine("");
        dashboard.log("Runtime", "%.1f sec", getRuntime());
        
        dashboard.update();
    }
}
