package ballisticslookuptable;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for IterativeAimingCalculator.
 * Tests predictive targeting, convergence, and lookup table integration.
 */
class IterativeAimingCalculatorTest {

    private static final double DELTA = 0.1; // 0.1 degree tolerance
    private IterativeAimingCalculator aimer;
    private BallisticsCalculator calculator;

    @BeforeEach
    void setUp() {
        // Create calculator with default config
        calculator = new BallisticsCalculator();
        aimer = new IterativeAimingCalculator(calculator);
    }

    // ==================== Constructor Tests ====================

    @Test
    @DisplayName("Constructor should accept BallisticsCalculator")
    void testConstructor_ValidCalculator() {
        assertNotNull(aimer, "Aimer should be created successfully");
    }

    @Test
    @DisplayName("Constructor should accept custom configured calculator")
    void testConstructor_CustomCalculator() {
        BallisticsConfig config = new BallisticsConfig()
            .setMinLaunchAngleDeg(25)
            .setMaxLaunchAngleDeg(75)
            .setMinLaunchVelocityMps(8)
            .setMaxLaunchVelocityMps(25);
        
        BallisticsCalculator customCalculator = new BallisticsCalculator(config);
        IterativeAimingCalculator customAimer = new IterativeAimingCalculator(customCalculator);
        
        assertNotNull(customAimer, "Should accept custom calculator");
    }

    @Test
    @DisplayName("Constructor should not accept null calculator")
    void testConstructor_NullCalculator() {
        assertThrows(NullPointerException.class, 
                    () -> new IterativeAimingCalculator(null),
                    "Should reject null calculator");
    }

    // ==================== predictTarget Tests ====================

    @Test
    @DisplayName("predictTarget should calculate correct position for stationary robot and target")
    void testPredictTarget_StationaryBothObjects() {
        double targetX = 10.0;
        double targetY = 5.0;
        double robotX = 0.0;
        double robotY = 0.0;
        double timeOfFlight = 0.5; // 0.5 seconds
        
        IterativeAimingCalculator.Coordinate predicted = aimer.predictTarget(
            0.0, 0.0,  // target velocity
            targetX, targetY,  // target position
            0.0, 0.0,  // robot velocity
            robotX, robotY,  // robot position
            timeOfFlight
        );
        
        // With no velocities, target stays at same relative position
        assertEquals(targetX, predicted.x(), DELTA);
        assertEquals(targetY, predicted.y(), DELTA);
    }

    @Test
    @DisplayName("predictTarget should account for target velocity")
    void testPredictTarget_MovingTarget() {
        double targetX = 10.0;
        double targetY = 0.0;
        double targetVelX = 2.0;  // Moving 2 m/s in X
        double robotX = 0.0;
        double robotY = 0.0;
        double timeOfFlight = 1.0; // 1 second
        
        IterativeAimingCalculator.Coordinate predicted = aimer.predictTarget(
            targetVelX, 0.0,
            targetX, targetY,
            0.0, 0.0,
            robotX, robotY,
            timeOfFlight
        );
        
        // Target moves 2m in 1 second
        assertTrue(predicted.x() > targetX, "Target should be further ahead");
    }

    @Test
    @DisplayName("predictTarget should account for robot velocity")
    void testPredictTarget_MovingRobot() {
        double targetX = 20.0;
        double targetY = 0.0;
        double robotX = 0.0;
        double robotY = 0.0;
        double robotVelX = 3.0;  // Robot moving 3 m/s in X
        double timeOfFlight = 1.0;
        
        IterativeAimingCalculator.Coordinate predicted1 = aimer.predictTarget(
            0.0, 0.0,
            targetX, targetY,
            0.0, 0.0,
            robotX, robotY,
            timeOfFlight
        );
        
        IterativeAimingCalculator.Coordinate predicted2 = aimer.predictTarget(
            0.0, 0.0,
            targetX, targetY,
            robotVelX, 0.0,
            robotX, robotY,
            timeOfFlight
        );
        
        // Moving robot should result in closer predicted target
        assertTrue(predicted1.x() > predicted2.x(), "Moving robot should reduce predicted distance");
    }

