/* Copyright (c) 2025 FTC DECODE Starter Kit. All rights reserved.
 *
 * PanelsDashboardHelper - Unified dashboard interface for Panels telemetry and field visualization
 * 
 * This helper class provides a clean API for sending telemetry and drawing on the field view
 * without needing to understand Panels internals.
 */

package org.firstinspires.ftc.teamcode.dashboard;

import com.bylazar.field.FieldManager;
import com.bylazar.field.PanelsField;
import com.bylazar.field.Style;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.List;

/**
 * PanelsDashboardHelper - Centralized dashboard interface
 * 
 * Purpose:
 * - Wrap Panels telemetry and field visualization APIs
 * - Provide simple methods for common tasks (log data, draw robot, draw paths)
 * - Handle coordinate system conversions
 * - Fall back to standard SDK telemetry if Panels unavailable
 * 
 * Why use this?
 * - One API for both Driver Station and Panels dashboard
 * - Easy to add telemetry without learning Panels internals
 * - Automatic fallback if Panels isn't running
 * - Consistent coordinate system handling
 * 
 * How to use:
 * 1. Create instance in OpMode init:
 *    PanelsDashboardHelper dashboard = new PanelsDashboardHelper(telemetry);
 * 
 * 2. Log telemetry in loop:
 *    dashboard.log("X Position", pose.getX());
 *    dashboard.log("Y Position", pose.getY());
 * 
 * 3. Draw robot on field:
 *    dashboard.setRobotPose(x, y, heading);
 * 
 * 4. Update at end of loop:
 *    dashboard.update();
 */
public class PanelsDashboardHelper {
    
    // ═══════════════════════════════════════════════════════════════
    // TELEMETRY MANAGERS
    // ═══════════════════════════════════════════════════════════════
    
    private final Telemetry sdkTelemetry;      // Standard Driver Station telemetry
    private TelemetryManager panelsTelemetry;  // Panels telemetry (may be null)
    private FieldManager panelsField;          // Panels field view (may be null)
    
    private boolean panelsAvailable = false;   // Track if Panels is working
    
    // ═══════════════════════════════════════════════════════════════
    // FIELD VISUALIZATION STYLES
    // ═══════════════════════════════════════════════════════════════
    
    // Robot marker style (green circle)
    private static final Style ROBOT_STYLE = new Style("Robot", "#00FF00", 0.75);
    private static final double ROBOT_RADIUS = 9.0;  // Inches
    
    // Path style (blue line)
    private static final Style PATH_STYLE = new Style("Path", "#3F51B5", 0.75);
    
    // Target/waypoint style (red circle)
    private static final Style TARGET_STYLE = new Style("Target", "#FF0000", 0.75);
    
    // ═══════════════════════════════════════════════════════════════
    // COORDINATE SYSTEM
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * FTC/Pedro Coordinate System (Right-Hand System):
     * 
     * Origin (0, 0):
     * - Located at field center
     * 
     * Axes:
     * - +X points toward red alliance wall
     * - +Y points toward blue audience side
     * - +Heading (rotation) is counterclockwise
     *   - 0° = facing +X direction (toward red wall)
     *   - 90° = facing +Y direction (toward blue audience)
     *   - 180° = facing -X direction (toward blue wall)
     *   - 270° = facing -Y direction (toward red audience)
     * 
     * Field Dimensions:
     * - Width: 144 inches (12 feet)
     * - Height: 144 inches (12 feet)
     * - X range: -72 to +72 inches
     * - Y range: -72 to +72 inches
     * 
     * Panels Field View:
     * - Uses same coordinate system
     * - Automatically scales for field size
     * - Rotation handled correctly by Panels
     */
    
