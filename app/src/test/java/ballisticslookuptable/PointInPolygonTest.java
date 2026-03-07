package ballisticslookuptable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ballisticslookuptable.PointInPolygon.Point;

public class PointInPolygonTest {


    @BeforeEach
    void setUp() {
        // nothing to do here since all methods are static and stateless
    }

    @Test
    @DisplayName("point in polygon test should return HitResult.INSIDE for point inside polygon")
    void testPointInPolygonInside() {
        java.util.Optional<PointInPolygon.Polygon> squareOptional = PointInPolygon.createPolygon(List.of(
            new Point(0, 0),
            new Point(4 ,0),
            new Point(4, 4),
            new Point(0, 4)
        ));

        assertTrue(squareOptional.isPresent(), "Polygon should be created");
        PointInPolygon.Polygon square = squareOptional.get();
        
        Point point = new Point(0.5, 0.5);
        assertEquals(PointInPolygon.HitResult.INSIDE, PointInPolygon.isPointInPolygon(point, square));
    }

    @Test
    @DisplayName("point in polygon test should return HitResult.ON_BOUNDARY for point on polygon edge")
    void testPointInPolygonOnBoundary() {
        java.util.Optional<PointInPolygon.Polygon> squareOptional = PointInPolygon.createPolygon(List.of(
            new Point(0, 0),
            new Point(4 ,0),
            new Point(4, 4),
            new Point(0, 4)
        ));

        assertTrue(squareOptional.isPresent(), "Polygon should be created");
        PointInPolygon.Polygon square = squareOptional.get();
        
        Point point = new Point(0, 0);
        assertEquals(PointInPolygon.HitResult.ON_BOUNDARY, PointInPolygon.isPointInPolygon(point, square));
    }

    @Test
    @DisplayName("point in polygon test should return HitResult.OUTSIDE for point outside polygon")
    void testPointInPolygonOutside() {
        java.util.Optional<PointInPolygon.Polygon> squareOptional = PointInPolygon.createPolygon(List.of(
            new Point(0, 0),
            new Point(4 ,0),
            new Point(4, 4),
            new Point(0, 4)
        ));

        assertTrue(squareOptional.isPresent(), "Polygon should be created");
        PointInPolygon.Polygon square = squareOptional.get();
        
        Point point = new Point(-1, -1);
        assertEquals(PointInPolygon.HitResult.OUTSIDE, PointInPolygon.isPointInPolygon(point, square));
    } 

    @Test
    @DisplayName("point in polygon test should return HitResult.INSIDE for point inside concave polygon")
    void testPointInPolygonInsideConcave() {
        java.util.Optional<PointInPolygon.Polygon> concaveOptional = PointInPolygon.createPolygon(List.of(
            new Point(0, 0),
            new Point(4 ,0),
            new Point(4, 4),
            new Point(2, 2),  // inward "dent"
            new Point(0, 4)
        ));

        assertTrue(concaveOptional.isPresent(), "Polygon should be created");
        PointInPolygon.Polygon concave = concaveOptional.get();
        
        Point point = new Point(1, 1);
        assertEquals(PointInPolygon.HitResult.INSIDE, PointInPolygon.isPointInPolygon(point, concave));
    }


    @Test
    @DisplayName("point in polygon test should return HitResult.OUTSIDE for point outside concave polygon")
    void testPointInPolygonOutsideConcave() {
        java.util.Optional<PointInPolygon.Polygon> concaveOptional = PointInPolygon.createPolygon(List.of(
            new Point(0, 0),
            new Point(4 ,0),
            new Point(4, 4),
            new Point(2, 2),  // inward "dent"
            new Point(0, 4)
        ));

        assertTrue(concaveOptional.isPresent(), "Polygon should be created");
        PointInPolygon.Polygon concave = concaveOptional.get();
        
        Point point = new Point(2, 3);
        assertEquals(PointInPolygon.HitResult.OUTSIDE, PointInPolygon.isPointInPolygon(point, concave));
    }

