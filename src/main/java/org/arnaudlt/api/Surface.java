package org.arnaudlt.api;

import java.util.List;
import java.util.Objects;

public class Surface {


    private final List<Point> points;


    public Surface(List<Point> points) {

        this.points = points;
    }

    public List<Point> getPoints() {

        return points;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Surface surface = (Surface) o;
        return Objects.equals(points, surface.points);
    }

    @Override
    public int hashCode() {

        return Objects.hash(points);
    }
}
