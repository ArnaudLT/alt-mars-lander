package org.arnaudlt.core;

import java.util.Objects;
import org.arnaudlt.api.Control;
import org.arnaudlt.api.Position;
import org.arnaudlt.api.Shuttle;
import org.arnaudlt.api.Speed;
import org.arnaudlt.api.Status;

public class MutableShuttle {


    MutablePosition position;

    MutableSpeed speed;

    int fuel;

    MutableControl control;

    Status status;


    MutableShuttle(Shuttle shuttle) {

        this.position = new MutablePosition(shuttle.getPosition().getX(), shuttle.getPosition().getY());
        this.speed = new MutableSpeed(shuttle.getSpeed().getHorizontalSpeed(), shuttle.getSpeed().getVerticalSpeed());
        this.control = new MutableControl(shuttle.getControl().getRotate(),shuttle.getControl().getPower());
        this.fuel = shuttle.getFuel();
        this.status = shuttle.getStatus();
    }

    Shuttle buildImmutableShuttle() {

        return new Shuttle(
            new Position(this.position.x, this.position.y),
            new Speed(this.speed.horizontalSpeed, this.speed.verticalSpeed),
            this.fuel,
            new Control(this.control.rotate, this.control.power),
            this.status
        );
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MutableShuttle that = (MutableShuttle) o;
        return fuel == that.fuel && Objects.equals(position, that.position) && Objects.equals(speed, that.speed)
            && Objects.equals(control, that.control) && status == that.status;
    }

    @Override
    public int hashCode() {

        return Objects.hash(position, speed, fuel, control, status);
    }
}
