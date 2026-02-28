package ballisticslookuptable;

public class App {

    public String getGreeting() {
        return "Hello World!";
    }

    public static void main(String[] args) {
        System.out.println(new App().getGreeting());

        // generate the lookup table
        BallisticsCalculator calculator = new BallisticsCalculator();

        // simulate as we travel away from the target, the range will increase 
        // in increments of 0.1 meters (approx. 5m/s), so we can print the best launch parameter for each range increment
        // calculate how many increments of 0.1 meters there are between 0.5 and 20 meters to prevent floating point errors
        int counts = (int) ((20 - 0.5) / 0.1); 
        for (int count = 0; count <= counts; count++) {
            double simulatedRange = 0.5 + (count * 0.1);
            
            // the shooter subsystem would call this method to get target velocity and angle for a given range, the method will return the best launch parameter for that range increment
            LaunchParameter param = calculator.getBestLaunchParameter(simulatedRange); // or real range from drive subsystem

            if (param != null) {
                System.out.println("Range: " + String.format("%.2f", simulatedRange) + " meters, Best Launch Parameter: " + param);
            }
        }
    }
}
