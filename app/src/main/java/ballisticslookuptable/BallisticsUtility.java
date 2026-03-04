package ballisticslookuptable;

public class BallisticsUtility {
    private static final double GRAVITY = 9.81;

    /**
     * Calculates the time of flight using the horizontal component of velocity, assuming no air resistance and level ground.
     * @param rangeMeters The horizontal distance to the target (meters).
     * @param targetElevationMeters The vertical distance to the target (meters).
     * @param launchAngleDeg The angle at which the projectile is launched, in degrees.
     * @param launchVelocityMps The initial velocity of the projectile (m/s).
     * @return The time of flight of the projectile.
     */
    public static double calculateFlightTimeFromRange(double rangeMeters, double targetElevationMeters, double launchAngleDeg, double launchVelocityMps) {
        // Calculate time from horizontal motion (always accurate)
        double vx = launchVelocityMps * Math.cos(Math.toRadians(launchAngleDeg));
        double timeOfFlight = rangeMeters / vx;
        
        // Verify vertical position matches target
        double vy = launchVelocityMps * Math.sin(Math.toRadians(launchAngleDeg));
        double actualElevation = (vy * timeOfFlight) - (0.5 * GRAVITY * timeOfFlight * timeOfFlight);
        
        // Allow small tolerance for floating-point errors (1cm)
        if (Math.abs(actualElevation - targetElevationMeters) > 0.01) {
            return -1;  // Trajectory misses target vertically
        }
        
        return timeOfFlight;
    }

    /**
     * Calculates the time of flight using the vertical component of velocity, considering the initial height and the effect of gravity.  
     * @param launchAngleDeg The angle at which the projectile is launched, in degrees.
     * @param launchVelocityMps The initial velocity of the projectile (m/s).
     * @param initialHeightMeters The initial height of the launcher (meters).
     * @return The time of flight of the projectile.
     */
    public static double calculateFlightTimeFromVerticalMotion(double launchAngleDeg, double launchVelocityMps, double initialHeightMeters) {
        double vy = launchVelocityMps * Math.sin(Math.toRadians(launchAngleDeg));
        double timeToPeak = vy / GRAVITY;
        double maxHeight = initialHeightMeters + (vy * timeToPeak) - (0.5 * GRAVITY * Math.pow(timeToPeak, 2));
        if (maxHeight < 0) {
            return Double.NaN; // Target is below the launch point
        }
        double timeToFall = Math.sqrt((2 * maxHeight) / GRAVITY);
        return timeToPeak + timeToFall;
    }


    /**
     * Calculates the time of flight using the quadratic formula, considering both vertical and horizontal components.
     * @param elevationMeters The displacement between the launch point and the target (meters).
     * @param launchVelocityMps The initial velocity of the projectile (m/s).
     * @param launchAngleDeg The angle at which the projectile is launched, in degrees.
     * @return The time of flight of the projectile.
     */
    public static double calculateFlightTimeFromElevation(double elevationMeters, double launchVelocityMps, double launchAngleDeg) {
        // Vertical component of velocity
        double vy = launchVelocityMps * Math.sin(Math.toRadians(launchAngleDeg));

        // Using kinematic equation: y = vy*t - 0.5*g*t^2
        // Rearranged: 0.5*g*t^2 - vy*t + elevation = 0
        // Solve using quadratic formula

        double a = 0.5 * GRAVITY;
        double b = -vy;
        double c = elevationMeters;

        double discriminant = b * b - 4 * a * c;

        if(discriminant < 0) {
        return -1; // No solution (shouldn't happen with valid trajectory)
        }

        // Two solutions - we want the positive one (forward in time)
        double t1 = (-b + Math.sqrt(discriminant)) / (2 * a);
        double t2 = (-b - Math.sqrt(discriminant)) / (2 * a);

        // Return the positive solution (or larger if both positive)
        return Math.max(t1, t2);
    }


