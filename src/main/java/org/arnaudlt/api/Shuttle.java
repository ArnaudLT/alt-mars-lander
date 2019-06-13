package org.arnaudlt.api;

import java.util.Objects;


public class Shuttle {

    private final Position position;

    private final Speed speed;

    private final int fuel;

    private final Control control;

    private final Status status;


    public Shuttle(Position position, Speed speed, int fuel, Control control, Status status) {

        this.position = position;
        this.speed = speed;
        this.fuel = fuel;
        this.control = control;
        this.status = status;
    }

    public Position getPosition() {

        return position;
    }

    public Speed getSpeed() {

        return speed;
    }

    public int getFuel() {

        return fuel;
    }

    public Control getControl() {

        return control;
    }

    public Status getStatus() {

        return status;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Shuttle shuttle = (Shuttle) o;
        return fuel == shuttle.fuel && position.equals(shuttle.position) && speed.equals(shuttle.speed) && control
            .equals(shuttle.control) && status == shuttle.status;
    }

    @Override
    public int hashCode() {

        return Objects.hash(position, speed, fuel, control, status);
    }

    @Override
    public String toString() {

        return "Shuttle{" + "position=" + position + ", speed=" + speed + ", fuel=" + fuel + ", control=" + control
            + ", status=" + status + '}';
    }
}
