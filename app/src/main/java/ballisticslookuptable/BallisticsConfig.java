package ballisticslookuptable;

import java.util.Optional;

/**
 * Configuration class for BallisticsCalculator.
 * Supports setter chaining (builder pattern) for convenient configuration.
 * 
 * Example usage:
 * <pre>
 * BallisticsConfig config = new BallisticsConfig()
 *     .setMaxPeakHeight(4.0)
 *     .setMinPeakHeight(1.5)
 *     .setImpactAngleWeight(0.8);
 * BallisticsCalculator calculator = new BallisticsCalculator(config);
 * </pre>
 */
public class BallisticsConfig {
    
    // Height constraints (meters)
    private double maxPeakHeight = 3.0;
    private double minPeakHeight = 1.2;
    
    // Impact angle constraint (degrees, negative = downward)
    private double minImpactAngle = -30.0;
    
    // Range parameters (meters)
    private double minRange = 0.5;
    private double maxRange = 20.0;
    private double rangeStep = 0.25;
    
    // Angle sweep parameters (degrees)
    private double angleStep = 1.0;
    private double minLaunchAngleDeg = 20.0;
    private double maxLaunchAngleDeg = 85.0;
    
    // Target elevation (meters)
    private double targetElevationMeters = 1.0;
    
    // Velocity constraints (m/s)
    private double maxLaunchVelocityMps = 30.0;
    private double minLaunchVelocityMps = 5.0;
    
    // Scoring weights (should sum to 1.0)
    private double impactAngleWeight = 0.75;
    private double timeOfFlightWeight = 0.25;
    
    /**
     * Default constructor with standard configuration values.
     */
    public BallisticsConfig() {
        // Uses default values defined above
    }
    
    // Getters
    
    public double getMaxPeakHeight() {
        return maxPeakHeight;
    }
    
    public double getMinPeakHeight() {
        return minPeakHeight;
    }
    
    public double getMinImpactAngle() {
        return minImpactAngle;
    }
    
    public double getMinRange() {
        return minRange;
    }
    
    public double getMaxRange() {
        return maxRange;
    }
    
    public double getRangeStep() {
        return rangeStep;
    }
    
    public double getAngleStep() {
        return angleStep;
    }
    
    public double getMinLaunchAngleDeg() {
        return minLaunchAngleDeg;
    }
    
    public double getMaxLaunchAngleDeg() {
        return maxLaunchAngleDeg;
    }
    
    public double getTargetElevationMeters() {
        return targetElevationMeters;
    }
    
    public double getMaxLaunchVelocityMps() {
        return maxLaunchVelocityMps;
    }
    
    public double getMinLaunchVelocityMps() {
        return minLaunchVelocityMps;
    }
    
    public double getImpactAngleWeight() {
        return impactAngleWeight;
    }
    
    public double getTimeOfFlightWeight() {
        return timeOfFlightWeight;
    }
    
    // Setters with chaining support
    
    /**
     * Sets the maximum allowed peak height for trajectories.
     * @param maxPeakHeight Maximum peak height in meters
     * @return This config instance for chaining
     */
    public BallisticsConfig setMaxPeakHeight(double maxPeakHeight) {
        this.maxPeakHeight = maxPeakHeight;
        return this;
    }
    
    /**
     * Sets the minimum allowed peak height for trajectories.
     * @param minPeakHeight Minimum peak height in meters
     * @return This config instance for chaining
     */
    public BallisticsConfig setMinPeakHeight(double minPeakHeight) {
        this.minPeakHeight = minPeakHeight;
        return this;
    }
    
    /**
     * Sets the minimum impact angle constraint (must be negative for downward).
     * @param minImpactAngle Minimum impact angle in degrees (e.g., -30)
     * @return This config instance for chaining
     */
    public BallisticsConfig setMinImpactAngle(double minImpactAngle) {
        this.minImpactAngle = minImpactAngle;
        return this;
    }
    
    /**
     * Sets the minimum range for lookup table generation.
     * @param minRange Minimum range in meters
     * @return This config instance for chaining
     */
    public BallisticsConfig setMinRange(double minRange) {
        this.minRange = minRange;
        return this;
    }
    
    /**
     * Sets the maximum range for lookup table generation.
     * @param maxRange Maximum range in meters
     * @return This config instance for chaining
     */
    public BallisticsConfig setMaxRange(double maxRange) {
        this.maxRange = maxRange;
        return this;
    }
    
