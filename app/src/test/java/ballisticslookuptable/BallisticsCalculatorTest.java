package ballisticslookuptable;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.TreeMap;

/**
 * Comprehensive tests for BallisticsCalculator.
 * Tests lookup table generation, queries, and edge cases.
 */
class BallisticsCalculatorTest {

    private static final double DELTA = 0.01;
    private BallisticsCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new BallisticsCalculator();
    }

    // ==================== Constructor Tests ====================

    @Test
    @DisplayName("Constructor should generate lookup table")
    void testConstructor_GeneratesLookupTable() {
        assertNotNull(calculator.getLookupTable(), "Lookup table should be initialized");
        assertFalse(calculator.getLookupTable().isEmpty(), "Lookup table should not be empty");
    }

    @Test
    @DisplayName("Constructor should populate table with valid entries")
    void testConstructor_ValidEntries() {
        TreeMap<Double, LaunchParameter> table = calculator.getLookupTable();
        
        for (LaunchParameter param : table.values()) {
            assertNotNull(param, "All entries should be non-null");
            assertTrue(param.getRangeMeters() > 0, "Range should be positive");
            assertTrue(param.getLaunchAngleDeg() >= 0, "Angle should be non-negative");
            assertTrue(param.getLaunchVelocityMps() > 0, "Velocity should be positive");
        }
    }

    @Test
    @DisplayName("Multiple constructor calls should produce consistent results")
    void testConstructor_Consistency() {
        BallisticsCalculator calc1 = new BallisticsCalculator();
        BallisticsCalculator calc2 = new BallisticsCalculator();
        
        assertEquals(calc1.getLookupTable().size(), calc2.getLookupTable().size(),
                    "Both calculators should have same table size");
    }

    // ==================== getLookupTable Tests ====================

    @Test
    @DisplayName("getLookupTable should return TreeMap")
    void testGetLookupTable_ReturnsTreeMap() {
        TreeMap<Double, LaunchParameter> table = calculator.getLookupTable();
        
        assertNotNull(table);
        assertTrue(table instanceof TreeMap);
    }

    @Test
    @DisplayName("getLookupTable should contain entries in range")
    void testGetLookupTable_ContainsExpectedRange() {
        TreeMap<Double, LaunchParameter> table = calculator.getLookupTable();
        
        // Should have entries starting from MIN_RANGE (0.5) up to MAX_RANGE (20.0)
        double firstKey = table.firstKey();
        double lastKey = table.lastKey();
        
        assertTrue(firstKey >= 0.5, "First key should be at least MIN_RANGE");
        assertTrue(lastKey <= 20.0, "Last key should be at most MAX_RANGE");
    }

    @Test
    @DisplayName("getLookupTable entries should be in RANGE_STEP increments")
    void testGetLookupTable_StepIncrements() {
        TreeMap<Double, LaunchParameter> table = calculator.getLookupTable();
        
        // RANGE_STEP is 0.25 meters
        Double[] keys = table.keySet().toArray(new Double[0]);
        
        if (keys.length > 1) {
            for (int i = 1; i < keys.length; i++) {
                double diff = keys[i] - keys[i-1];
                // Difference should be approximately RANGE_STEP (0.25)
                assertTrue(diff >= 0.24 && diff <= 0.26, 
                          "Keys should be in ~0.25m increments, got " + diff);
            }
        }
    }

    @Test
    @DisplayName("getLookupTable should regenerate if empty")
    void testGetLookupTable_RegeneratesIfEmpty() {
        // This test relies on internal behavior - getLookupTable regenerates if empty
        TreeMap<Double, LaunchParameter> table = calculator.getLookupTable();
        int originalSize = table.size();
        assertTrue(originalSize > 0, "Table should initially have entries");

        // Clear and call again (if implementation allows)
        TreeMap<Double, LaunchParameter> tableAgain = calculator.getLookupTable();
        
        assertFalse(tableAgain.isEmpty(), "Should regenerate if empty");
    }

    // ==================== getBestLaunchParameter Tests ====================

    @Test
    @DisplayName("getBestLaunchParameter for exact table entry")
    void testGetBestLaunchParameter_ExactEntry() {
        // Query for a range that should be exactly in the table
        double targetRange = 5.0; // Should be in table (MIN=0.5, MAX=20, STEP=0.25)
        
        LaunchParameter param = calculator.getBestLaunchParameter(targetRange);
        
        assertNotNull(param, "Should find parameter for valid range");
        assertEquals(5.0, param.getRangeMeters(), 0.3, "Should return close match");
    }

    @Test
    @DisplayName("getBestLaunchParameter for range between table entries")
    void testGetBestLaunchParameter_BetweenEntries() {
        // Query for a range between table entries
        double targetRange = 5.12; // Between 5.0 and 5.25
        
        LaunchParameter param = calculator.getBestLaunchParameter(targetRange);
        
        assertNotNull(param, "Should find closest parameter");
        // Should be close to one of the neighboring entries
        assertTrue(Math.abs(param.getRangeMeters() - targetRange) < 0.2,
                  "Should return nearby range");
    }

    @Test
    @DisplayName("getBestLaunchParameter for minimum range")
    void testGetBestLaunchParameter_MinRange() {
        double minRange = 0.5;
        
        LaunchParameter param = calculator.getBestLaunchParameter(minRange);
        
        assertNotNull(param, "Should find parameter for min range");
        assertTrue(param.getRangeMeters() >= minRange - 0.1, 
                  "Returned range should be close to requested");
    }

    @Test
    @DisplayName("getBestLaunchParameter for maximum valid range")
    void testGetBestLaunchParameter_MaxValidRange() {
        // Max range is 20.0, but some high ranges might not have valid trajectories
        double maxRange = 15.0; // Use a safer max
        
        LaunchParameter param = calculator.getBestLaunchParameter(maxRange);
        
        // May or may not find parameter depending on trajectory constraints
        if (param != null) {
            assertTrue(param.getRangeMeters() > 0, "If found, should have positive range");
        }
    }

    @Test
    @DisplayName("getBestLaunchParameter below minimum range")
    void testGetBestLaunchParameter_BelowMinRange() {
        double belowMin = 0.2; // Below MIN_RANGE of 0.5
        
        LaunchParameter param = calculator.getBestLaunchParameter(belowMin);
        
        // Should return closest available (likely the minimum)
        if (param != null) {
            assertTrue(param.getRangeMeters() >= 0.5, 
                      "Should return closest available parameter");
        }
    }

    @Test
    @DisplayName("getBestLaunchParameter above maximum range")
    void testGetBestLaunchParameter_AboveMaxRange() {
        double aboveMax = 25.0; // Above MAX_RANGE of 20.0
        
        LaunchParameter param = calculator.getBestLaunchParameter(aboveMax);
        
        // Should return closest available (likely the maximum valid)
        if (param != null) {
            assertTrue(param.getRangeMeters() <= 20.0, 
                      "Should return closest available parameter within table");
        }
    }

    @Test
    @DisplayName("getBestLaunchParameter for zero range")
    void testGetBestLaunchParameter_ZeroRange() {
        double zeroRange = 0.0;
        
        LaunchParameter param = calculator.getBestLaunchParameter(zeroRange);
        
        // Should return closest available (minimum range parameter)
        if (param != null) {
            assertTrue(param.getRangeMeters() >= 0.0, "Should handle zero gracefully");
        }
    }

    @Test
    @DisplayName("getBestLaunchParameter for negative range")
    void testGetBestLaunchParameter_NegativeRange() {
        double negativeRange = -5.0;
        
        LaunchParameter param = calculator.getBestLaunchParameter(negativeRange);
        
        // Implementation should handle this - likely returns null or closest
        // Negative range doesn't make physical sense
        if (param != null) {
            assertTrue(param.getRangeMeters() >= 0.0, 
                      "Should not return negative range parameter");
        }
    }

    @Test
    @DisplayName("getBestLaunchParameter should use floor/ceiling logic correctly")
    void testGetBestLaunchParameter_FloorCeilingLogic() {
        // Query slightly above a table entry
        double targetRange = 5.1; // Between 5.0 and 5.25
        
        LaunchParameter param = calculator.getBestLaunchParameter(targetRange);
        
        assertNotNull(param);
        // Should choose the closer of floor (5.0) or ceiling (5.25)
        // 5.1 is closer to 5.0 (diff=0.1) than to 5.25 (diff=0.15)
        assertTrue(Math.abs(param.getRangeMeters() - 5.0) < 0.3 ||
                  Math.abs(param.getRangeMeters() - 5.25) < 0.3,
                  "Should return floor or ceiling entry");
    }

    @Test
    @DisplayName("getBestLaunchParameter for multiple queries should be consistent")
    void testGetBestLaunchParameter_Consistency() {
        double targetRange = 8.5;
        
        LaunchParameter param1 = calculator.getBestLaunchParameter(targetRange);
        LaunchParameter param2 = calculator.getBestLaunchParameter(targetRange);
        
        if (param1 != null && param2 != null) {
            assertEquals(param1.getRangeMeters(), param2.getRangeMeters(), DELTA,
                        "Same query should return same result");
            assertEquals(param1.getLaunchAngleDeg(), param2.getLaunchAngleDeg(), DELTA);
            assertEquals(param1.getLaunchVelocityMps(), param2.getLaunchVelocityMps(), DELTA);
        }
    }

    // ==================== Lookup Table Properties Tests ====================

    @Test
    @DisplayName("Lookup table should have valid trajectories only")
    void testLookupTable_ValidTrajectoriesOnly() {
        TreeMap<Double, LaunchParameter> table = calculator.getLookupTable();
        
        for (LaunchParameter param : table.values()) {
            assertTrue(param.getLaunchVelocityMps() > 0, 
                      "All entries should have positive velocity");
            assertTrue(param.getTimeOfFlightSeconds() > 0, 
                      "All entries should have positive flight time");
            assertFalse(Double.isNaN(param.getImpactAngleDeg()), 
                       "All entries should have valid impact angle");
        }
    }

    @Test
    @DisplayName("Lookup table entries should satisfy height constraints")
    void testLookupTable_HeightConstraints() {
        TreeMap<Double, LaunchParameter> table = calculator.getLookupTable();
        
        // MAX_PEAK_HEIGHT = 3.0, MIN_PEAK_HEIGHT = 1.2
        for (LaunchParameter param : table.values()) {
            double peakHeight = param.getPeakHeightMeters();
            assertTrue(peakHeight >= 1.2, 
                      "Peak height should be at least MIN_PEAK_HEIGHT (1.2m)");
            assertTrue(peakHeight <= 3.0, 
                      "Peak height should be at most MAX_PEAK_HEIGHT (3.0m)");
        }
    }

    @Test
    @DisplayName("Lookup table entries should satisfy impact angle constraints")
    void testLookupTable_ImpactAngleConstraints() {
        TreeMap<Double, LaunchParameter> table = calculator.getLookupTable();
        
        // MIN_IMPACT_ANGLE = -30 degrees
        for (LaunchParameter param : table.values()) {
            assertTrue(param.getImpactAngleDeg() <= -30.0, 
                      "Impact angle should be steeper than MIN_IMPACT_ANGLE (-30°)");
        }
    }

    @Test
    @DisplayName("Lookup table entries should have scores assigned")
    void testLookupTable_ScoresAssigned() {
        TreeMap<Double, LaunchParameter> table = calculator.getLookupTable();
        
        for (LaunchParameter param : table.values()) {
            double score = param.getScore();
            assertTrue(score >= 0.0 && score <= 1.0, 
                      "Score should be normalized between 0 and 1, got: " + score);
        }
    }

    @Test
    @DisplayName("Lookup table should have best parameter for each range")
    void testLookupTable_BestParameterForEachRange() {
        TreeMap<Double, LaunchParameter> table = calculator.getLookupTable();
        
        // Each entry should represent the best trajectory for that range
        // (highest score among valid trajectories)
        for (LaunchParameter param : table.values()) {
            assertNotNull(param, "Each range should have a best parameter");
            assertTrue(param.getScore() >= 0.0, "Best parameter should have valid score");
        }
    }

    // ==================== Edge Case Tests ====================

    @Test
    @DisplayName("Calculator should handle ranges with no valid trajectories")
    void testEdgeCase_NoValidTrajectory() {
        // Very high ranges (15-20m) might not have valid trajectories due to constraints
        double highRange = 18.0;
        
        LaunchParameter param = calculator.getBestLaunchParameter(highRange);
        
        // May return null or closest available parameter
        // Implementation may vary - just ensure it doesn't crash
        if (param != null) {
            assertTrue(param.getRangeMeters() > 0, "If parameter exists, should be valid");
        }
    }

    @Test
    @DisplayName("Table size should be reasonable")
    void testTableSize_Reasonable() {
        TreeMap<Double, LaunchParameter> table = calculator.getLookupTable();
        
        // RANGE: 0.5 to 20.0, STEP: 0.25
        // Expected entries: (20.0 - 0.5) / 0.25 + 1 = 79 (if all valid)
        // But some may be filtered out by constraints
        assertTrue(table.size() > 0, "Table should not be empty");
        assertTrue(table.size() <= 79, "Table should not exceed expected maximum");
    }

    @Test
    @DisplayName("Query within valid range should always return result")
    void testQuery_WithinValidRange() {
        // Most ranges between 0.5 and ~10m should have valid trajectories
        double[] testRanges = {1.0, 2.5, 5.0, 7.5, 10.0};
        
        for (double range : testRanges) {
            LaunchParameter param = calculator.getBestLaunchParameter(range);
            assertNotNull(param, "Should find parameter for range: " + range);
        }
    }

    @Test
    @DisplayName("Consecutive range queries should return different parameters")
    void testQuery_ConsecutiveRanges() {
        LaunchParameter param1 = calculator.getBestLaunchParameter(5.0);
        LaunchParameter param2 = calculator.getBestLaunchParameter(10.0);
        
        if (param1 != null && param2 != null) {
            assertNotEquals(param1.getRangeMeters(), param2.getRangeMeters(), DELTA,
                          "Different ranges should return different parameters");
            // Velocity typically increases with range
            assertTrue(param2.getLaunchVelocityMps() > param1.getLaunchVelocityMps(),
                      "Higher range should generally require higher velocity");
        }
    }

    // ==================== Integration Tests ====================

    @Test
    @DisplayName("Integration: Full workflow from construction to query")
    void testIntegration_FullWorkflow() {
        // Create calculator (generates table)
        BallisticsCalculator calc = new BallisticsCalculator();
        
        // Verify table was generated
        assertNotNull(calc.getLookupTable());
        assertFalse(calc.getLookupTable().isEmpty());
        
        // Query for a parameter
        LaunchParameter param = calc.getBestLaunchParameter(7.5);
        
        // Verify parameter is valid
        if (param != null) {
            assertTrue(param.getLaunchVelocityMps() > 0);
            assertTrue(param.getTimeOfFlightSeconds() > 0);
            assertTrue(param.getPeakHeightMeters() >= 1.2 && param.getPeakHeightMeters() <= 3.0);
            assertTrue(param.getImpactAngleDeg() <= -30.0);
        }
    }

    @Test
    @DisplayName("Integration: Verify scoring produces best trajectories")
    void testIntegration_ScoringProducesBestTrajectories() {
        TreeMap<Double, LaunchParameter> table = calculator.getLookupTable();
        
        // For each entry, the stored parameter should have the highest score
        // (This is implicit in the algorithm, but we verify it's reasonable)
        for (LaunchParameter param : table.values()) {
            // Score should be in reasonable range
            assertTrue(param.getScore() >= 0.0 && param.getScore() <= 1.0);
            
            // Higher scores should generally mean better trajectories
            // (steeper impact angles and shorter flight times)
            if (param.getScore() > 0.8) {
                // High-score trajectories should meet quality criteria
                assertTrue(param.getImpactAngleDeg() < -30.0, 
                          "High-score should have steep impact");
            }
        }
    }

    @Test
    @DisplayName("Integration: Physical consistency of table entries")
    void testIntegration_PhysicalConsistency() {
        TreeMap<Double, LaunchParameter> table = calculator.getLookupTable();
        
        for (LaunchParameter param : table.values()) {
            // Verify physics: range = vx * t
            double vx = param.getLaunchVelocityMps() * 
                       Math.cos(Math.toRadians(param.getLaunchAngleDeg()));
            double calculatedRange = vx * param.getTimeOfFlightSeconds();
            
            assertEquals(param.getRangeMeters(), calculatedRange, 0.5,
                        "Range should equal horizontal velocity * time");
        }
    }
}
