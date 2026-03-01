package ballisticslookuptable;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for LaunchParameter data class.
 * Tests constructor initialization, getters, setters, and toString.
 */
class LaunchParameterTest {

    private static final double DELTA = 0.01; // Tolerance for floating point comparisons

    // ==================== Constructor Tests ====================

    @Test
    @DisplayName("Constructor should initialize all fields correctly")
    void testConstructor_ValidParameters() {
        double angle = 45.0;
        double range = 10.0;
        double elevation = 1.0;
        
        LaunchParameter param = new LaunchParameter(angle, range, elevation);
        
        assertEquals(range, param.getRangeMeters(), DELTA);
        assertEquals(angle, param.getLaunchAngleDeg(), DELTA);
        assertTrue(param.getLaunchVelocityMps() > 0, "Velocity should be calculated and positive");
        assertFalse(Double.isNaN(param.getImpactAngleDeg()), "Impact angle should be calculated");
        assertTrue(param.getPeakHeightMeters() > 0, "Peak height should be positive");
        assertTrue(param.getTimeOfFlightSeconds() > 0, "Flight time should be positive");
        assertEquals(0.0, param.getScore(), DELTA, "Initial score should be 0");
    }

    @Test
    @DisplayName("Constructor with steep angle should calculate correctly")
    void testConstructor_SteepAngle() {
        double angle = 85.0;
        double range = 5.0;
        double elevation = 1.0;
        
        LaunchParameter param = new LaunchParameter(angle, range, elevation);
        
        assertEquals(angle, param.getLaunchAngleDeg(), DELTA);
        assertEquals(range, param.getRangeMeters(), DELTA);
        assertTrue(param.getPeakHeightMeters() > range, "Steep angle should produce high arc");
    }

    @Test
    @DisplayName("Constructor with shallow angle should calculate correctly")
    void testConstructor_ShallowAngle() {
        double angle = 15.0;
        double range = 10.0;
        double elevation = 0.0;
        
        LaunchParameter param = new LaunchParameter(angle, range, elevation);
        
        assertEquals(angle, param.getLaunchAngleDeg(), DELTA);
        assertTrue(param.getLaunchVelocityMps() > 0, "Shallow angle should require velocity");
    }

    @Test
    @DisplayName("Constructor with zero elevation (level ground)")
    void testConstructor_LevelGround() {
        double angle = 45.0;
        double range = 8.0;
        double elevation = 0.0;
        
        LaunchParameter param = new LaunchParameter(angle, range, elevation);
        
        assertEquals(elevation, 0.0, DELTA);
        assertTrue(param.getLaunchVelocityMps() > 0);
        assertTrue(param.getImpactAngleDeg() < 0, "Impact should be downward on level ground");
    }

    @Test
    @DisplayName("Constructor with positive elevation (upward target)")
    void testConstructor_PositiveElevation() {
        double angle = 60.0;
        double range = 5.0;
        double elevation = 3.0;
        
        LaunchParameter param = new LaunchParameter(angle, range, elevation);
        
        assertTrue(param.getLaunchVelocityMps() > 0, "Should calculate velocity for upward target");
        assertTrue(param.getTimeOfFlightSeconds() > 0, "Should calculate flight time");
    }

    @Test
    @DisplayName("Constructor with negative elevation (downward target)")
    void testConstructor_NegativeElevation() {
        double angle = 30.0;
        double range = 10.0;
        double elevation = -2.0;
        
        LaunchParameter param = new LaunchParameter(angle, range, elevation);
        
        assertTrue(param.getLaunchVelocityMps() > 0, "Should calculate velocity for downward target");
        assertTrue(param.getImpactAngleDeg() < 0, "Impact angle should be downward");
    }

    @Test
    @DisplayName("Constructor with impossible trajectory should handle gracefully")
    void testConstructor_ImpossibleTrajectory() {
        double angle = 5.0; // Very shallow
        double range = 100.0; // Very far
        double elevation = 50.0; // Very high
        
        LaunchParameter param = new LaunchParameter(angle, range, elevation);
        
        // Should complete without exception, even if values are invalid
        assertNotNull(param);
        assertTrue(param.getLaunchVelocityMps() <= 0, "Impossible trajectory should have invalid velocity");
    }

    @Test
    @DisplayName("Constructor with 90 degree angle (vertical shot)")
    void testConstructor_VerticalShot() {
        double angle = 90.0;
        double range = 0.0; // No horizontal range
        double elevation = 5.0;
        
        LaunchParameter param = new LaunchParameter(angle, range, elevation);
        
        assertEquals(90.0, param.getLaunchAngleDeg(), DELTA);
        // Vertical shot to horizontal range should be problematic
    }