    @Test
    @DisplayName("point in polygon test should return HitResult.ON_BOUNDARY for point on concave polygon edge")
    void testPointInPolygonOnBoundaryConcave() {
        java.util.Optional<PointInPolygon.Polygon> concaveOptional = PointInPolygon.createPolygon(List.of(
            new Point(0, 0),
            new Point(4 ,0),
            new Point(4, 4),
            new Point(2, 2),  // inward "dent"
            new Point(0, 4)
        ));

        assertTrue(concaveOptional.isPresent(), "Polygon should be created");
        PointInPolygon.Polygon concave = concaveOptional.get();
        
        Point point = new Point(2, 2);
        assertEquals(PointInPolygon.HitResult.ON_BOUNDARY, PointInPolygon.isPointInPolygon(point, concave));
    }

    @Test
    @DisplayName("point in polygon test should return HitResult.INSIDE for point inside polygon with collinear edges")
    void testPointInPolygonInsideCollinear() {
        java.util.Optional<PointInPolygon.Polygon> collinearOptional = PointInPolygon.createPolygon(List.of(
            new Point(0, 0),
            new Point(4 ,0),
            new Point(4, 4),
            new Point(2, 4),  // collinear edge
            new Point(0, 4)
        ));

        assertTrue(collinearOptional.isPresent(), "Polygon should be created");
        PointInPolygon.Polygon collinear = collinearOptional.get();
        
        Point point = new Point(3, 3);
        assertEquals(PointInPolygon.HitResult.INSIDE, PointInPolygon.isPointInPolygon(point, collinear));
    }   

    @Test
    @DisplayName("point in polygon test should return HitResult.ON_BOUNDARY for point on collinear edge")
    void testPointInPolygonOnBoundaryCollinear() {
        java.util.Optional<PointInPolygon.Polygon> collinearOptional = PointInPolygon.createPolygon(List.of(
            new Point(0, 0),
            new Point(4 ,0),
            new Point(4, 4),
            new Point(2, 4),  // collinear edge
            new Point(0, 4)
        ));

        assertTrue(collinearOptional.isPresent(), "Polygon should be created");
        PointInPolygon.Polygon collinear = collinearOptional.get();
        
        Point point = new Point(3, 4);
        assertEquals(PointInPolygon.HitResult.ON_BOUNDARY, PointInPolygon.isPointInPolygon(point, collinear));  
    }

    @Test
    @DisplayName("point in polygon test should return HitResult.INSIDE for point inside polygon with very close edges")
    void testPointInPolygonInsideCloseEdges() {
        java.util.Optional<PointInPolygon.Polygon> closeEdgesOptional = PointInPolygon.createPolygon(List.of(
            new Point(0, 0),
            new Point(4 ,0),
            new Point(4, 4),
            new Point(2.0000000001, 4),  // very close edge
            new Point(0, 4)
        ));

        assertTrue(closeEdgesOptional.isPresent(), "Polygon should be created");
        PointInPolygon.Polygon closeEdges = closeEdgesOptional.get();
        
        Point point = new Point(3, 3);
        assertEquals(PointInPolygon.HitResult.INSIDE, PointInPolygon.isPointInPolygon(point, closeEdges));
    }

    @Test
    @DisplayName("point in polygon test should return HitTest.INSIDE for point inside any polygon in a list of polygons")
    void testPointInPolygonInsideAnyPolygon() {
        java.util.Optional<PointInPolygon.Polygon> firstOptional = PointInPolygon.createPolygon(List.of(
                new Point(0, 0),
                new Point(4 ,0),
                new Point(4, 4),
                new Point(0, 4)
        ));
        java.util.Optional<PointInPolygon.Polygon> secondOptional = PointInPolygon.createPolygon(List.of(
                new Point(5, 5),
                new Point(9 ,5),
                new Point(9, 9),
                new Point(5, 9)
        ));

        assertTrue(firstOptional.isPresent(), "Polygon should be created");
        assertTrue(secondOptional.isPresent(), "Polygon should be created");

        PointInPolygon.Polygon first = firstOptional.get();
        PointInPolygon.Polygon second = secondOptional.get();

        List<PointInPolygon.Polygon> polygons = List.of(first, second);

        Point point = new Point(3, 3);
        assertEquals(PointInPolygon.HitResult.INSIDE, PointInPolygon.hitTest(polygons, point));
    }
}