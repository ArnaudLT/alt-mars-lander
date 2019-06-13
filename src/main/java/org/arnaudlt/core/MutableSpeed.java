package org.arnaudlt.core;

import java.util.Objects;

public class MutableSpeed {


    double horizontalSpeed;

    double verticalSpeed;

    MutableSpeed(double horizontalSpeed, double verticalSpeed) {

        this.horizontalSpeed = horizontalSpeed;
        this.verticalSpeed = verticalSpeed;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MutableSpeed that = (MutableSpeed) o;
        return Double.compare(that.horizontalSpeed, horizontalSpeed) == 0
            && Double.compare(that.verticalSpeed, verticalSpeed) == 0;
    }

    @Override
    public int hashCode() {

        return Objects.hash(horizontalSpeed, verticalSpeed);
    }
}
