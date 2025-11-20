/* Copyright (c) 2025 FTC DECODE Starter Kit. All rights reserved.
 *
 * PedroAutonomousBuilder - Simplified state machine builder for Pedro Pathing autonomous
 */

package org.firstinspires.ftc.teamcode.auto.pedro;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierCurve;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;
import org.firstinspires.ftc.teamcode.core.StarterShooter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

/**
 * PedroAutonomousBuilder - Simplified state machine builder for Pedro Pathing autonomous
 *
 * Features:
 * - Easy path sequencing with automatic state management
 * - Action insertion between paths (shooting, mechanisms, etc.)
 * - Continuous position tracking during actions
 * - Automatic shooter integration
 * - Heading alignment actions
 * - Pause/resume functionality
 *
 * Example Usage:
 * <pre>
 * builder.addPath(paths.leaveLaunchLine)
 *        .addShootAction(2, StarterShooter.ShootingPreset.SHORT_RANGE)
 *        .addPath(paths.moveToBase)
 *        .addTurnToHeading(Math.toRadians(90))
 *        .addCustomAction("Deploy Mechanism", (follower, time) -> {
 *            // Custom mechanism code here
 *            return time > 1.0; // Complete after 1 second
 *        });
 * </pre>
 */
public class PedroAutonomousBuilder {

    // State machine components
    private final List<AutonomousStep> steps;
    private final Follower follower;
    private StarterShooter shooter;

    private int currentStepIndex;
    private double stepStartTime;
    private boolean isExecuting;

    // Connector path configuration
    private double reconnectDistanceThreshold = 6.0; // inches
    private double reconnectHeadingThreshold = Math.toRadians(20); // radians
    private boolean autoConnectEnabled = true;

    /**
     * Create a new autonomous builder
     * @param follower Pedro Pathing follower instance
     */
    public PedroAutonomousBuilder(Follower follower) {
        this.follower = follower;
        this.steps = new ArrayList<>();
        this.currentStepIndex = 0;
        this.stepStartTime = 0;
        this.isExecuting = false;
    }

    /**
     * Set the shooter instance for shooting actions
     * @param shooter StarterShooter instance
     * @return this builder for chaining
     */
    public PedroAutonomousBuilder withShooter(StarterShooter shooter) {
        this.shooter = shooter;
        return this;
    }

    /**
     * Add a path to follow
     * @param path PathChain to follow
     * @return this builder for chaining
     */
    public PedroAutonomousBuilder addPath(PathChain path) {
        steps.add(new PathStep(path, "Path"));
        return this;
    }

    /**
     * Add a path with a custom name
     * @param path PathChain to follow
     * @param name Name for telemetry
     * @return this builder for chaining
     */
    public PedroAutonomousBuilder addPath(PathChain path, String name) {
        steps.add(new PathStep(path, name));
        return this;
    }

    /**
     * Add a shooting action using StarterShooter
     * @param numShots Number of shots to fire
     * @param preset Shooter preset to use
     * @return this builder for chaining
     */
    public PedroAutonomousBuilder addShootAction(int numShots, StarterShooter.ShootingPreset preset) {
        if (shooter == null) {
            throw new IllegalStateException("Shooter not configured. Call withShooter() first.");
        }
        steps.add(new ShootAction(shooter, numShots, preset));
        return this;
    }

    /**
     * Add a turn to specific heading action
     * @param targetHeading Target heading in radians
     * @return this builder for chaining
     */
    public PedroAutonomousBuilder addTurnToHeading(double targetHeading) {
        steps.add(new TurnToHeadingAction(targetHeading));
        return this;
    }

    /**
     * Add a wait/pause action
     * @param durationSeconds How long to wait in seconds
     * @return this builder for chaining
     */
    public PedroAutonomousBuilder addWait(double durationSeconds) {
        steps.add(new WaitAction(durationSeconds));
        return this;
    }