    /**
     * Sets the step size for range increments in the lookup table.
     * @param rangeStep Range step in meters
     * @return This config instance for chaining
     */
    public BallisticsConfig setRangeStep(double rangeStep) {
        this.rangeStep = rangeStep;
        return this;
    }
    
    /**
     * Sets the step size for angle sweep when testing trajectories.
     * @param angleStep Angle step in degrees
     * @return This config instance for chaining
     */
    public BallisticsConfig setAngleStep(double angleStep) {
        this.angleStep = angleStep;
        return this;
    }
    
    /**
     * Sets the minimum launch angle for the shooter mechanism.
     * @param minLaunchAngleDeg Minimum launch angle in degrees
     * @return This config instance for chaining
     */
    public BallisticsConfig setMinLaunchAngleDeg(double minLaunchAngleDeg) {
        this.minLaunchAngleDeg = minLaunchAngleDeg;
        return this;
    }
    
    /**
     * Sets the maximum launch angle for the shooter mechanism.
     * @param maxLaunchAngleDeg Maximum launch angle in degrees
     * @return This config instance for chaining
     */
    public BallisticsConfig setMaxLaunchAngleDeg(double maxLaunchAngleDeg) {
        this.maxLaunchAngleDeg = maxLaunchAngleDeg;
        return this;
    }
    
    /**
     * Sets the target elevation above launcher.
     * @param targetElevationMeters Target elevation in meters
     * @return This config instance for chaining
     */
    public BallisticsConfig setTargetElevationMeters(double targetElevationMeters) {
        this.targetElevationMeters = targetElevationMeters;
        return this;
    }
    
    /**
     * Sets the maximum launch velocity constraint for the shooter mechanism.
     * @param maxLaunchVelocityMps Maximum launch velocity in m/s
     * @return This config instance for chaining
     */
    public BallisticsConfig setMaxLaunchVelocityMps(double maxLaunchVelocityMps) {
        this.maxLaunchVelocityMps = maxLaunchVelocityMps;
        return this;
    }
    
    /**
     * Sets the minimum launch velocity constraint for the shooter mechanism.
     * @param minLaunchVelocityMps Minimum launch velocity in m/s
     * @return This config instance for chaining
     */
    public BallisticsConfig setMinLaunchVelocityMps(double minLaunchVelocityMps) {
        this.minLaunchVelocityMps = minLaunchVelocityMps;
        return this;
    }
    
    /**
     * Sets the weight for impact angle in trajectory scoring.
     * Should be between 0 and 1, and impactAngleWeight + timeOfFlightWeight should equal 1.
     * @param impactAngleWeight Weight for impact angle (typically 0.75)
     * @return This config instance for chaining
     */
    public BallisticsConfig setImpactAngleWeight(double impactAngleWeight) {
        this.impactAngleWeight = impactAngleWeight;
        return this;
    }
    
    /**
     * Sets the weight for time of flight in trajectory scoring.
     * Should be between 0 and 1, and impactAngleWeight + timeOfFlightWeight should equal 1.
     * @param timeOfFlightWeight Weight for time of flight (typically 0.25)
     * @return This config instance for chaining
     */
    public BallisticsConfig setTimeOfFlightWeight(double timeOfFlightWeight) {
        this.timeOfFlightWeight = timeOfFlightWeight;
        return this;
    }
    
    /**
     * Validates that the configuration values are reasonable.
     * @return Optional error message if configuration is invalid
     */
    public Optional<String> validate() {
        if (minRange >= maxRange) {
            return Optional.of("minRange must be less than maxRange");
        }
        if (rangeStep <= 0) {
            return Optional.of("rangeStep must be positive");
        }
        if (angleStep <= 0) {
            return Optional.of("angleStep must be positive");
        }
        if (minPeakHeight >= maxPeakHeight) {
            return Optional.of("minPeakHeight must be less than maxPeakHeight");
        }
        if (minLaunchAngleDeg >= maxLaunchAngleDeg) {
            return Optional.of("minLaunchAngleDeg must be less than maxLaunchAngleDeg");
        }
        if (minLaunchVelocityMps >= maxLaunchVelocityMps) {
            return Optional.of("minLaunchVelocityMps must be less than maxLaunchVelocityMps");
        }
        if (Math.abs(impactAngleWeight + timeOfFlightWeight - 1.0) > 0.01) {
            return Optional.of("impactAngleWeight and timeOfFlightWeight must sum to 1.0");
        }
        return Optional.empty();
    }
}
