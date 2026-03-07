package ballisticslookuptable;
/** 
 * 
 * 
 * WARNING!!!! This is a work in progress
 * 
 * 
 * 
 * 
 * 
 * 
 * WARNING!!!! This is a work in progress
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * WARNING!!!! This is a work in progress
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * WARNING!!!! This is a work in progress
 * 
 * 
 */
/**
 * Handles iterative predictive aiming for moving targets using a ballistics lookup table.
 * 
 * This class uses an iterative approach to predict where a moving target will be when
 * a projectile reaches it. It converges on an accurate prediction by:
 * 1. Predicting target position based on velocities
 * 2. Using the lookup table to get the required time of flight for that distance
 * 3. Re-predicting with the new time of flight value
 * 4. Repeating until convergence
 * 
 * ref: https://gamedev.stackexchange.com/questions/28481/how-to-lead-a-moving-target-from-a-moving-shooter
 */
public class IterativeAimingCalculator {
    
    private final BallisticsCalculator ballisticsCalculator;
    
    /**
     * Creates an IterativeAimingCalculator with a ballistics lookup table.
     * @param ballisticsCalculator Calculator containing the lookup table for time-of-flight queries
     */
    public IterativeAimingCalculator(BallisticsCalculator ballisticsCalculator) {
        if (ballisticsCalculator == null) {
            throw new NullPointerException("BallisticsCalculator cannot be null");
        }
        this.ballisticsCalculator = ballisticsCalculator;
    }
    
    /**
     * Record type to hold predicted target coordinates.
     */
    public record Coordinate( double x, double y) {}
    
    /**
     * Predicts where a moving target will be at a given time in the future,
     * accounting for the robot's own motion.
     * 
     * @param targetVelocityX Target velocity in X direction (m/s)
     * @param targetVelocityY Target velocity in Y direction (m/s)
     * @param targetPositionX Target X position (m)
     * @param targetPositionY Target Y position (m)
     * @param robotVelocityX Robot velocity in X direction (m/s)
     * @param robotVelocityY Robot velocity in Y direction (m/s)
     * @param robotPositionX Robot X position (m)
     * @param robotPositionY Robot Y position (m)
     * @param timeOfFlightSeconds Time of flight (seconds)
     * @return Predicted target coordinates relative to robot
    * Time complexity: O(1).
     */
    public Coordinate predictTarget(
        double targetVelocityX, double targetVelocityY,
        double targetPositionX, double targetPositionY,
        double robotVelocityX, double robotVelocityY,
        double robotPositionX, double robotPositionY,
        double timeOfFlightSeconds
    ) {
        if (timeOfFlightSeconds < 0) {
            throw new IllegalArgumentException("timeOfFlightSeconds must be >= 0");
        }

        double distanceX = targetPositionX - robotPositionX;
        double distanceY = targetPositionY - robotPositionY;
        
        double velocityX = targetVelocityX - robotVelocityX;
        double velocityY = targetVelocityY - robotVelocityY;
        
        double virtualX = distanceX + (velocityX * timeOfFlightSeconds);
        double virtualY = distanceY + (velocityY * timeOfFlightSeconds);
        
        return new Coordinate(virtualX, virtualY);
    }

