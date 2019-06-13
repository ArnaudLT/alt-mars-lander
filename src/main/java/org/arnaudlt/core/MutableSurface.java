package org.arnaudlt.core;

import java.util.ArrayList;
import java.util.List;
import org.arnaudlt.api.Point;
import org.arnaudlt.api.Surface;

public class MutableSurface {


    List<Point> points;

    List<Segment> segments;

    Segment flatZone;


    MutableSurface(Surface surface) {

        this.points = surface.getPoints();
        this.segments = buildSegments(surface);
        this.flatZone = findFlatZone(this.segments);
    }

    private static List<Segment> buildSegments(Surface surface) {

        List<Point> pts = surface.getPoints();
        List<Segment> segs = new ArrayList<>(pts.size()-1);
        for (int i = 1; i < pts.size(); i++) {

            final Point previous = pts.get(i - 1);
            final Point current = pts.get(i);
            segs.add(new Segment(previous.getX(),previous.getY(),current.getX(),current.getY()));
        }
        return segs;
    }

    private static Segment findFlatZone(List<Segment> segs) {

        return segs.stream()
            .filter(s -> Math.abs(s.start.y - s.end.y) < 0.000_000_1)
            .findFirst()
            .get();
    }


    boolean collideWithSurface(MutablePosition p1, MutablePosition p2) {

        if ( p2.x > 6999 || p2.x < 0 || p2.y > 2999 || p2.y < 0 ) {
            return true;
        }
        for (Segment s : this.segments) {

            if ( s.intersect(p1.x, p1.y, p2.x, p2.y) ) {
                return true;
            }
        }
        return false;
    }


    static class DoublePoint {

        double x;
        double y;

        DoublePoint(double x, double y) {

            this.x = x;
            this.y = y;
        }

    }

    static class Segment {

        DoublePoint start;
        DoublePoint end;


        Segment(double x, double y, double x1, double y1) {
            this.start = new DoublePoint(x, y);
            this.end = new DoublePoint(x1, y1);
        }

        boolean intersect(double p2x, double p2y, double q2x, double q2y) {

            return intersect(start.x, start.y, end.x, end.y, p2x, p2y, q2x, q2y);
        }

        static boolean intersect(double p1x, double p1y, double q1x, double q1y,
                                 double p2x, double p2y, double q2x, double q2y ) {

            // Find the four orientations needed for general and
            // special cases
            int o1 = orientation(p1x, p1y, q1x, q1y, p2x, p2y);
            int o2 = orientation(p1x, p1y, q1x, q1y, q2x, q2y);
            int o3 = orientation(p2x, p2y, q2x, q2y, p1x, p1y);
            int o4 = orientation(p2x, p2y, q2x, q2y, q1x, q1y);
            // General case
            if (o1 != o2 && o3 != o4) {
                return true;
            }
            // Special Cases
            // p1, q1 and p2 are colinear and p2 lies on segment p1q1
            if (o1 == 0 && onSegment(p1x, p1y, p2x, p2y, q1x, q1y)) return true;
            // p1, q1 and p2 are colinear and q2 lies on segment p1q1
            if (o2 == 0 && onSegment(p1x, p1y, q2x, q2y, q1x, q1y)) return true;
            // p2, q2 and p1 are colinear and p1 lies on segment p2q2
            if (o3 == 0 && onSegment(p2x, p2y, p1x, p1y, q2x, q2y)) return true;
            // p2, q2 and q1 are colinear and q1 lies on segment p2q2
            if (o4 == 0 && onSegment(p2x, p2y, q1x, q1y, q2x, q2y)) return true;
            return false; // Doesn’t fall in any of the above cases
        }

        static boolean onSegment(double px, double py, double qx, double qy, double rx, double ry) {

            return (qx <= Math.max(px, rx) && qx >= Math.min(px, rx) &&
                qy <= Math.max(py, ry) && qy >= Math.min(py, ry));
        }

        static int orientation(double px, double py, double qx, double qy, double rx, double ry) {

            double val = (qy - py) * (rx - qx) - (qx - px) * (ry - qy);
            if ( Math.abs(val) < 0.000_000_1 ) {
                return 0;
            } else if ( val > 0 ) {
                return 1;
            } else {
                return 2;
            }
        }

    }

}
