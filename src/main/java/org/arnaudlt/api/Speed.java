package org.arnaudlt.api;

import java.util.Objects;

public class Speed {

    private final double horizontalSpeed;

    private final double verticalSpeed;

    public Speed(double horizontalSpeed, double verticalSpeed) {

        this.horizontalSpeed = horizontalSpeed;
        this.verticalSpeed = verticalSpeed;
    }

    public double getHorizontalSpeed() {

        return horizontalSpeed;
    }

    public double getVerticalSpeed() {

        return verticalSpeed;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Speed speed = (Speed) o;
        return Double.compare(speed.horizontalSpeed, horizontalSpeed) == 0
            && Double.compare(speed.verticalSpeed, verticalSpeed) == 0;
    }

    @Override
    public int hashCode() {

        return Objects.hash(horizontalSpeed, verticalSpeed);
    }

    @Override
    public String toString() {

        return "{" + "horizontalSpeed=" + horizontalSpeed + ", verticalSpeed=" + verticalSpeed + '}';
    }
}