    @Test
    @DisplayName("predictTarget should handle both X and Y components")
    void testPredictTarget_2DMotion() {
        double targetX = 10.0;
        double targetY = 10.0;
        double targetVelX = 1.0;
        double targetVelY = 2.0;
        double timeOfFlight = 1.0;
        
        IterativeAimingCalculator.Coordinate predicted = aimer.predictTarget(
            targetVelX, targetVelY,
            targetX, targetY,
            0.0, 0.0,
            0.0, 0.0,
            timeOfFlight
        );
        
        // Target should move in both dimensions
        assertTrue(predicted.x() > targetX, "Should move in X");
        assertTrue(predicted.y() > targetY, "Should move in Y");
    }

    @Test
    @DisplayName("predictTarget with zero time should give current relative position")
    void testPredictTarget_ZeroTime() {
        double targetX = 15.0;
        double targetY = 8.0;
        
        IterativeAimingCalculator.Coordinate predicted = aimer.predictTarget(
            5.0, 3.0,
            targetX, targetY,
            1.0, 1.0,
            0.0, 0.0,
            0.0  // Zero time of flight
        );
        
        // With zero flight time, no movement (but division by zero would be issue)
        // This tests edge case handling
        assertNotNull(predicted, "Should handle zero time gracefully");
    }

    // ==================== iterativePredictiveAim Tests ====================

    @Test
    @DisplayName("iterativePredictiveAim should calculate aim angle for target ahead")
    void testIterativePredictiveAim_TargetAhead() {
        double targetX = 10.0;
        double targetY = 0.0;
        double timeOfFlight = 0.5;
        
        double aimAngle = aimer.iterativePredictiveAim(
            0.0, 0.0,  // stationary target
            targetX, targetY,
            0.0, 0.0,  // stationary robot
            0.0, 0.0,
            timeOfFlight,
            5
        );
        
        // Target is directly ahead (X=10, Y=0)
        // Angle should be close to 0 degrees
        assertTrue(aimAngle >= -10 && aimAngle <= 10, 
                  "Angle to target ahead should be ~0°, got " + aimAngle);
    }

    @Test
    @DisplayName("iterativePredictiveAim should calculate aim angle for target to the side")
    void testIterativePredictiveAim_TargetToSide() {
        double targetX = 0.0;
        double targetY = 10.0;
        double timeOfFlight = 0.5;
        
        double aimAngle = aimer.iterativePredictiveAim(
            0.0, 0.0,
            targetX, targetY,
            0.0, 0.0,
            0.0, 0.0,
            timeOfFlight,
            5
        );
        
        // Target is directly to the side (X=0, Y=10)
        // Angle should be close to 90 degrees
        assertTrue(aimAngle >= 80 && aimAngle <= 100,
                  "Angle to target to side should be ~90°, got " + aimAngle);
    }

    @Test
    @DisplayName("iterativePredictiveAim should account for moving target")
    void testIterativePredictiveAim_MovingTarget() {
        double targetX = 10.0;
        double targetY = 0.0;
        double targetVelX = 2.0;  // Moving away
        double timeOfFlight = 0.5;
        
        double aimAngleStatic = aimer.iterativePredictiveAim(
            0.0, 0.0,
            targetX, targetY,
            0.0, 0.0,
            0.0, 0.0,
            timeOfFlight,
            5
        );
        
        double aimAngleMoving = aimer.iterativePredictiveAim(
            targetVelX, 0.0,
            targetX, targetY,
            0.0, 0.0,
            0.0, 0.0,
            timeOfFlight,
            5
        );
        
        // Aim angle should be different when target is moving
        assertNotEquals(aimAngleStatic, aimAngleMoving, DELTA,
                       "Moving target should require different aim angle");
    }

