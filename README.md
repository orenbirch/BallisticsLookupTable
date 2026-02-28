# Ballistics Lookup Table Demo Application

## Overview

Welcome to the **Ballistics Lookup Table** project! This is an educational Java application designed to demonstrate how to calculate and generate a lookup table of optimal launch parameters for hitting targets at various distances.

This application uses physics-based simulations to determine the best combination of **launch angle** and **launch velocity** for different target ranges. The results are stored in a lookup table that can be quickly queried during runtime—useful for robotics systems, game development, or physics simulations where you need to quickly find the optimal way to launch a projectile at a target.

### What This Project Teaches

- **Physics Calculations**: How to apply projectile motion equations to solve real-world problems
- **Algorithm Design**: How to optimize solutions by scoring and comparing multiple candidates
- **Java Development**: Working with data structures (TreeMaps), object-oriented design, and build automation
- **Gradle Build System**: How to configure, build, and run Java projects using Gradle

---

## Prerequisites

Before you begin, make sure you have the following installed on your system:

### Required Software

1. **Java Development Kit (JDK) 21 or later**
   - This project requires Java 21 due to language features used in the codebase
   - Download from: [Eclipse Adoptium](https://adoptium.net/) or [Oracle JDK](https://www.oracle.com/java/technologies/downloads/)
   - After installation, verify by opening a terminal and running: `java -version`

2. **Gradle**
   - This project uses the Gradle wrapper (included), so you don't need to install Gradle separately
   - However, if you want to use Gradle system-wide, download from: [Gradle Official Website](https://gradle.org/install/)

### Optional but Recommended

- **Git**: For version control (download from [git-scm.com](https://git-scm.com/))
- **IDE**: Any Java IDE such as:
  - IntelliJ IDEA (Community Edition is free)
  - Eclipse
  - VS Code with Java Extension Pack
  - NetBeans

---

## Project Structure

Here's a quick overview of the project folder structure:

```
BallisticsLookupTable/
├── app/                          # Main application code
│   ├── src/
│   │   ├── main/java/
│   │   │   └── ballisticslookuptable/
│   │   │       ├── App.java                    # Entry point of the application
│   │   │       ├── BallisticsCalculator.java   # Core calculation engine
│   │   │       ├── BallisticsUtility.java      # Physics helper functions
│   │   │       └── LaunchParameter.java        # Data class for launch parameters
│   │   └── test/java/                          # Unit tests
│   ├── build.gradle               # Gradle build configuration
│   └── build/                     # Generated build artifacts (created during build)
├── gradle/                        # Gradle wrapper files
├── gradlew                        # Gradle wrapper script (Linux/Mac)
├── gradlew.bat                    # Gradle wrapper script (Windows)
├── gradle.properties              # Gradle configuration properties
└── settings.gradle                # Gradle project settings

```

### Key Source Files Explained

- **App.java**: The main entry point. It creates a `BallisticsCalculator` and iterates through different ranges to display the optimal launch parameters for each distance.

- **BallisticsCalculator.java**: The core of the application. It generates a lookup table by simulating various launch angles and velocities, then scores each trajectory based on impact angle and time of flight.

- **LaunchParameter.java**: A data class that stores all the information about a single launch scenario:
  - Launch angle (degrees)
  - Launch velocity (meters per second)
  - Impact angle (degrees)
  - Peak height (meters)
  - Time of flight (seconds)
  - Score (used to determine the best trajectory)

- **BallisticsUtility.java**: Contains utility functions for physics calculations like:
  - Calculating required velocity for a given range
  - Calculating impact angle
  - Calculating peak height
  - Calculating flight time

---

## Building the Project

### Step-by-Step Build Instructions

Follow these steps to build the project from scratch:

#### 1. Navigate to the Project Directory

Open your terminal (Command Prompt on Windows, Terminal on Mac/Linux) and navigate to the project root:

```bash
cd BallisticsLookupTable
```

Make sure you're in the directory that contains `gradlew`, `gradlew.bat`, and `build.gradle` files.

#### 2. Run the Build Command

The Gradle wrapper (`gradlew` or `gradlew.bat`) handles everything for you. Run:

**On Windows:**
```bash
gradlew.bat build
```

**On Mac/Linux:**
```bash
./gradlew build
```

#### 3. What Happens During Build

When you run the build command, Gradle will:
- Download any required dependencies (like JUnit for testing)
- Compile all Java source files in `src/main/java/`
- Compile all test files in `src/test/java/`
- Run unit tests
- Package the compiled code into a JAR file (located in `app/build/libs/`)

#### 4. Successful Build Output

You should see output similar to:
```
> Task :app:compileJava
> Task :app:processResources
> Task :app:classes
> Task :app:jar
> Task :app:startScripts
> Task :app:build

BUILD SUCCESSFUL in X.XXs
```

If you see `BUILD SUCCESSFUL`, congratulations! The project compiled without errors.

#### 5. Troubleshooting Build Issues

**Issue: "Gradle not found" or permission denied**
- **Windows**: Make sure you're using `gradlew.bat` (with .bat extension)
- **Mac/Linux**: Make sure the file is executable: `chmod +x gradlew`

**Issue: "Java compilation failed"**
- Ensure you have Java 21 or later: `java -version`
- Check that your JAVA_HOME environment variable is set correctly

**Issue: "Build successful but then runs slowly"**
- First build is slower because Gradle downloads dependencies. Subsequent builds are much faster.

---

## Running the Application

### From the Terminal

Once the project is built successfully, run the application using the Gradle wrapper:

**On Windows:**
```bash
gradlew.bat run
```

**On Mac/Linux:**
```bash
./gradlew run
```

### What You'll See

When you run the application, you'll see output like this:

```
Hello World!
Range: 0.50 meters, Best Launch Parameter: LaunchParameter{rangeMeters=0.50, launchAngleDeg=45.00, launchVelocityMps=2.21, impactAngleDeg=45.00, peakHeightMeters=0.25, timeOfFlightSeconds=0.32, score=1.00}
Range: 0.60 meters, Best Launch Parameter: LaunchParameter{rangeMeters=0.60, launchAngleDeg=45.00, launchVelocityMps=2.42, impactAngleDeg=45.00, peakHeightMeters=0.30, timeOfFlightSeconds=0.35, score=0.95}
Range: 0.70 meters, Best Launch Parameter: LaunchParameter{rangeMeters=0.70, launchAngleDeg=45.00, launchVelocityMps=2.61, impactAngleDeg=45.00, peakHeightMeters=0.37, timeOfFlightSeconds=0.38, score=0.90}
...
```

The application runs through ranges from 0.5 meters to 20 meters in 0.1-meter increments (about 200 test cases) and displays the optimal launch parameters for each distance.

### Running Without Gradle

If you prefer to run the compiled JAR file directly (after building):

**Windows:**
```bash
java -cp app/build/classes/java/main ballisticslookuptable.App
```

**Mac/Linux:**
```bash
java -cp app/build/classes/java/main ballisticslookuptable.App
```

---

## Understanding the Output

Each line of output shows the optimal launch parameters for a specific range. Here's what each field means:

### Output Field Explanations

```
Range: 0.50 meters, Best Launch Parameter: 
  LaunchParameter{
    rangeMeters=0.50              # Distance to target (in meters)
    launchAngleDeg=45.00          # Angle to launch projectile (in degrees)
    launchVelocityMps=2.21        # Speed of projectile (in meters/second)
    impactAngleDeg=45.00          # Angle at which projectile hits target (in degrees)
    peakHeightMeters=0.25         # Maximum height reached during flight (in meters)
    timeOfFlightSeconds=0.32      # How long projectile is in the air (in seconds)
    score=1.00                    # Quality score (1.0 = perfect, lower = less ideal)
  }
```

### What Makes a "Best" Launch Parameter?

The application scores each trajectory based on two factors:

1. **Impact Angle (75% weight)**: The angle at which the projectile hits the target
   - An impact angle of -30° to -90° is ideal
   - This represents hitting the target at a steep downward angle, which is often more effective

2. **Time of Flight (25% weight)**: How long the projectile takes to reach the target
   - Shorter flight times are generally preferred
   - Less time means faster response and less vulnerability to target movement

The "best" launch parameter is the one with the highest combined score for that specific range.

---

## Running Tests

This project includes unit tests to verify the physics calculations. To run the tests:

**On Windows:**
```bash
gradlew.bat test
```

**On Mac/Linux:**
```bash
./gradlew test
```

After running tests, you can view a detailed test report at:
```
app/build/reports/tests/test/index.html
```

Open this HTML file in your web browser to see which tests passed and which failed.

---

## Modifying the Application

### Adjusting the Range of Calculations

Open `app/src/main/java/ballisticslookuptable/BallisticsCalculator.java` and look for these constants (near the top of the class):

```java
private final double MIN_RANGE = 0.5;      // Minimum range (meters)
private final double MAX_RANGE = 20;       // Maximum range (meters)
private final double RANGE_STEP = 0.25;    // Increment between ranges (meters)
```

- **MIN_RANGE**: Change this to start calculations at a different distance
- **MAX_RANGE**: Change this to calculate up to a different maximum distance
- **RANGE_STEP**: Change this to use larger/smaller increments (larger = fewer calculations but less precise)

### Adjusting Scoring Weights

In the same file, look for:

```java
private static final double impactAngleWeight = 0.75;
private static final double timeOfFlightWeight = 0.25;
```

- Increase `impactAngleWeight` if you care more about hitting at a specific angle
- Increase `timeOfFlightWeight` if you care more about speed

The two weights should always sum to 1.0.

### Adjusting Height Constraints

Look for these constants:

```java
private final double MAX_PEAK_HEIGHT = 3;    // Maximum allowed height (meters)
private final double MIN_PEAK_HEIGHT = 1.2;  // Minimum allowed height (meters)
```

These prevent trajectories that go too high or too low.

### After Making Changes

After modifying the code:

1. **Rebuild**: `gradlew.bat build` (Windows) or `./gradlew build` (Mac/Linux)
2. **Run**: `gradlew.bat run` (Windows) or `./gradlew run` (Mac/Linux)
3. Observe how your changes affect the output

---

## Common Gradle Commands Reference

Here are useful Gradle commands for developing this project:

| Command | Purpose |
|---------|---------|
| `gradle build` | Compile, test, and package the project |
| `gradle run` | Compile and run the application |
| `gradle test` | Run all unit tests |
| `gradle clean` | Delete all build artifacts (start fresh) |
| `gradle compileJava` | Only compile Java source files |
| `gradle tasks` | List all available Gradle tasks |
| `gradle dependencies` | Show project dependencies |

**Note**: Use `gradlew.bat` on Windows or `./gradlew` on Mac/Linux (these are the Gradle wrapper scripts included with the project).

---

## Project Dependencies

This project uses very few external dependencies to keep it simple:

- **Guava**: A utility library by Google (for helper functions)
- **JUnit Jupiter**: For unit testing

These are automatically downloaded by Gradle when you first build the project.

---

## Learning Resources

To deepen your understanding of the concepts in this project:

### Physics
- **Projectile Motion**: Search for "projectile motion equations" to understand the physics
- **Key Formula**: Range = (v² × sin(2θ)) / g
  - v = initial velocity
  - θ = launch angle
  - g = gravitational acceleration (9.81 m/s²)

### Java & Gradle
- [Java Language Documentation](https://docs.oracle.com/javase/tutorial/)
- [Gradle User Manual](https://docs.gradle.org/current/userguide/userguide.html)
- [TreeMap Documentation](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/TreeMap.html)

### Object-Oriented Design
- Study how `BallisticsCalculator` uses the `LaunchParameter` class
- Notice how `BallisticsUtility` provides utility functions used by multiple classes
- This is an example of the **Single Responsibility Principle** (each class has one job)

---

## Troubleshooting Common Issues

### "Command 'gradlew' not found"
- Make sure you're in the correct directory (the one containing `gradlew` and `build.gradle`)
- On Mac/Linux, try: `chmod +x gradlew`

### "Java cannot be found"
- Install JDK 21 or later
- Set your JAVA_HOME environment variable to your JDK installation directory
- Restart your terminal after setting environment variables

### "BUILD FAILED"
- Read the error message carefully
- Common causes:
  - Wrong Java version (need Java 21+)
  - Syntax errors in your modified code
  - Missing closing braces or semicolons

### Application runs but shows no output
- Make sure you're using the `run` task: `gradlew.bat run` or `./gradlew run`
- The output should start with "Hello World!"

### Gradle build is very slow
- First build downloads all dependencies (can take 2-5 minutes)
- Subsequent builds are much faster (usually 10-30 seconds)
- If builds remain slow, try: `gradle clean build` to start fresh

---

## Next Steps

Once you're comfortable running and understanding this application, try:

1. **Modify the physics**: Change the constants to see how they affect launch parameters
2. **Add new features**: Create a method to find the launch parameter for a specific range
3. **Improve the UI**: Print results in a table format for better readability
4. **Extend the tests**: Write unit tests for the physics calculations
5. **Optimize the lookup**: Use binary search to find launch parameters for ranges not in the table

---

## Contributing & Questions

If you encounter issues or have questions:
1. Check the **Troubleshooting** section above
2. Review the comments in the source code
3. Run `gradle tasks` to see all available commands
4. Consult the learning resources provided

---

## License

This is an educational project. Use it to learn and experiment!

---

Happy coding! This project is designed to teach both physics and Java programming. Don't hesitate to experiment—that's how you learn best!