    /**
     * Calulates the predicted coordinate of the target using iterative prediction and the
     * ballistics lookup table.
     * 
     * The algorithm:
     * 1. Predicts where the target will be based on initial time of flight
     * 2. Looks up the required time of flight for that distance using the lookup table
     * 3. Re-predicts with the new time of flight value
     * 4. Repeats until convergence or maximum iterations reached
     * 
     * This approach converges better than a single prediction because the lookup table
     * respects all the ballistics constraints (velocity limits, angle limits, etc.)
     * 
     * @param targetVelocityX Target velocity in X direction (m/s)
     * @param targetVelocityY Target velocity in Y direction (m/s)
     * @param targetPositionX Target X position (m)
     * @param targetPositionY Target Y position (m)
     * @param robotVelocityX Robot velocity in X direction (m/s)
     * @param robotVelocityY Robot velocity in Y direction (m/s)
     * @param robotPositionX Robot X position (m)
     * @param robotPositionY Robot Y position (m)
     * @param initialTimeOfFlightSeconds Initial time of flight estimate (seconds)
     * @param maxIterations Maximum number of iterations for convergence
     * @return Predicted target coordinates relative to robot
    * Time complexity: O(I * log N).
     */
    public Coordinate interativePredictCoordinate(
        double targetVelocityX, double targetVelocityY,
        double targetPositionX, double targetPositionY,
        double robotVelocityX, double robotVelocityY,
        double robotPositionX, double robotPositionY,
        double initialTimeOfFlightSeconds,
        int maxIterations
    ){
        double timeOfFlight = initialTimeOfFlightSeconds;
        Coordinate predictedTarget = null;
        double previousDistance = Double.MAX_VALUE;
        
        for (int iteration = 0; iteration < maxIterations; iteration++) {
            // Predict where the target will be at the current time of flight
            predictedTarget = predictTarget(
                targetVelocityX, targetVelocityY,
                targetPositionX, targetPositionY,
                robotVelocityX, robotVelocityY,
                robotPositionX, robotPositionY,
                timeOfFlight
            );
            
            // Calculate distance from robot to predicted target
            double distanceToPredictedTarget = Math.sqrt(
                Math.pow(predictedTarget.x, 2) + 
                Math.pow(predictedTarget.y, 2)
            );

            double minSupportedRange = ballisticsCalculator.getLookupTable().firstKey();
            double maxSupportedRange = ballisticsCalculator.getLookupTable().lastKey();
            double supportedRangeTolerance = 0.0;
            if (ballisticsCalculator.getLookupTable().size() > 1) {
                Double secondKey = ballisticsCalculator.getLookupTable().higherKey(minSupportedRange);
                if (secondKey != null) {
                    supportedRangeTolerance = Math.abs(secondKey - minSupportedRange);
                }
            }

            if (distanceToPredictedTarget < minSupportedRange - supportedRangeTolerance ||
                distanceToPredictedTarget > maxSupportedRange + supportedRangeTolerance) {
                throw new IllegalArgumentException(
                    "No valid trajectory found for predicted range: " + distanceToPredictedTarget +
                    " meters (supported range: " + minSupportedRange + " to " + maxSupportedRange + " m)"
                );
            }
            
            // Check for convergence (distance changes less than 1cm between iterations)
            if (Math.abs(distanceToPredictedTarget - previousDistance) < 0.01) {
                break;
            }
            previousDistance = distanceToPredictedTarget;
            
            // Use lookup table to get better time of flight estimate for this distance
            LaunchParameter bestParam = ballisticsCalculator.getBestLaunchParameter(distanceToPredictedTarget);
            
            if (bestParam == null) {
                throw new IllegalArgumentException(
                    "No valid trajectory found for predicted range: " + distanceToPredictedTarget + " meters"
                );
            }
            
            // Update time of flight for next iteration
            timeOfFlight = bestParam.getTimeOfFlightSeconds();
        }
        
        if (predictedTarget == null) {
            throw new IllegalStateException("No target prediction calculated");
        }
        return predictedTarget;
    }
    
