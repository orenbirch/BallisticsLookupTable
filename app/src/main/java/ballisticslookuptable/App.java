package ballisticslookuptable;

public class App {

  public String getGreeting() {
    return "Ballistics Lookup Table Demo: each output line shows a target range (meters) and the best launch settings; higher score means a better trajectory for that range.";
  }

  public static void main(String[] args) {
    System.out.println(new App().getGreeting());

    // create a configuration for the calculator
    BallisticsConfig config = new BallisticsConfig()
        // simulation parameters
        .setMinRange(0.5)
        .setMaxRange(20)
        .setRangeStep(0.1)
        .setAngleStep(1)
        .setMinImpactAngle(-25)
        .setImpactAngleWeight(0.75) // weight for impact angle in scoring
        .setTimeOfFlightWeight(0.25) // weight for time of flight in scoring
        // target parameters
        .setTargetElevationMeters(1) // 0.0 is at the same height as the launcher, negative means target is below launcher
        .setMinPeakHeight(1.5)
        .setMaxPeakHeight(3) // height above launch point
        // robot constraints
        .setMinLaunchAngleDeg(90 - 49)
        .setMaxLaunchAngleDeg(90 - 21)
        .setMaxLaunchVelocityMps(15) // max velocity constraint for shooter mechanism
        .setMinLaunchVelocityMps(2); // min velocity constraint for shooter mechanism

    // generate the lookup table
    BallisticsCalculator calculator = new BallisticsCalculator(config);

    // Print table header
    System.out.println("\n" + String.format("%-10s %-12s %-14s %-13s %-12s %-14s %-8s",
        "Range (m)", "Angle (deg)", "Velocity (m/s)", "Impact (deg)", "Height (m)", "Flight (s)", "Score"));
    System.out.println("=".repeat(95));

    // simulate as we travel away from the target, the range will increase 
    // in increments of 0.1 meters (approx. 5m/s), so we can print the best launch parameter for each range increment
    // calculate how many increments of 0.1 meters there are between 0.5 and 20 meters to prevent floating point errors
    int counts = (int) ((20 - 0.5) / 0.1);
    for(int count = 0; count <= counts; count++) {
      double simulatedRange = 0.5 + (count * 0.1);

      // the shooter subsystem would call this method to get target velocity and angle for a given range, the method will return the best launch parameter for that range increment
      LaunchParameter param = calculator.getBestLaunchParameter(simulatedRange); // or real range from drive subsystem

      if(param != null) {
        System.out.println(String.format("%-10.2f %-12.2f %-14.2f %-13.2f %-12.2f %-14.2f %-8.2f",
            param.getRangeMeters(),
            param.getLaunchAngleDeg(),
            param.getLaunchVelocityMps(),
            param.getImpactAngleDeg(),
            param.getPeakHeightMeters(),
            param.getTimeOfFlightSeconds(),
            param.getScore()));
      }
    }

    // Demonstrate the new IterativeAimingCalculator using the lookup table
    System.out.println("\n" + "=".repeat(95));
    System.out.println("Predictive Aiming Example: Hitting a Moving Target");
    System.out.println("=".repeat(95));

    // Create an aiming calculator with our ballistics lookup table
    IterativeAimingCalculator aimer = new IterativeAimingCalculator(calculator);

    // Simulate a moving target scenario
    double targetPosX = 10.0; // 10 meters away
    double targetPosY = 5.0; // 5 meters to the side
    double targetVelX = 2.0; // Target moving at 2 m/s in X direction
    double targetVelY = 1.0; // Target moving at 1 m/s in Y direction

    double robotPosX = 0.0; // Robot at origin
    double robotPosY = 0.0;
    double robotVelX = 0.0; // Robot stationary
    double robotVelY = 0.0;

    double initialRangeEstimate = Math.sqrt(targetPosX * targetPosX + targetPosY * targetPosY);
    LaunchParameter initialEstimate = calculator.getBestLaunchParameter(initialRangeEstimate);

    if(initialEstimate != null) {
      try {
        double aimAngle = aimer.iterativePredictiveAim(
            targetVelX, targetVelY,
            targetPosX, targetPosY,
            robotVelX, robotVelY,
            robotPosX, robotPosY,
            initialEstimate.getTimeOfFlightSeconds(),
            10 // Maximum 10 iterations for convergence
        );

        System.out.println("Target Position: (" + String.format("%.2f", targetPosX) + ", "
            + String.format("%.2f", targetPosY) + ") meters");
        System.out.println("Target Velocity: (" + String.format("%.2f", targetVelX) + ", "
            + String.format("%.2f", targetVelY) + ") m/s");
        System.out.println("Aim Angle: " + String.format("%.2f", aimAngle) + " degrees");
        System.out.println("(Using iterative lookup table convergence)");
      } catch (IllegalArgumentException e) {
        System.out.println("Error: " + e.getMessage());
      }
    }
  }
}