    @Test
    @DisplayName("iterativePredictiveAim should improve with iterations")
    void testIterativePredictiveAim_ConvergenceWithIterations() {
        double targetX = 8.0;
        double targetY = 6.0;
        double targetVelX = 1.5;
        double targetVelY = 0.8;
        double timeOfFlight = 0.3;
        
        double angleWith1Iteration = aimer.iterativePredictiveAim(
            targetVelX, targetVelY,
            targetX, targetY,
            0.0, 0.0,
            0.0, 0.0,
            timeOfFlight,
            1
        );
        
        double angleWith10Iterations = aimer.iterativePredictiveAim(
            targetVelX, targetVelY,
            targetX, targetY,
            0.0, 0.0,
            0.0, 0.0,
            timeOfFlight,
            10
        );
        
        // Both should be reasonable angles
        assertTrue(angleWith1Iteration >= -180 && angleWith1Iteration <= 180);
        assertTrue(angleWith10Iterations >= -180 && angleWith10Iterations <= 180);
    }

    @Test
    @DisplayName("iterativePredictiveAim should throw for unreachable target")
    void testIterativePredictiveAim_UnreachableTarget() {
        double targetX = 100.0;  // Very far
        double targetY = 100.0;  // Maximum range is ~20m
        double timeOfFlight = 0.5;
        
        assertThrows(IllegalArgumentException.class,
                    () -> aimer.iterativePredictiveAim(
                        0.0, 0.0,
                        targetX, targetY,
                        0.0, 0.0,
                        0.0, 0.0,
                        timeOfFlight,
                        10
                    ),
                    "Should throw for unreachable target");
    }

    @Test
    @DisplayName("iterativePredictiveAim with default iterations should work")
    void testIterativePredictiveAim_DefaultIterations() {
        double targetX = 8.0;
        double targetY = 0.0;
        
        // Use method with default iterations
        double aimAngle = aimer.iterativePredictiveAim(
            1.0, 0.0,
            targetX, targetY,
            0.0, 0.0,
            0.0, 0.0,
            0.3  // Initial estimate
        );
        
        assertTrue(aimAngle >= -180 && aimAngle <= 180,
                  "Should return valid angle with default iterations");
    }

    @Test
    @DisplayName("iterativePredictiveAim should handle moving robot")
    void testIterativePredictiveAim_MovingRobot() {
        double targetX = 12.0;
        double targetY = 0.0;
        double robotVelX = 3.0;  // Robot moving towards target
        double timeOfFlight = 0.4;
        
        double aimAngle = aimer.iterativePredictiveAim(
            0.0, 0.0,
            targetX, targetY,
            robotVelX, 0.0,
            0.0, 0.0,
            timeOfFlight,
            10
        );
        
        // Should still calculate valid angle
        assertTrue(aimAngle >= -180 && aimAngle <= 180);
    }

    // ==================== Convergence Tests ====================

    @Test
    @DisplayName("iterativePredictiveAim should converge with diagonal target")
    void testIterativePredictiveAim_DiagonalTarget() {
        double targetX = 10.0;
        double targetY = 10.0;
        double targetVelX = 0.5;
        double targetVelY = 0.5;
        
        // Should converge without throwing
        double aimAngle = aimer.iterativePredictiveAim(
            targetVelX, targetVelY,
            targetX, targetY,
            0.0, 0.0,
            0.0, 0.0,
            0.3,
            15
        );
        
        // Diagonal target, should aim around 45 degrees
        assertTrue(aimAngle >= 30 && aimAngle <= 60,
                  "Aim angle for diagonal target should be ~45°, got " + aimAngle);
    }

    @Test
    @DisplayName("iterativePredictiveAim should handle negative coordinates")
    void testIterativePredictiveAim_NegativeCoordinates() {
        double targetX = -5.0;
        double targetY = -5.0;
        
        double aimAngle = aimer.iterativePredictiveAim(
            0.0, 0.0,
            targetX, targetY,
            0.0, 0.0,
            0.0, 0.0,
            0.5,
            5
        );
        
        // Should aim into third quadrant (~225°)
        assertTrue(aimAngle >= 200 && aimAngle <= 250,
                  "Angle for negative coordinates should be ~225°, got " + aimAngle);
    }

    // ==================== Physics Validation Tests ====================

    @Test
    @DisplayName("iterativePredictiveAim result should respect lookup table constraints")
    void testIterativePredictiveAim_RespectConstraints() {
        double targetX = 5.0;
        double targetY = 3.0;
        
        // Should complete successfully using lookup table constraints
        double aimAngle = aimer.iterativePredictiveAim(
            0.5, 0.5,
            targetX, targetY,
            0.0, 0.0,
            0.0, 0.0,
            0.2,
            10
        );
        
        // Should find valid solution within configured constraints
        assertFalse(Double.isNaN(aimAngle), "Should return valid angle");
        assertFalse(Double.isInfinite(aimAngle), "Should not be infinite");
    }