    // ═══════════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Initialize dashboard helper
     * 
     * Attempts to connect to Panels dashboard. If Panels is not running or
     * fails to initialize, gracefully falls back to SDK telemetry only.
     * 
     * @param sdkTelemetry Telemetry object from OpMode
     */
    public PanelsDashboardHelper(Telemetry sdkTelemetry) {
        this.sdkTelemetry = sdkTelemetry;
        
        try {
            // Attempt to get Panels telemetry instance
            panelsTelemetry = PanelsTelemetry.INSTANCE.getTelemetry();
            
            // Attempt to get Panels field manager
            panelsField = PanelsField.INSTANCE.getField();
            
            // Set field offsets for Pedro Pathing coordinate system
            panelsField.setOffsets(PanelsField.INSTANCE.getPresets().getPEDRO_PATHING());
            
            panelsAvailable = true;
            sdkTelemetry.addLine("✅ Panels dashboard connected");
            sdkTelemetry.addLine("   Access at: http://192.168.43.1:8080");
            
        } catch (Exception e) {
            // Panels not available - fall back to SDK telemetry only
            panelsAvailable = false;
            panelsTelemetry = null;
            panelsField = null;
            
            sdkTelemetry.addLine("⚠️  Panels dashboard not available");
            sdkTelemetry.addLine("   Using Driver Station telemetry only");
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // TELEMETRY METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Log a key-value pair to both SDK and Panels telemetry
     * 
     * @param caption Label for the data
     * @param value Value to display
     */
    public void log(String caption, Object value) {
        // Always log to SDK telemetry
        sdkTelemetry.addData(caption, value);
        
        // Log to Panels if available
        if (panelsAvailable && panelsTelemetry != null) {
            try {
                panelsTelemetry.debug(caption, value);
            } catch (Exception e) {
                // Panels failed - disable it
                panelsAvailable = false;
            }
        }
    }
    
    /**
     * Log a formatted key-value pair
     * 
     * @param caption Label for the data
     * @param format Format string (e.g., "%.2f")
     * @param args Arguments for format string
     */
    public void log(String caption, String format, Object... args) {
        String formatted = String.format(format, args);
        log(caption, formatted);
    }
    
    /**
     * Log a line of text (no key-value pair)
     * 
     * @param text Text to display
     */
    public void logLine(String text) {
        sdkTelemetry.addLine(text);
        
        if (panelsAvailable && panelsTelemetry != null) {
            try {
                panelsTelemetry.debug("", text);
            } catch (Exception e) {
                panelsAvailable = false;
            }
        }
    }
    
    /**
     * Get the raw SDK telemetry object
     * 
     * Use this for direct telemetry calls that don't need Panels mirroring
     * 
     * @return SDK Telemetry object
     */
    public Telemetry getSdkTelemetry() {
        return sdkTelemetry;
    }
    
    // ═══════════════════════════════════════════════════════════════
    // FIELD VISUALIZATION METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Draw robot position on field view
     * 
     * This displays the robot as a circle at its current position.
     * The circle is oriented to show heading direction.
     * 
     * @param xInches X position in inches (field coordinate system)
     * @param yInches Y position in inches (field coordinate system)
     * @param headingDeg Heading in degrees (0° = facing +X)
     */
    public void setRobotPose(double xInches, double yInches, double headingDeg) {
        if (!panelsAvailable || panelsField == null) {
            return;  // Panels not available
        }
        
        // Validate inputs - skip if NaN
        if (Double.isNaN(xInches) || Double.isNaN(yInches) || Double.isNaN(headingDeg)) {
            return;
        }

        try {
            // Set style for robot drawing
            panelsField.setStyle(ROBOT_STYLE);

            // Draw robot as a circle
            panelsField.moveCursor(xInches, yInches);
            panelsField.circle(ROBOT_RADIUS);

            // Draw heading indicator (line from center to edge)
            double headingRad = Math.toRadians(headingDeg);
            double endX = xInches + ROBOT_RADIUS * Math.cos(headingRad);
            double endY = yInches + ROBOT_RADIUS * Math.sin(headingRad);
            panelsField.moveCursor(xInches, yInches);
            panelsField.line(endX, endY);

        } catch (Exception e) {
            panelsAvailable = false;
        }
    }
    
    /**
     * Draw robot position from Pedro Pose object
     * 
     * Convenience method that extracts position and heading from Pose.
     * Includes validation to handle null or invalid pose data.
     *
     * @param pose Pedro Pose object (x, y, heading in radians)
     */
    public void setRobotPose(Pose pose) {
        if (pose == null || Double.isNaN(pose.getX()) || Double.isNaN(pose.getY()) || Double.isNaN(pose.getHeading())) {
            return;
        }

        if (!panelsAvailable || panelsField == null) {
            return;
        }

        try {
            // Set style for robot drawing
            panelsField.setStyle(ROBOT_STYLE);

            // Draw robot as a circle
            panelsField.moveCursor(pose.getX(), pose.getY());
            panelsField.circle(ROBOT_RADIUS);

            // Draw heading indicator (line from center to edge)
            double x2 = pose.getX() + ROBOT_RADIUS * Math.cos(pose.getHeading());
            double y2 = pose.getY() + ROBOT_RADIUS * Math.sin(pose.getHeading());
            panelsField.moveCursor(pose.getX(), pose.getY());
            panelsField.line(x2, y2);

        } catch (Exception e) {
            panelsAvailable = false;
        }
    }
    
    /**
     * Draw a path on the field view
     * 
     * This displays a planned or active path as a series of line segments.
     * 
     * @param waypoints List of (x, y) positions along the path
     */
    public void drawPath(List<double[]> waypoints) {
        if (!panelsAvailable || panelsField == null || waypoints == null || waypoints.size() < 2) {
            return;
        }
        
        try {
            // Set style for path drawing
            panelsField.setStyle(PATH_STYLE);

            // Draw line segments between consecutive waypoints
            double[] start = waypoints.get(0);
            panelsField.moveCursor(start[0], start[1]);

            for (int i = 1; i < waypoints.size(); i++) {
                double[] point = waypoints.get(i);
                panelsField.line(point[0], point[1]);
                panelsField.moveCursor(point[0], point[1]);
            }
        } catch (Exception e) {
            panelsAvailable = false;
        }
    }

    /**
     * Draw a Pedro Pathing path on the field view
     *
     * Uses Pedro's built-in getPanelsDrawingPoints() method for accurate path visualization.
     * Includes NaN validation and cleanup.
     *
     * @param path Pedro Path object to draw
     */
    public void drawPedroPath(com.pedropathing.paths.Path path) {
        if (!panelsAvailable || panelsField == null || path == null) {
            return;
        }

        try {
            double[][] points = path.getPanelsDrawingPoints();

            if (points == null || points.length < 2 || points[0].length == 0) {
                return;
            }

            // Clean up NaN values
            for (int i = 0; i < points[0].length; i++) {
                for (int j = 0; j < points.length; j++) {
                    if (Double.isNaN(points[j][i])) {
                        points[j][i] = 0;
                    }
                }
            }

            // Set style and draw path
            panelsField.setStyle(PATH_STYLE);
            panelsField.moveCursor(points[0][0], points[1][0]);

            // Draw lines connecting each point
            for (int i = 1; i < points[0].length; i++) {
                panelsField.line(points[0][i], points[1][i]);
            }
        } catch (Exception e) {
            panelsAvailable = false;
        }
    }
    
    /**
     * Draw a target position on the field
     * 
     * Useful for showing goal positions or intermediate waypoints.
     * 
     * @param xInches X position in inches
     * @param yInches Y position in inches
     * @param radius Radius of target marker
     */
    public void drawTarget(double xInches, double yInches, double radius) {
        if (!panelsAvailable || panelsField == null) {
            return;
        }
        
        try {
            // Set style for target drawing
            panelsField.setStyle(TARGET_STYLE);

            // Draw target as a circle
            panelsField.moveCursor(xInches, yInches);
            panelsField.circle(radius);
        } catch (Exception e) {
            panelsAvailable = false;
        }
    }
    
    /**
     * Clear all field overlays
     * 
     * Note: Panels Field automatically redraws on each update() call.
     * The field is effectively cleared when you draw new content and call update().
     * This method is kept for API compatibility but does nothing.
     */
    public void clearFieldOverlays() {
        // No-op: Panels Field redraws automatically on update()
        // Each update() call shows only what was drawn since the last update()
    }
    
    // ═══════════════════════════════════════════════════════════════
    // UPDATE METHOD
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Update telemetry and field view
     * 
     * IMPORTANT: Call this at the end of every loop iteration!
     * 
     * This flushes all queued telemetry to both Driver Station and Panels.
     */
    public void update() {
        // Update SDK telemetry (always)
        sdkTelemetry.update();
        
        // Update Panels telemetry if available
        if (panelsAvailable && panelsTelemetry != null) {
            try {
                panelsTelemetry.update(sdkTelemetry);
            } catch (Exception e) {
                panelsAvailable = false;
            }
        }

        // Update Panels field view if available
        if (panelsAvailable && panelsField != null) {
            try {
                panelsField.update();
            } catch (Exception e) {
                panelsAvailable = false;
            }
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // STATUS METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Check if Panels dashboard is available
     * 
     * @return true if Panels is connected and working
     */
    public boolean isPanelsAvailable() {
        return panelsAvailable;
    }
    
    /**
     * Get Panels dashboard URL
     * 
     * @return URL string or null if Panels not available
     */
    public String getPanelsUrl() {
        if (panelsAvailable) {
            return "http://192.168.43.1:8080";
        }
        return null;
    }
}