    /**
     * Calculates the angle of the projectile's velocity vector when it reaches the target.
     * This is the impact angle or arrival angle.
     * 
     * @param rangeMeters Horizontal distance to target (meters)
     * @param elevationMeters Vertical distance to target (meters, positive = above launcher)
     * @param launchVelocityMps Launch velocity (m/s)
     * @param launchAngleDeg Launch angle (degrees from horizontal)
     * @return Angle at target in radians (negative = downward), or NaN if trajectory is invalid
     */
    public static double calculateImpactAngleAtTarget(double rangeMeters, double elevationMeters, double launchVelocityMps, double launchAngleDeg) {
        // Calculate time of flight
        double timeOfFlight = calculateFlightTimeFromElevation(elevationMeters, launchVelocityMps, launchAngleDeg);

        if(timeOfFlight < 0) {
            return Double.NaN; // Invalid trajectory
        }

        // Horizontal velocity component (constant throughout flight)
        double vx = launchVelocityMps * Math.cos(Math.toRadians(launchAngleDeg));

        // Vertical velocity component at launch
        double vy0 = launchVelocityMps * Math.sin(Math.toRadians(launchAngleDeg));

        // Vertical velocity at target (affected by gravity)
        // vy = vy0 - g*t
        double vyAtTarget = vy0 - GRAVITY * timeOfFlight;

        // Angle of velocity vector at target
        // tan(angle) = vy / vx
        return Math.toDegrees(Math.atan2(vyAtTarget, vx));
    }

    /**
     * Calculates the apex (maximum height) of the projectile trajectory.
     * 
     * @param launchVelocityMps Launch velocity (m/s)
     * @param launchAngleDeg Launch angle (degrees from horizontal)
     * @param launcherHeightMeters Height of launcher above ground (meters)
     * @return Maximum height above ground in meters
     */
    public static double calculatePeakHeight(double launchVelocityMps, double launchAngleDeg, double launcherHeightMeters) {
        // Vertical velocity component
        double vy = launchVelocityMps * Math.sin(Math.toRadians(launchAngleDeg));

        // At apex, vertical velocity = 0
        // Using v^2 = v0^2 - 2*g*h
        // Height above launcher: h = vy^2 / (2*g)
        double heightAboveLauncher = (vy * vy) / (2 * GRAVITY);

        return launcherHeightMeters + heightAboveLauncher;
    }


    /**
     * Calculates the required launch velocity given a launch angle.
     * 
     * @param rangeMeters Horizontal distance to target (meters)
     * @param elevationMeters Vertical distance to target (meters, positive = above launcher)
     * @param launchAngleDeg Launch angle in degrees (from horizontal)
     * @return Required launch velocity in m/s, or -1 if impossible
     */
    public static double calculateLaunchVelocityForRange(double rangeMeters, double elevationMeters, double launchAngleDeg) {
        double cosAngle = Math.cos(Math.toRadians(launchAngleDeg));
        double tanAngle = Math.tan(Math.toRadians(launchAngleDeg));

        // Derived from kinematic equations:
        // v^2 = (g * range^2) / (2 * cos^2(θ) * (range * tan(θ) - elevation))
        double denominator = 2 * cosAngle * cosAngle * (rangeMeters * tanAngle - elevationMeters);

        if(denominator <= 0) {
            return -1; // Impossible trajectory
        }

        double velocitySquared = (GRAVITY * rangeMeters * rangeMeters) / denominator;

        if(velocitySquared < 0) {
            return -1; // Impossible trajectory
        }

        return Math.sqrt(velocitySquared);
    }

    // /**
    //  * Predictive aiming calculation to determine the angle to aim at a moving target, given the target's 
    //  * velocity and position, the robot's velocity and position, and the time of flight of the projectile.
    //  * @param targetVelocityX Velocity of the target in the X direction (m/s)
    //  * @param targetVelocityY Velocity of the target in the Y direction (m/s)
    //  * @param targetPositionX X position of the target (m)
    //  * @param targetPositionY Y position of the target (m)
    //  * @param robotVelocityX Velocity of the robot in the X direction (m/s)
    //  * @param robotVelocityY Velocity of the robot in the Y direction (m/s)
    //  * @param robotPositionX X position of the robot (m)
    //  * @param robotPositionY Y position of the robot (m)
    //  * @param timeOfFlightSeconds Time of flight of the projectile (s)
    //  * @return Angle to aim at the moving target (degrees)
    //  */
    // public static double predictiveAim(
    //     double targetVelocityX, double targetVelocityY,
    //     double targetPositionX, double targetPositionY,
    //     double robotVelocityX, double robotVelocityY,
    //     double robotPositionX, double robotPositionY,
    //     double timeOfFlightSeconds
    // ) {
    //     Double distanceX = targetPositionX - robotPositionX;
    //     Double distanceY = targetPositionY - robotPositionY;
        
