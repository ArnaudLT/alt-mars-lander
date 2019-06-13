package org.arnaudlt.api;

import java.util.Objects;

public class Control {

    private final int rotate;

    private final int power;

    public Control(int rotate, int power) {

        this.rotate = rotate;
        this.power = power;
    }

    public int getRotate() {

        return rotate;
    }

    public int getPower() {

        return power;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Control control = (Control) o;
        return rotate == control.rotate && power == control.power;
    }

    @Override
    public int hashCode() {

        return Objects.hash(rotate, power);
    }

    @Override
    public String toString() {

        return "{" + "rotate=" + rotate + ", power=" + power + '}';
    }
}