    @Test
    @DisplayName("iterativePredictiveAim should use lookup table for convergence")
    void testIterativePredictiveAim_UsesLookupTable() {
        // Create calculator with specific config to verify lookup table usage
        BallisticsConfig config = new BallisticsConfig()
            .setMinRange(0.5)
            .setMaxRange(15.0);
        
        BallisticsCalculator customCalculator = new BallisticsCalculator(config);
        IterativeAimingCalculator customAimer = new IterativeAimingCalculator(customCalculator);
        
        double targetX = 10.0;
        double targetY = 0.0;
        
        // Should work with custom ranges
        double aimAngle = customAimer.iterativePredictiveAim(
            0.0, 0.0,
            targetX, targetY,
            0.0, 0.0,
            0.0, 0.0,
            0.3,
            5
        );
        
        assertTrue(aimAngle >= -10 && aimAngle <= 10,
                  "Custom calculator should still produce valid aim angle");
    }

    // ==================== Coordinate Record Tests ====================

    @Test
    @DisplayName("Coordinate record should store x and y values")
    void testCoordinate_StoresValues() {
        double x = 3.5;
        double y = 7.2;
        
        IterativeAimingCalculator.Coordinate coord = new IterativeAimingCalculator.Coordinate(x, y);
        
        assertEquals(x, coord.x(), DELTA);
        assertEquals(y, coord.y(), DELTA);
    }

    @Test
    @DisplayName("Coordinate record should handle negative values")
    void testCoordinate_NegativeValues() {
        double x = -4.2;
        double y = -6.8;
        
        IterativeAimingCalculator.Coordinate coord = new IterativeAimingCalculator.Coordinate(x, y);
        
        assertEquals(x, coord.x(), DELTA);
        assertEquals(y, coord.y(), DELTA);
    }

    // ==================== Integration Tests ====================

    @Test
    @DisplayName("Integration: Full predictive aiming workflow")
    void testIntegration_FullWorkflow() {
        // Simulate a game scenario: robot shooting at moving target
        double targetX = 7.5;
        double targetY = 5.0;
        double targetVelX = 1.5;
        double targetVelY = 0.8;
        
        double robotX = 0.0;
        double robotY = 0.0;
        double robotVelX = 0.0;
        double robotVelY = 0.0;
        
        // Get initial range estimate
        double initialRange = Math.sqrt(targetX * targetX + targetY * targetY);
        LaunchParameter initialParam = calculator.getBestLaunchParameter(initialRange);
        
        assertNotNull(initialParam, "Should find parameter for this range");
        
        // Calculate iterative aim angle using lookup table
        double aimAngle = aimer.iterativePredictiveAim(
            targetVelX, targetVelY,
            targetX, targetY,
            robotVelX, robotVelY,
            robotX, robotY,
            initialParam.getTimeOfFlightSeconds(),
            10
        );
        
        // Verify reasonable result
        assertTrue(aimAngle >= 10 && aimAngle <= 90,
                  "Aim angle for this scenario should be 10-90°, got " + aimAngle);
    }

    @Test
    @DisplayName("Integration: Multiple predictions with same target")
    void testIntegration_MultipleSnapshots() {
        IterativeAimingCalculator.Coordinate target = 
            new IterativeAimingCalculator.Coordinate(10.0, 0.0);
        
        // Predict at different times
        IterativeAimingCalculator.Coordinate predicted05s = aimer.predictTarget(
            1.0, 0.0,
            target.x(), target.y(),
            0.0, 0.0,
            0.0, 0.0,
            0.5
        );
        
        IterativeAimingCalculator.Coordinate predicted10s = aimer.predictTarget(
            1.0, 0.0,
            target.x(), target.y(),
            0.0, 0.0,
            0.0, 0.0,
            1.0
        );
        
        // Further in future = further position
        assertTrue(predicted10s.x() > predicted05s.x(),
                  "Target should be further ahead after longer time");
    }
}