    @Test
    @DisplayName("Constructor with zero angle (horizontal shot)")
    void testConstructor_HorizontalShot() {
        double angle = 0.0;
        double range = 10.0;
        double elevation = 0.0;
        
        LaunchParameter param = new LaunchParameter(angle, range, elevation);
        
        assertEquals(0.0, param.getLaunchAngleDeg(), DELTA);
        assertTrue(param.getLaunchVelocityMps() <= 0, "Horizontal shot to level target impossible");
    }

    // ==================== Getter Tests ====================

    @Test
    @DisplayName("getRangeMeters should return correct value")
    void testGetRangeMeters() {
        double expectedRange = 12.5;
        LaunchParameter param = new LaunchParameter(45.0, expectedRange, 1.0);
        
        assertEquals(expectedRange, param.getRangeMeters(), DELTA);
    }

    @Test
    @DisplayName("getLaunchAngleDeg should return correct value")
    void testGetLaunchAngleDeg() {
        double expectedAngle = 52.3;
        LaunchParameter param = new LaunchParameter(expectedAngle, 10.0, 1.0);
        
        assertEquals(expectedAngle, param.getLaunchAngleDeg(), DELTA);
    }

    @Test
    @DisplayName("getLaunchVelocityMps should return calculated value")
    void testGetLaunchVelocityMps() {
        LaunchParameter param = new LaunchParameter(45.0, 10.0, 1.0);
        
        double velocity = param.getLaunchVelocityMps();
        assertTrue(velocity > 0, "Valid trajectory should have positive velocity");
    }

    @Test
    @DisplayName("getImpactAngleDeg should return calculated value")
    void testGetImpactAngleDeg() {
        LaunchParameter param = new LaunchParameter(45.0, 10.0, 0.0);
        
        double impactAngle = param.getImpactAngleDeg();
        assertFalse(Double.isNaN(impactAngle), "Valid trajectory should have valid impact angle");
        assertTrue(impactAngle < 0, "Impact on level ground should be downward");
    }

    @Test
    @DisplayName("getPeakHeightMeters should return calculated value")
    void testGetPeakHeightMeters() {
        LaunchParameter param = new LaunchParameter(60.0, 8.0, 1.0);
        
        double peakHeight = param.getPeakHeightMeters();
        assertTrue(peakHeight > 0, "Peak height should be positive");
    }

    @Test
    @DisplayName("getTimeOfFlightSeconds should return calculated value")
    void testGetTimeOfFlightSeconds() {
        LaunchParameter param = new LaunchParameter(45.0, 10.0, 1.0);
        
        double time = param.getTimeOfFlightSeconds();
        assertTrue(time > 0, "Valid trajectory should have positive flight time");
    }

    @Test
    @DisplayName("getScore should return initial value of 0")
    void testGetScore_Initial() {
        LaunchParameter param = new LaunchParameter(45.0, 10.0, 1.0);
        
        assertEquals(0.0, param.getScore(), DELTA);
    }

    // ==================== Setter Tests ====================

    @Test
    @DisplayName("setScore should update score value")
    void testSetScore() {
        LaunchParameter param = new LaunchParameter(45.0, 10.0, 1.0);
        double newScore = 0.85;
        
        param.setScore(newScore);
        
        assertEquals(newScore, param.getScore(), DELTA);
    }

    @Test
    @DisplayName("setScore with zero should work")
    void testSetScore_Zero() {
        LaunchParameter param = new LaunchParameter(45.0, 10.0, 1.0);
        param.setScore(0.5);
        
        param.setScore(0.0);
        
        assertEquals(0.0, param.getScore(), DELTA);
    }

    @Test
    @DisplayName("setScore with negative value should work")
    void testSetScore_Negative() {
        LaunchParameter param = new LaunchParameter(45.0, 10.0, 1.0);
        double negativeScore = -0.5;
        
        param.setScore(negativeScore);
        
        assertEquals(negativeScore, param.getScore(), DELTA);
    }

    @Test
    @DisplayName("setScore with value greater than 1 should work")
    void testSetScore_GreaterThanOne() {
        LaunchParameter param = new LaunchParameter(45.0, 10.0, 1.0);
        double highScore = 1.5;
        
        param.setScore(highScore);
        
        assertEquals(highScore, param.getScore(), DELTA);
    }

    @Test
    @DisplayName("setScore multiple times should update correctly")
    void testSetScore_MultipleUpdates() {
        LaunchParameter param = new LaunchParameter(45.0, 10.0, 1.0);
        
        param.setScore(0.3);
        assertEquals(0.3, param.getScore(), DELTA);
        
        param.setScore(0.7);
        assertEquals(0.7, param.getScore(), DELTA);
        
        param.setScore(1.0);
        assertEquals(1.0, param.getScore(), DELTA);
    }

    // ==================== toString Tests ====================