    /**
     * Add a custom action with a lambda function
     * @param name Action name for telemetry
     * @param action Function that takes (follower, elapsedTime) and returns true when complete
     * @return this builder for chaining
     */
    public PedroAutonomousBuilder addCustomAction(String name, ActionFunction action) {
        steps.add(new CustomAction(name, action));
        return this;
    }

    /**
     * Start executing the autonomous sequence
     */
    public void start() {
        if (steps.isEmpty()) {
            return;
        }
        currentStepIndex = 0;
        stepStartTime = System.currentTimeMillis() / 1000.0;
        isExecuting = true;
        steps.get(0).onStart(follower, stepStartTime);
    }

    /**
     * Update the autonomous - call this in your loop() method
     * IMPORTANT: Also call follower.update() before this
     * @return Current step name for telemetry
     */
    public String update() {
        if (!isExecuting || currentStepIndex >= steps.size()) {
            return "FINISHED";
        }

        // Always update follower position tracking
        follower.update();

        AutonomousStep currentStep = steps.get(currentStepIndex);
        double currentTime = System.currentTimeMillis() / 1000.0;
        double elapsedTime = currentTime - stepStartTime;

        // Update current step
        boolean isComplete = currentStep.update(follower, elapsedTime);

        if (isComplete) {
            // Move to next step
            currentStepIndex++;
            if (currentStepIndex < steps.size()) {
                stepStartTime = currentTime;
                steps.get(currentStepIndex).onStart(follower, stepStartTime);
            } else {
                isExecuting = false;
            }
        }

        return currentStep.getName();
    }

    /**
     * Check if autonomous is finished
     * @return true if all steps are complete
     */
    public boolean isFinished() {
        return !isExecuting || currentStepIndex >= steps.size();
    }

    /**
     * Get current step index
     * @return Current step index (0-based)
     */
    public int getCurrentStepIndex() {
        return currentStepIndex;
    }

    /**
     * Get total number of steps
     * @return Total step count
     */
    public int getTotalSteps() {
        return steps.size();
    }

    /**
     * Get current robot pose (always tracked)
     * @return Current pose
     */
    public Pose getCurrentPose() {
        return follower.getPose();
    }

    // ==================== STEP DEFINITIONS ====================

    /**
     * Base interface for autonomous steps
     */
    private interface AutonomousStep {
        void onStart(Follower follower, double startTime);
        boolean update(Follower follower, double elapsedTime);
        String getName();
    }

    /**
     * Path following step
     */
    private static class PathStep implements AutonomousStep {
        private final PathChain path;
        private final String name;

        public PathStep(PathChain path, String name) {
            this.path = path;
            this.name = name;
        }

        @Override
        public void onStart(Follower follower, double startTime) {
            follower.followPath(path);
        }

        @Override
        public boolean update(Follower follower, double elapsedTime) {
            return !follower.isBusy();
        }

        @Override
        public String getName() {
            return name;
        }
    }

    /**
     * Shooting action step using StarterShooter
     */
    private static class ShootAction implements AutonomousStep {
        private final StarterShooter shooter;
        private final int numShots;
        private final StarterShooter.ShootingPreset preset;
        private int shotsFired = 0;
        private boolean isSpinningUp = true;
        private double lastShotTime = 0;
        private static final double SHOT_INTERVAL = 0.5; // seconds between shots

        public ShootAction(StarterShooter shooter, int numShots, StarterShooter.ShootingPreset preset) {
            this.shooter = shooter;
            this.numShots = numShots;
            this.preset = preset;
        }

        @Override
        public void onStart(Follower follower, double startTime) {
            shotsFired = 0;
            isSpinningUp = true;
            lastShotTime = 0;
            shooter.setPreset(preset);
            shooter.spinUp();
        }

        @Override
        public boolean update(Follower follower, double elapsedTime) {
            // CRITICAL: Update follower continuously during shooting to maintain position tracking
            follower.update();

            // Wait for spin up
            if (isSpinningUp) {
                if (shooter.isAtSpeed()) {
                    isSpinningUp = false;
                    lastShotTime = elapsedTime;
                }
                return false;
            }

            // Fire shots with interval
            if (shotsFired < numShots && (elapsedTime - lastShotTime) >= SHOT_INTERVAL) {
                shooter.fireOnce();
                shotsFired++;
                lastShotTime = elapsedTime;
            }

            // Complete when all shots fired
            if (shotsFired >= numShots) {
                shooter.stop();
                return true;
            }

            return false;
        }

