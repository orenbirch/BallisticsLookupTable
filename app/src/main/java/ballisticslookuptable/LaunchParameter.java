package ballisticslookuptable;



public class LaunchParameter {
    private double rangeMeters;
    private double launchAngleDeg;
    private double launchVelocityMps;
    private double impactAngleDeg;
    private double peakHeightMeters;
    private double timeOfFlightSeconds;
    private double score;

    public LaunchParameter(double launchAngleDeg, double rangeMeters, double elevationToTargetMeters) {
        this.rangeMeters = rangeMeters;
        this.launchAngleDeg = launchAngleDeg;
        this.launchVelocityMps = BallisticsUtility.calculateLaunchVelocityForRange(rangeMeters, elevationToTargetMeters, launchAngleDeg);
        this.impactAngleDeg = BallisticsUtility.calculateImpactAngleAtTarget(rangeMeters, elevationToTargetMeters, launchVelocityMps, launchAngleDeg);
        // TODO: launcher height is currently assumed to be 0, this means that out max height constraint is height above launcher
        this.peakHeightMeters = BallisticsUtility.calculatePeakHeight(launchVelocityMps, launchAngleDeg, 0);
        this.timeOfFlightSeconds = BallisticsUtility.calculateFlightTimeFromRange(rangeMeters, elevationToTargetMeters, launchAngleDeg, launchVelocityMps);
        //this.timeOfFlightSeconds = BallisticsUtility.calculateFlightTimeFromElevation(elevationToTargetMeters, launchVelocityMps, launchAngleDeg);
        this.score = 0;
    }

    public double getRangeMeters() {
        return rangeMeters;
    }

    public double getLaunchAngleDeg() {
        return launchAngleDeg;
    }

    public double getLaunchVelocityMps() {
        return launchVelocityMps;
    }

    public double getImpactAngleDeg() {
        return impactAngleDeg;
    }

    public double getPeakHeightMeters() {
        return peakHeightMeters;
    }

    public double getTimeOfFlightSeconds() {
        return timeOfFlightSeconds;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "LaunchParameter{" +
                "rangeMeters=" + String.format("%.2f", rangeMeters) +
                ", launchAngleDeg=" + String.format("%.2f", launchAngleDeg) +
                ", launchVelocityMps=" + String.format("%.2f", launchVelocityMps) +
                ", impactAngleDeg=" + String.format("%.2f", impactAngleDeg) +
                ", peakHeightMeters=" + String.format("%.2f", peakHeightMeters) +
                ", timeOfFlightSeconds=" + String.format("%.2f", timeOfFlightSeconds) +
                ", score=" + String.format("%.2f", score) +
                '}';
    }
}

