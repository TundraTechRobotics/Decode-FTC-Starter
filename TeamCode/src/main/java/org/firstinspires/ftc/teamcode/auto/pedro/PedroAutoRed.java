/* Copyright (c) 2025 FTC DECODE Starter Kit. All rights reserved.
 *
 * PedroAutoRed - Advanced autonomous using Pedro Pathing (Red Alliance)
 * 
 * This OpMode is the red alliance version of PedroAutoBlue.
 * See PedroAutoBlue.java for detailed comments and explanation.
 */

package org.firstinspires.ftc.teamcode.auto.pedro;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.core.StarterShooter;
import org.firstinspires.ftc.teamcode.dashboard.PanelsDashboardHelper;

@Autonomous(name="Pedro Auto Red (Advanced)", group="Pedro Autonomous")
@Configurable
public class PedroAutoRed extends OpMode {
    
    private Follower follower;
    private StarterShooter shooter;
    private PanelsDashboardHelper dashboard;
    private PedroAutonomousBuilder autoBuilder;

    private PathChain leaveLaunchLine;
    private PathChain moveToBase;
    
    /**
     * Starting pose for red alliance
     * Mirror of blue alliance pose across X-axis
     */
    private static final Pose START_POSE = new Pose(-56.5, 8.5, Math.toRadians(90));
    
    @Override
    public void init() {
        dashboard = new PanelsDashboardHelper(telemetry);
        dashboard.logLine("╔════════════════════════════════════╗");
        dashboard.logLine("║  PEDRO AUTO RED (ADVANCED)         ║");
        dashboard.logLine("╚════════════════════════════════════╝");
        dashboard.logLine("");
        
        try {
            follower = Constants.createFollower(hardwareMap);
            follower.setStartingPose(START_POSE);
            dashboard.logLine("✅ Pedro Pathing initialized");
        } catch (Exception e) {
            follower = null;
            dashboard.logLine("❌ Pedro Pathing FAILED: " + e.getMessage());
        }
        
        try {
            shooter = new StarterShooter(hardwareMap);
            dashboard.logLine("✅ Shooter initialized");
        } catch (Exception e) {
            shooter = null;
            dashboard.logLine("❌ Shooter FAILED: " + e.getMessage());
        }
        
        if (follower != null) {
            buildPaths();
            dashboard.logLine("✅ Paths built");

            buildAutonomousSequence();
            dashboard.logLine("✅ Autonomous sequence configured");
        }
        
        dashboard.logLine("");
        dashboard.logLine("Strategy: Leave line → Turn → Shoot → Base");
        dashboard.logLine("");
        dashboard.logLine("✅ Ready! Press START");
        dashboard.update();
    }
    
    private void buildAutonomousSequence() {
        autoBuilder = new PedroAutonomousBuilder(follower)
            .withShooter(shooter)
            .addPath(leaveLaunchLine, "Leave Launch Line")
            .addTurnToHeading(Math.toRadians(66))  // Adjust angle for red alliance
            .addShootAction(2, StarterShooter.ShootingPreset.SHORT_RANGE)
            .addPath(moveToBase, "Move to Base");
    }

    private void buildPaths() {
        // Path 1: Leave LAUNCH LINE
        leaveLaunchLine = follower.pathBuilder()
                .addPath(new BezierLine(
                    new Pose(START_POSE.getX(), START_POSE.getY(), START_POSE.getHeading()),
                    new Pose(START_POSE.getX(), START_POSE.getY() + 30, START_POSE.getHeading())
                ))
                .setLinearHeadingInterpolation(START_POSE.getHeading(), START_POSE.getHeading())
                .build();
        
        // Path 2: Move toward BASE
        Pose afterLeaving = new Pose(START_POSE.getX(), START_POSE.getY() + 30, START_POSE.getHeading());
        moveToBase = follower.pathBuilder()
                .addPath(new BezierLine(
                    new Pose(afterLeaving.getX(), afterLeaving.getY(), afterLeaving.getHeading()),
                    new Pose(afterLeaving.getX() + 20, afterLeaving.getY() + 20, Math.toRadians(135))
                ))
                .setLinearHeadingInterpolation(afterLeaving.getHeading(), Math.toRadians(135))
                .build();
    }
    
    @Override
    public void start() {
        if (autoBuilder != null) {
            autoBuilder.start();
            dashboard.logLine("▶️  Autonomous started!");
            dashboard.update();
        }
    }
    
    @Override
    public void loop() {
        if (follower == null || autoBuilder == null) {
            dashboard.logLine("❌ Cannot run - initialization failed");
            dashboard.update();
            return;
        }
        
        follower.update();
        
        if (shooter != null) {
            shooter.update(gamepad1);
        }
        
        String currentStep = autoBuilder.update();
        updateTelemetry(currentStep);
    }
    
    private void updateTelemetry(String currentStep) {
        Pose currentPose = follower.getPose();
        dashboard.clearFieldOverlays();
        dashboard.setRobotPose(currentPose);
        
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