    /**
     * Calculates the angle to aim at a moving target using iterative prediction.
     * 
     * The algorithm:
     * 1. Predicts where the target will be based on initial time of flight
     * 2. Looks up the required time of flight for that distance using the lookup table
     * 3. Re-predicts with the new time of flight value
     * 4. Repeats until convergence or maximum iterations reached
     * 
     * This approach converges better than a single prediction because the lookup table
     * respects all the ballistics constraints (velocity limits, angle limits, etc.)
     * 
     * @param targetVelocityX Target velocity in X direction (m/s)
     * @param targetVelocityY Target velocity in Y direction (m/s)
     * @param targetPositionX Target X position (m)
     * @param targetPositionY Target Y position (m)
     * @param robotVelocityX Robot velocity in X direction (m/s)
     * @param robotVelocityY Robot velocity in Y direction (m/s)
     * @param robotPositionX Robot X position (m)
     * @param robotPositionY Robot Y position (m)
     * @param initialTimeOfFlightSeconds Initial time of flight estimate (seconds)
     * @param maxIterations Maximum number of iterations for convergence
     * @return Aiming angle in degrees (from +X axis)
     * @throws IllegalArgumentException if no valid trajectory found for predicted range
    * Time complexity: O(I * log N).
     */
    public double iterativePredictiveAim(
        double targetVelocityX, double targetVelocityY,
        double targetPositionX, double targetPositionY,
        double robotVelocityX, double robotVelocityY,
        double robotPositionX, double robotPositionY,
        double initialTimeOfFlightSeconds,
        int maxIterations
    ) {
        double timeOfFlight = initialTimeOfFlightSeconds;
        Coordinate predictedTarget = null;
        double previousDistance = Double.MAX_VALUE;
        
        for (int iteration = 0; iteration < maxIterations; iteration++) {
            // Predict where the target will be at the current time of flight
            predictedTarget = predictTarget(
                targetVelocityX, targetVelocityY,
                targetPositionX, targetPositionY,
                robotVelocityX, robotVelocityY,
                robotPositionX, robotPositionY,
                timeOfFlight
            );
            
            // Calculate distance from robot to predicted target
            double distanceToPredictedTarget = Math.sqrt(
                Math.pow(predictedTarget.x, 2) + 
                Math.pow(predictedTarget.y, 2)
            );

            double minSupportedRange = ballisticsCalculator.getLookupTable().firstKey();
            double maxSupportedRange = ballisticsCalculator.getLookupTable().lastKey();
            double supportedRangeTolerance = 0.0;
            if (ballisticsCalculator.getLookupTable().size() > 1) {
                Double secondKey = ballisticsCalculator.getLookupTable().higherKey(minSupportedRange);
                if (secondKey != null) {
                    supportedRangeTolerance = Math.abs(secondKey - minSupportedRange);
                }
            }

            if (distanceToPredictedTarget < minSupportedRange - supportedRangeTolerance ||
                distanceToPredictedTarget > maxSupportedRange + supportedRangeTolerance) {
                throw new IllegalArgumentException(
                    "No valid trajectory found for predicted range: " + distanceToPredictedTarget +
                    " meters (supported range: " + minSupportedRange + " to " + maxSupportedRange + " m)"
                );
            }
            
            // Check for convergence (distance changes less than 1cm between iterations)
            if (Math.abs(distanceToPredictedTarget - previousDistance) < 0.01) {
                break;
            }
            previousDistance = distanceToPredictedTarget;
            
            // Use lookup table to get better time of flight estimate for this distance
            LaunchParameter bestParam = ballisticsCalculator.getBestLaunchParameter(distanceToPredictedTarget);
            
            if (bestParam == null) {
                throw new IllegalArgumentException(
                    "No valid trajectory found for predicted range: " + distanceToPredictedTarget + " meters"
                );
            }
            
            // Update time of flight for next iteration
            timeOfFlight = bestParam.getTimeOfFlightSeconds();
        }
        
        if (predictedTarget == null) {
            throw new IllegalStateException("No target prediction calculated");
        }
        
        // Calculate angle to the final predicted target position
        double headingToVirtualTarget = Math.atan2(predictedTarget.y, predictedTarget.x);
        return Math.toDegrees(headingToVirtualTarget);
    }
    
    /**
     * Simplified variant that uses a default number of iterations.
     * @see #iterativePredictiveAim(double, double, double, double, double, double, double, double, double, int)
        * Time complexity: O(log N) with a constant default iteration count.
     */
    public double iterativePredictiveAim(
        double targetVelocityX, double targetVelocityY,
        double targetPositionX, double targetPositionY,
        double robotVelocityX, double robotVelocityY,
        double robotPositionX, double robotPositionY,
        double initialTimeOfFlightSeconds
    ) {
        return iterativePredictiveAim(
            targetVelocityX, targetVelocityY,
            targetPositionX, targetPositionY,
            robotVelocityX, robotVelocityY,
            robotPositionX, robotPositionY,
            initialTimeOfFlightSeconds,
            10  // Default to 10 iterations for convergence
        );
    }
}
 