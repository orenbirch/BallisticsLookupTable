# Performance Estimates for RoboRIO

Estimated clock cycle counts for methods running on RoboRIO (ARM Cortex-A9 @ 667 MHz).

## BallisticsUtility Methods - O(1)

All methods perform pure arithmetic calculations with no loops.

| Method | Estimated Cycles | Estimated Time |
|--------|-----------------|----------------|
| `calculateFlightTimeFromRange()` | ~100-500 | ~0.15-0.75 μs |
| `calculateFlightTimeFromVerticalMotion()` | ~100-500 | ~0.15-0.75 μs |
| `calculateFlightTimeFromElevation()` | ~100-500 | ~0.15-0.75 μs |
| `calculateImpactAngleAtTarget()` | ~100-500 | ~0.15-0.75 μs |
| `calculatePeakHeight()` | ~100-500 | ~0.15-0.75 μs |
| `calculateLaunchVelocityForRange()` | ~100-500 | ~0.15-0.75 μs |

**Notes:**
- Each method contains ~5-15 floating-point operations
- ARM Cortex-A9 NEON SIMD provides fast FP operations (~10-20 cycles each)
- Times assume JIT-compiled hot code

## BallisticsCalculator Methods

### Constructor / `generateLookupTable()` - O(R × A)

| Configuration | R (ranges) | A (angles) | Estimated Cycles | Estimated Time |
|---------------|------------|------------|-----------------|----------------|
| Default | 78 | 65 | ~1-5 million | ~1.5-7.5 ms |
| Custom (larger) | 195 | 65 | ~2.5-12 million | ~3.75-18 ms |

**Analysis:**
- Inner loop: ~50-100 FP operations per angle/range combination
- Score normalization: 2 passes over valid parameters
- **Recommendation:** Execute once at robot initialization (not in periodic loop)

### `getBestLaunchParameter()` - O(log N)

| Lookup Table Size | Tree Depth | Estimated Cycles | Estimated Time |
|-------------------|------------|-----------------|----------------|
| 78 entries | ~7 | ~500-2,000 | ~0.75-3 μs |
| 195 entries | ~8 | ~600-2,500 | ~0.9-3.75 μs |

**Analysis:**
- TreeMap binary search + comparison operations
- Very fast; suitable for real-time control loops

## IterativeAimingCalculator Methods

### `predictTarget()` - O(1)

| Estimated Cycles | Estimated Time |
|-----------------|----------------|
| ~200-500 | ~0.3-0.75 μs |

**Analysis:**
- 8-10 floating-point arithmetic operations
- No branching or complex logic

### `iterativePredictiveAim()` - O(I × log N)

| Iterations (I) | Estimated Cycles | Estimated Time |
|----------------|-----------------|----------------|
| 10 (default) | ~10,000-30,000 | ~15-45 μs |
| 5 | ~5,000-15,000 | ~7.5-22.5 μs |
| 15 | ~15,000-45,000 | ~22.5-67.5 μs |

**Analysis:**
- Each iteration calls `predictTarget()` + `getBestLaunchParameter()`
- Convergence typically occurs in 3-7 iterations
- **Recommendation:** Safe to call in 20ms control loop (uses < 0.3% CPU)

### `interativePredictCoordinate()` - O(I × log N)

Same performance characteristics as `iterativePredictiveAim()`.

## PointInPolygon Methods

### `isPointInPolygon()` - O(V)

| Vertices (V) | Estimated Cycles | Estimated Time |
|--------------|-----------------|----------------|
| 4 (square) | ~500-1,000 | ~0.75-1.5 μs |
| 6 (hexagon) | ~750-1,500 | ~1.1-2.25 μs |
| 10 (complex) | ~1,000-2,000 | ~1.5-3 μs |

**Analysis:**
- Ray-casting algorithm with one pass through vertices
- Includes edge intersection and boundary checks

### `hitTest()` - O(K × V)

| Polygons (K) | Avg Vertices (V) | Estimated Cycles | Estimated Time |
|--------------|------------------|-----------------|----------------|
| 3 | 4 | ~1,500-3,000 | ~2.25-4.5 μs |
| 5 | 6 | ~3,750-7,500 | ~5.6-11.25 μs |
| 10 | 6 | ~7,500-15,000 | ~11.25-22.5 μs |

**Analysis:**
- Tests point against multiple polygons sequentially
- Returns early on first `ON_BOUNDARY` or `INSIDE` hit

## Real-Time Control Loop Budget

For a **20ms control loop** (50 Hz, typical FRC robot periodic):

| Resource | Available | Notes |
|----------|-----------|-------|
| Clock cycles | ~13.3 million | Per 20ms period |
| Typical usage | < 50 μs | All runtime methods combined |
| CPU percentage | < 0.4% | Leaves 99.6% for other code |

### Example Budget Breakdown

```
Periodic Method (20ms):
  - 50× iterativePredictiveAim() calls    = ~2,250 μs  (11.25% CPU)
  - 100× getBestLaunchParameter() calls   = ~150 μs    (0.75% CPU)
  - 20× hitTest() calls (5 polygons)      = ~150 μs    (0.75% CPU)
  - Other robot code                      = ~17,450 μs (87.25% CPU)
  ────────────────────────────────────────────────────────────────
  Total                                   = 20,000 μs  (100% CPU)
```

## Optimization Notes

### Critical for Performance
1. **Initialize lookup table at robot startup** - One-time 1.5-7.5ms cost
2. **Warm up JIT compiler** - First calls are 10-100× slower
3. **Minimize GC pressure** - Avoid allocations in hot paths
4. **Use primitives where possible** - Optional wrapping adds overhead

### JVM Flags for RoboRIO
```bash
-XX:+UseG1GC                    # Better for real-time workloads
-XX:MaxGCPauseMillis=5          # Limit GC pause times
-XX:+DisableExplicitGC          # Prevent manual GC calls
-Xmx256m                        # Set appropriate heap size
```

### Profiling on RoboRIO
- Use Java Flight Recorder (JFR) for detailed profiling
- `jcmd <pid> JFR.start duration=60s filename=/tmp/recording.jfr`
- Analyze with JDK Mission Control or IntelliJ

## Caveats

These estimates are **order-of-magnitude approximations**:

- **JIT Compilation:** Cold code runs 10-100× slower until JIT-compiled
- **Cache Behavior:** Cache misses can add 50-200 cycles per access
- **GC Pauses:** Can introduce unpredictable latency spikes (1-50ms)
- **Thread Context Switches:** OS scheduler overhead not included
- **ARM Pipeline:** Assumes optimal instruction pipelining
- **Branch Prediction:** Mispredictions add ~10-20 cycle penalties

**For production code:** Always profile on actual hardware with realistic workloads.

## Variables Used in Complexity Analysis

- **R** = Number of range steps (default: 78)
- **A** = Number of angle steps (default: 65)
- **N** = Lookup table size (≈ R after filtering)
- **I** = Max iterations for convergence (default: 10)
- **V** = Number of polygon vertices (4-10 typical)
- **K** = Number of polygons to test (1-10 typical)
