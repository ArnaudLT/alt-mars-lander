package org.arnaudlt.api;

import java.util.List;
import java.util.Objects;

public class ControlChain {

    private final List<Control> controls;

    public ControlChain(List<Control> controls) {

        this.controls = controls;
    }

    public List<Control> getControls() {

        return controls;
    }

    public int size() {

        return ( this.controls != null ) ? this.controls.size() : 0;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ControlChain that = (ControlChain) o;
        return controls.equals(that.controls);
    }

    @Override
    public int hashCode() {

        return Objects.hash(controls);
    }

    @Override
    public String toString() {

        return "{" + "controls=" + controls + '}';
    }
}
