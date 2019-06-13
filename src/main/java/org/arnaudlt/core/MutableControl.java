package org.arnaudlt.core;

import java.util.Objects;

public class MutableControl {


    int rotate;

    int power;

    MutableControl(int rotate, int power) {

        this.rotate = rotate;
        this.power = power;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MutableControl that = (MutableControl) o;
        return rotate == that.rotate && power == that.power;
    }

    @Override
    public int hashCode() {

        return Objects.hash(rotate, power);
    }
}
