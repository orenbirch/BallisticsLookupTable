package ballisticslookuptable;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        PointInPolygon.Polygon square = new PointInPolygon.Polygon(List.of(
            new Point(0, 0),
            new Point(4 ,0),
            new Point(4, 4),
            new Point(0, 4)
        ));
        
        Point point = new Point(0.5, 0.5);
        assertEquals(PointInPolygon.HitResult.INSIDE, PointInPolygon.isPointInPolygon(point, square));
    }

    @Test
    @DisplayName("point in polygon test should return HitResult.ON_BOUNDARY for point on polygon edge")
    void testPointInPolygonOnBoundary() {
        PointInPolygon.Polygon square = new PointInPolygon.Polygon(List.of(
            new Point(0, 0),
            new Point(4 ,0),
            new Point(4, 4),
            new Point(0, 4)
        ));
        
        Point point = new Point(0, 0);
        assertEquals(PointInPolygon.HitResult.ON_BOUNDARY, PointInPolygon.isPointInPolygon(point, square));
    }

    @Test
    @DisplayName("point in polygon test should return HitResult.OUTSIDE for point outside polygon")
    void testPointInPolygonOutside() {
        PointInPolygon.Polygon square = new PointInPolygon.Polygon(List.of(
            new Point(0, 0),
            new Point(4 ,0),
            new Point(4, 4),
            new Point(0, 4)
        ));
        
        Point point = new Point(-1, -1);
        assertEquals(PointInPolygon.HitResult.OUTSIDE, PointInPolygon.isPointInPolygon(point, square));
    } 

    @Test
    @DisplayName("point in polygon test should return HitResult.INSIDE for point inside concave polygon")
    void testPointInPolygonInsideConcave() {
        PointInPolygon.Polygon concave = new PointInPolygon.Polygon(List.of(
            new Point(0, 0),
            new Point(4 ,0),
            new Point(4, 4),
            new Point(2, 2),  // inward "dent"
            new Point(0, 4)
        ));
        
        Point point = new Point(1, 1);
        assertEquals(PointInPolygon.HitResult.INSIDE, PointInPolygon.isPointInPolygon(point, concave));
    }


    @Test
    @DisplayName("point in polygon test should return HitResult.OUTSIDE for point outside concave polygon")
    void testPointInPolygonOutsideConcave() {
        PointInPolygon.Polygon concave = new PointInPolygon.Polygon(List.of(
            new Point(0, 0),
            new Point(4 ,0),
            new Point(4, 4),
            new Point(2, 2),  // inward "dent"
            new Point(0, 4)
        ));
        
        Point point = new Point(2, 3);
        assertEquals(PointInPolygon.HitResult.OUTSIDE, PointInPolygon.isPointInPolygon(point, concave));
    }

    @Test
    @DisplayName("point in polygon test should return HitResult.ON_BOUNDARY for point on concave polygon edge")
    void testPointInPolygonOnBoundaryConcave() {
        PointInPolygon.Polygon concave = new PointInPolygon.Polygon(List.of(
            new Point(0, 0),
            new Point(4 ,0),
            new Point(4, 4),
            new Point(2, 2),  // inward "dent"
            new Point(0, 4)
        ));
        
        Point point = new Point(2, 2);
        assertEquals(PointInPolygon.HitResult.ON_BOUNDARY, PointInPolygon.isPointInPolygon(point, concave));
    }

    @Test
    @DisplayName("point in polygon test should return HitResult.INSIDE for point inside polygon with collinear edges")
    void testPointInPolygonInsideCollinear() {
        PointInPolygon.Polygon collinear = new PointInPolygon.Polygon(List.of(
            new Point(0, 0),
            new Point(4 ,0),
            new Point(4, 4),
            new Point(2, 4),  // collinear edge
            new Point(0, 4)
        ));
        
        Point point = new Point(3, 3);
        assertEquals(PointInPolygon.HitResult.INSIDE, PointInPolygon.isPointInPolygon(point, collinear));
    }   

    @Test
    @DisplayName("point in polygon test should return HitResult.ON_BOUNDARY for point on collinear edge")
    void testPointInPolygonOnBoundaryCollinear() {
        PointInPolygon.Polygon collinear = new PointInPolygon.Polygon(List.of(
            new Point(0, 0),
            new Point(4 ,0),
            new Point(4, 4),
            new Point(2, 4),  // collinear edge
            new Point(0, 4)
        ));
        
        Point point = new Point(3, 4);
        assertEquals(PointInPolygon.HitResult.ON_BOUNDARY, PointInPolygon.isPointInPolygon(point, collinear));  
    }

    @Test
    @DisplayName("point in polygon test should return HitResult.INSIDE for point inside polygon with very close edges")
    void testPointInPolygonInsideCloseEdges() {
        PointInPolygon.Polygon closeEdges = new PointInPolygon.Polygon(List.of(
            new Point(0, 0),
            new Point(4 ,0),
            new Point(4, 4),
            new Point(2.0000000001, 4),  // very close edge
            new Point(0, 4)
        ));
        
        Point point = new Point(3, 3);
        assertEquals(PointInPolygon.HitResult.INSIDE, PointInPolygon.isPointInPolygon(point, closeEdges));
    }

    @Test
    @DisplayName("point in polygon test should return HitTest.INSIDE for point inside any polygon in a list of polygons")
    void testPointInPolygonInsideAnyPolygon() {
        List<PointInPolygon.Polygon> polygons = List.of(
            new PointInPolygon.Polygon(List.of(
                new Point(0, 0),
                new Point(4 ,0),
                new Point(4, 4),
                new Point(0, 4)
            )),
            new PointInPolygon.Polygon(List.of(
                new Point(5, 5),
                new Point(9 ,5),
                new Point(9, 9),
                new Point(5, 9)
            ))
        );

        Point point = new Point(3, 3);
        assertEquals(PointInPolygon.HitResult.INSIDE, PointInPolygon.hitTest(polygons, point));
    }
}