    //     Double velocityX = targetVelocityX - robotVelocityX;
    //     Double velocityY = targetVelocityY - robotVelocityY;

    //     Double virtualX = velocityX + distanceX / timeOfFlightSeconds;
    //     Double virtualY = velocityY + distanceY / timeOfFlightSeconds;

    //     // calculate angle to virtual target
    //     Double headingToVirtualTarget = Math.atan2(virtualY, virtualX);
    //     headingToVirtualTarget = Math.toDegrees(headingToVirtualTarget);
    //     return headingToVirtualTarget;
    // }

    // public record Coordinate(double x, double y) {};

    // public static Coordinate predictTarget(
    //     double targetVelocityX, double targetVelocityY,
    //     double targetPositionX, double targetPositionY,
    //     double robotVelocityX, double robotVelocityY,
    //     double robotPositionX, double robotPositionY,
    //     double timeOfFlightSeconds
    // ) {
    //     Double distanceX = targetPositionX - robotPositionX;
    //     Double distanceY = targetPositionY - robotPositionY;
        
    //     Double velocityX = targetVelocityX - robotVelocityX;
    //     Double velocityY = targetVelocityY - robotVelocityY;

    //     Double virtualX = velocityX + distanceX / timeOfFlightSeconds;
    //     Double virtualY = velocityY + distanceY / timeOfFlightSeconds;
    //     return new Coordinate(virtualX, virtualY);
    // }

    // /**
    //  * Iterative predictive aiming calculation using hardcoded physics parameters.
    //  * 
    //  * @deprecated Use {@link IterativeAimingCalculator} instead. This static method uses hardcoded
    //  * angle and velocity values for time-of-flight calculations. The new class uses a ballistics
    //  * lookup table that respects actual system constraints and converges more accurately.
    //  * 
    //  * @param targetVelocityX Velocity of the target in the X direction (m/s)
    //  * @param targetVelocityY Velocity of the target in the Y direction (m/s)
    //  * @param targetPositionX X position of the target (m)
    //  * @param targetPositionY Y position of the target (m)
    //  * @param robotVelocityX Velocity of the robot in the X direction (m/s)
    //  * @param robotVelocityY Velocity of the robot in the Y direction (m/s)
    //  * @param robotPositionX X position of the robot (m)
    //  * @param robotPositionY Y position of the robot (m)
    //  * @param initialTimeOfFlightSeconds Time of flight of the projectile (s)
    //  * @param iterations Number of iterations for convergence
    //  * @return Angle to aim at the moving target (degrees)
    //  */
    // @Deprecated(since = "2.0", forRemoval = true)
    // public static double interativePredictiveAim(
    //     double targetVelocityX, double targetVelocityY,
    //     double targetPositionX, double targetPositionY,
    //     double robotVelocityX, double robotVelocityY,
    //     double robotPositionX, double robotPositionY,
    //     double initialTimeOfFlightSeconds,
    //     int iterations
    // ) {
    //     double timeOfFlight = initialTimeOfFlightSeconds;
    //     Coordinate predictedTarget = null;
    //     for (int i = 0; i < iterations; i++) {
    //         predictedTarget = predictTarget(
    //             targetVelocityX, targetVelocityY,
    //             targetPositionX, targetPositionY,
    //             robotVelocityX, robotVelocityY,
    //             robotPositionX, robotPositionY,
    //             timeOfFlight
    //         );
    //         double distanceToPredictedTarget = Math.sqrt(Math.pow(predictedTarget.x - robotPositionX, 2) + Math.pow(predictedTarget.y - robotPositionY, 2));
    //         // Hardcoded parameters - this is why this method is deprecated
    //         // Use IterativeAimingCalculator instead for lookup table based calculations
    //         timeOfFlight = calculateFlightTimeFromRange(distanceToPredictedTarget, 0, 45, 10);
    //     }
    //     // calculate angle to virtual target
    //     Double headingToVirtualTarget = Math.atan2(predictedTarget.y, predictedTarget.x);
    //     headingToVirtualTarget = Math.toDegrees(headingToVirtualTarget);
    //     return headingToVirtualTarget;
    // }   

}