        @Override
        public String getName() {
            if (isSpinningUp) {
                return "Shooting (Spinning up...)";
            }
            return "Shooting (" + shotsFired + "/" + numShots + ")";
        }
    }

    /**
     * Turn to heading action
     */
    private static class TurnToHeadingAction implements AutonomousStep {
        private final double targetHeading;
        private static final double HEADING_TOLERANCE = Math.toRadians(3);
        private static final double TIMEOUT = 2.0;
        private boolean turnStarted = false;

        public TurnToHeadingAction(double targetHeading) {
            this.targetHeading = targetHeading;
        }

        @Override
        public void onStart(Follower follower, double startTime) {
            Pose currentPose = follower.getPose();

            // Create a tiny path to trigger heading change
            double forwardDist = 0.1;
            double newX = currentPose.getX() + Math.cos(currentPose.getHeading()) * forwardDist;
            double newY = currentPose.getY() + Math.sin(currentPose.getHeading()) * forwardDist;

            PathChain turnPath = follower.pathBuilder()
                .addPath(new com.pedropathing.geometry.BezierLine(
                    new Pose(currentPose.getX(), currentPose.getY(), currentPose.getHeading()),
                    new Pose(newX, newY, targetHeading)
                ))
                .setLinearHeadingInterpolation(currentPose.getHeading(), targetHeading)
                .build();

            follower.followPath(turnPath);
            turnStarted = true;
        }

        @Override
        public boolean update(Follower follower, double elapsedTime) {
            if (!turnStarted) {
                return false;
            }

            // Check timeout
            if (elapsedTime > TIMEOUT) {
                return true;
            }

            // Check heading accuracy
            double currentHeading = follower.getPose().getHeading();
            double headingError = Math.abs(normalizeAngle(currentHeading - targetHeading));

            return headingError < HEADING_TOLERANCE || (!follower.isBusy() && headingError < Math.toRadians(5));
        }

        @Override
        public String getName() {
            return String.format(Locale.US, "Turn to %.1f°", Math.toDegrees(targetHeading));
        }

        private double normalizeAngle(double angle) {
            while (angle > Math.PI) angle -= 2 * Math.PI;
            while (angle < -Math.PI) angle += 2 * Math.PI;
            return angle;
        }
    }

    /**
     * Wait/pause action
     */
    private static class WaitAction implements AutonomousStep {
        private final double duration;

        public WaitAction(double duration) {
            this.duration = duration;
        }

        @Override
        public void onStart(Follower follower, double startTime) {
            // Nothing to start
        }

        @Override
        public boolean update(Follower follower, double elapsedTime) {
            return elapsedTime >= duration;
        }

        @Override
        public String getName() {
            return String.format(Locale.US, "Wait (%.1fs)", duration);
        }
    }

    /**
     * Custom action with lambda function
     */
    private static class CustomAction implements AutonomousStep {
        private final String name;
        private final ActionFunction action;

        public CustomAction(String name, ActionFunction action) {
            this.name = name;
            this.action = action;
        }

        @Override
        public void onStart(Follower follower, double startTime) {
            // Lambda handles initialization
        }

        @Override
        public boolean update(Follower follower, double elapsedTime) {
            return action.execute(follower, elapsedTime);
        }

        @Override
        public String getName() {
            return name;
        }
    }

    /**
     * Functional interface for custom actions
     */
    @FunctionalInterface
    public interface ActionFunction {
        /**
         * Execute the action
         * @param follower Follower instance for position tracking
         * @param elapsedTime Time elapsed since action started
         * @return true when action is complete
         */
        boolean execute(Follower follower, double elapsedTime);
    }
}

