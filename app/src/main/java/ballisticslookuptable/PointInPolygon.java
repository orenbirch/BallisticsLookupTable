package ballisticslookuptable;

import java.util.List;
import java.util.Optional;

public class PointInPolygon {

    /** Tolerance for floating-point comparisons. */
    private static final double EPSILON = 1e-10;


    /**
     * Result of a point-in-polygon test.
     */
    public enum HitResult {
        /** The point is strictly inside a polygon. */
        INSIDE,
        /** The point lies exactly on a polygon edge or vertex. */
        ON_BOUNDARY,
        /** The point is outside all polygons. */
        OUTSIDE
    }
    
    /**
     * Immutable 2D point.
     */
    public record Point(double x, double y) {}


    /**
     * A polygon defined by an ordered list of vertices.
     * The last vertex is implicitly connected back to the first.
     */
    public record Polygon(List<Point> vertices) {
    }

    /**
     * Creates a polygon if the vertex list is valid (at least 3 points).
     * @param vertices ordered list of polygon vertices
     * @return Optional polygon instance if valid
     */
    public static Optional<Polygon> createPolygon(List<Point> vertices) {
        if (vertices == null || vertices.size() < 3) {
            return Optional.empty();
        }
        return Optional.of(new Polygon(vertices));
    }

    /**
     * Determines if a point is inside, on the boundary, or outside a polygon.
     *
     * @param point The point to test.
     * @param polygon The polygon to test against.
     * @return A HitResult indicating the relationship of the point to the polygon.
        * Time complexity: O(V).
     */
    public static HitResult isPointInPolygon(Point point, Polygon polygon) {
        List<Point> vetices = polygon.vertices();
        int n = vetices.size();
        int crossings = 0;

        for (int i = 0; i < n; i++) {
            Point a = vetices.get(i);
            Point b = vetices.get((i + 1) % n);   // wrap around to close the polygon

            // ── Edge case: point exactly on this edge (or vertex) ──────────────
            if (isOnSegment(a, b, point)) {
                return HitResult.ON_BOUNDARY;
            }

            // ── Ray-crossing test ──────────────────────────────────────────────
            // Cast a ray from point in the +X direction.
            // Count upward crossings and downward crossings with the "half-open"
            // interval rule to avoid double-counting vertices:
            //   - include the edge if a.y <= point.y < b.y  (upward edge)
            //   - include the edge if b.y <= point.y < a.y  (downward edge)
            if (rayIntersectsEdge(point, a, b)) {
                crossings++;
            }
        }

        // Odd number of crossings -> inside
        return (crossings % 2 == 1) ? HitResult.INSIDE : HitResult.OUTSIDE;
    }

    /**
     * Returns {@code true} if the horizontal ray from {@code point} toward +X
     * crosses the directed edge {@code a→b}.
     *
     * <p>Uses the half-open interval [a.y, b.y) so that a vertex shared by two
     * edges is counted exactly once, correctly handling points aligned with
     * horizontal edges and polygon vertices without extra special-casing.
        * Time complexity: O(1).
     */
    private static boolean rayIntersectsEdge(Point point, Point a, Point b) {
        // Check whether point.y is in the half-open vertical range of the edge.
        boolean inRange = (a.y <= point.y && point.y < b.y)   // upward edge
                       || (b.y <= point.y && point.y < a.y);   // downward edge

        if (!inRange) return false;

        // Compute the x-coordinate of the edge at point.y via linear interpolation.
        // Cross only if that intersection is strictly to the right of point.
        double intersectX = a.x + (point.y - a.y) * (b.x - a.x) / (b.y - a.y);
        return point.x < intersectX;
    }

    /**
     * Returns {@code true} if point {@code point} lies exactly on segment {@code a→b}.
     *
     * <p>First confirms collinearity via the cross product, then checks that
     * {@code point} is within the bounding box of the segment.
        * Time complexity: O(1).
     */
    private static boolean isOnSegment(Point a, Point b, Point point) {
        // Cross product of (b-a) × (point-a) — zero means collinear.
        double cross = (b.x - a.x) * (point.y - a.y)
                     - (b.y - a.y) * (point.x - a.x);

        if (Math.abs(cross) > EPSILON) return false;   // not collinear

        // Confirm point is within the axis-aligned bounding box of the segment.
        return point.x >= Math.min(a.x, b.x) - EPSILON
            && point.x <= Math.max(a.x, b.x) + EPSILON
            && point.y >= Math.min(a.y, b.y) - EPSILON
            && point.y <= Math.max(a.y, b.y) + EPSILON;
    }


    /**
     * Tests whether the given point lies inside, on the boundary of, or outside
     * any polygon in the provided list.
     *
     * <p>Returns {@link HitResult#ON_BOUNDARY} if the point is on any edge or
     * vertex of any polygon, {@link HitResult#INSIDE} if it falls strictly
     * inside at least one polygon, and {@link HitResult#OUTSIDE} otherwise.
     *
     * @param polygons non-null list of polygons to test against
     * @param point    the 2D query point
     * @return the {@link HitResult} describing the point's relationship to the polygons
    * Time complexity: O(K * V).
     */
    public static HitResult hitTest(List<Polygon> polygons, Point point) {
        if (polygons == null || polygons.isEmpty()) {
            return HitResult.OUTSIDE;
        }

        for (Polygon polygon : polygons) {
            HitResult result = isPointInPolygon(point, polygon);
            if (result == HitResult.ON_BOUNDARY) return HitResult.ON_BOUNDARY;
            if (result == HitResult.INSIDE)      return HitResult.INSIDE;
        }

        return HitResult.OUTSIDE;
    }
}
