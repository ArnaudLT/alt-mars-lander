package org.arnaudlt.core;

import org.arnaudlt.api.Control;
import org.arnaudlt.api.ControlChain;
import org.arnaudlt.api.MarsLander;
import org.arnaudlt.api.Shuttle;
import org.arnaudlt.api.Status;
import org.arnaudlt.api.Surface;

public class MarsLanderImpl implements MarsLander {


    @Override
    public Shuttle simulateOutcome(Surface surface, Shuttle initialShuttle, ControlChain requested) {

        MutableShuttle currentShuttle = new MutableShuttle(initialShuttle);
        MutableSurface mutableSurface = new MutableSurface(surface);

        for (Control ctrl : requested.getControls()) {

            simulateOutcome(mutableSurface, currentShuttle, ctrl);
        }

        return currentShuttle.buildImmutableShuttle();
    }


    private void simulateOutcome(MutableSurface surface, MutableShuttle shuttle, Control ctrl) {

        // 1 - Compute control
        if ( ctrl.getPower() > shuttle.control.power ) {
            shuttle.control.power++;
        } else if ( ctrl.getPower() < shuttle.control.power ) {
            shuttle.control.power--;
        }

        if ( ctrl.getRotate() > shuttle.control.rotate ) {
            shuttle.control.rotate = Math.min(ctrl.getRotate(), shuttle.control.rotate + 15);
        } else if ( ctrl.getRotate() < shuttle.control.rotate ) {
            shuttle.control.rotate = Math.max(ctrl.getRotate(), shuttle.control.rotate-15);
        }

        // 2 - Compute remaining fuel
        shuttle.fuel -= shuttle.control.power;

        // 3 - Compute new position
        MutablePosition prevPosition = new MutablePosition(shuttle.position.x, shuttle.position.y);
        shuttle.position.x = shuttle.position.x + shuttle.speed.horizontalSpeed
            - 0.5 * (Math.sin(Math.toRadians(shuttle.control.rotate)) * shuttle.control.power);
        shuttle.position.y = shuttle.position.y + shuttle.speed.verticalSpeed
            + 0.5 * (Math.cos(Math.toRadians(shuttle.control.rotate)) * shuttle.control.power - 3.711);

        // 4 - Compute new speed
        shuttle.speed.horizontalSpeed = shuttle.speed.horizontalSpeed
            - Math.sin(Math.toRadians(shuttle.control.rotate)) * shuttle.control.power;
        shuttle.speed.verticalSpeed = shuttle.speed.verticalSpeed
            + Math.cos(Math.toRadians(shuttle.control.rotate)) * shuttle.control.power - 3.711;

        // 5 Compute new status
        shuttle.status = Status.FLYING; // TODO integrate ground and collision.

        if ( surface.collideWithSurface(prevPosition, shuttle.position) ) {
            if ( respectLandingConstraints(surface, shuttle) ) {
                shuttle.status = Status.LANDED;
            } else {
                shuttle.status = Status.CRASHED;
            }
        }

    }


    private boolean respectLandingConstraints(MutableSurface surface, MutableShuttle shuttle) {

        return (
             Math.abs(shuttle.speed.horizontalSpeed) < 20 &&
             Math.abs(shuttle.speed.verticalSpeed) < 40 &&
             shuttle.control.rotate == 0 &&
             surface.flatZone.start.x <= shuttle.position.x &&
             surface.flatZone.end.x >= shuttle.position.x);
    }
}
