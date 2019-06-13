package org.arnaudlt.core;

import java.util.Objects;

public class MutablePosition {


    double x;

    double y;

    MutablePosition(double x, double y) {

        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MutablePosition that = (MutablePosition) o;
        return Double.compare(that.x, x) == 0 && Double.compare(that.y, y) == 0;
    }

    @Override
    public int hashCode() {

        return Objects.hash(x, y);
    }
}