    @Test
    @DisplayName("toString should contain all field names")
    void testToString_ContainsFieldNames() {
        LaunchParameter param = new LaunchParameter(45.0, 10.0, 1.0);
        param.setScore(0.75);
        
        String result = param.toString();
        
        assertTrue(result.contains("rangeMeters"), "Should contain rangeMeters");
        assertTrue(result.contains("launchAngleDeg"), "Should contain launchAngleDeg");
        assertTrue(result.contains("launchVelocityMps"), "Should contain launchVelocityMps");
        assertTrue(result.contains("impactAngleDeg"), "Should contain impactAngleDeg");
        assertTrue(result.contains("peakHeightMeters"), "Should contain peakHeightMeters");
        assertTrue(result.contains("timeOfFlightSeconds"), "Should contain timeOfFlightSeconds");
        assertTrue(result.contains("score"), "Should contain score");
    }

    @Test
    @DisplayName("toString should contain formatted values")
    void testToString_ContainsFormattedValues() {
        double range = 10.0;
        double angle = 45.0;
        LaunchParameter param = new LaunchParameter(angle, range, 1.0);
        param.setScore(0.85);
        
        String result = param.toString();
        
        // Check for formatted values (2 decimal places)
        assertTrue(result.contains("10.00"), "Should contain formatted range");
        assertTrue(result.contains("45.00"), "Should contain formatted angle");
        assertTrue(result.contains("0.85"), "Should contain formatted score");
    }

    @Test
    @DisplayName("toString should start with class name")
    void testToString_StartsWithClassName() {
        LaunchParameter param = new LaunchParameter(45.0, 10.0, 1.0);
        
        String result = param.toString();
        
        assertTrue(result.startsWith("LaunchParameter{"), "Should start with class name");
    }

    @Test
    @DisplayName("toString should not return null")
    void testToString_NotNull() {
        LaunchParameter param = new LaunchParameter(45.0, 10.0, 1.0);
        
        String result = param.toString();
        
        assertNotNull(result, "toString should never return null");
    }

    // ==================== Edge Case Tests ====================

    @Test
    @DisplayName("Very small range should be handled")
    void testEdgeCase_VerySmallRange() {
        LaunchParameter param = new LaunchParameter(45.0, 0.1, 0.5);
        
        assertEquals(0.1, param.getRangeMeters(), DELTA);
        assertNotNull(param);
    }

    @Test
    @DisplayName("Very large range should be handled")
    void testEdgeCase_VeryLargeRange() {
        LaunchParameter param = new LaunchParameter(45.0, 100.0, 1.0);
        
        assertEquals(100.0, param.getRangeMeters(), DELTA);
        assertNotNull(param);
    }

    @Test
    @DisplayName("Peak height should be greater than or equal to launcher height")
    void testPeakHeight_AboveLauncher() {
        double launcherHeight = 0.0; // Assumed in LaunchParameter constructor
        LaunchParameter param = new LaunchParameter(45.0, 10.0, 1.0);
        
        assertTrue(param.getPeakHeightMeters() >= launcherHeight, 
                   "Peak height should be at or above launcher");
    }

    @Test
    @DisplayName("Flight time should be reasonable for given range")
    void testFlightTime_Reasonable() {
        double range = 10.0;
        LaunchParameter param = new LaunchParameter(45.0, range, 0.0);
        
        double time = param.getTimeOfFlightSeconds();
        // Flight time shouldn't be absurdly long (e.g., < 10 seconds for 10m range)
        assertTrue(time > 0 && time < 10, "Flight time should be reasonable");
    }

    @Test
    @DisplayName("Compare two LaunchParameters with same inputs")
    void testComparison_SameInputs() {
        double angle = 50.0;
        double range = 12.0;
        double elevation = 1.5;
        
        LaunchParameter param1 = new LaunchParameter(angle, range, elevation);
        LaunchParameter param2 = new LaunchParameter(angle, range, elevation);
        
        assertEquals(param1.getRangeMeters(), param2.getRangeMeters(), DELTA);
        assertEquals(param1.getLaunchAngleDeg(), param2.getLaunchAngleDeg(), DELTA);
        assertEquals(param1.getLaunchVelocityMps(), param2.getLaunchVelocityMps(), DELTA);
        assertEquals(param1.getImpactAngleDeg(), param2.getImpactAngleDeg(), DELTA);
        assertEquals(param1.getPeakHeightMeters(), param2.getPeakHeightMeters(), DELTA);
        assertEquals(param1.getTimeOfFlightSeconds(), param2.getTimeOfFlightSeconds(), DELTA);
    }

    @Test
    @DisplayName("Different angles for same range should produce different results")
    void testComparison_DifferentAngles() {
        double range = 10.0;
        double elevation = 1.0;
        
        LaunchParameter param1 = new LaunchParameter(30.0, range, elevation);
        LaunchParameter param2 = new LaunchParameter(60.0, range, elevation);
        
        assertNotEquals(param1.getLaunchVelocityMps(), param2.getLaunchVelocityMps(), DELTA);
        assertNotEquals(param1.getPeakHeightMeters(), param2.getPeakHeightMeters(), DELTA);
    }
}
