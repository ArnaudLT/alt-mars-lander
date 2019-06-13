package org.arnaudlt.core;

import java.util.Arrays;
import org.arnaudlt.api.Control;
import org.arnaudlt.api.ControlChain;
import org.arnaudlt.api.Point;
import org.arnaudlt.api.Position;
import org.arnaudlt.api.Shuttle;
import org.arnaudlt.api.Speed;
import org.arnaudlt.api.Status;
import org.arnaudlt.api.Surface;
import org.junit.Assert;
import org.junit.Test;

public class MarsLanderImplTest {

    private static final double EPSILON = 0.000_000_1;

    @Test
    public void simulateOutcomeFreeFallTest() {

        MarsLanderImpl mars = new MarsLanderImpl();
        Surface surface = getBasicSurface();
        Shuttle shuttle = getStaticShuttle();
        Control requested = new Control(0, 0);
        Shuttle termShuttle = mars.simulateOutcome(surface, shuttle,
            new ControlChain(Arrays.asList(requested, requested, requested)));

        Assert.assertEquals(500, termShuttle.getPosition().getX(), EPSILON);
        Assert.assertEquals(483.3004999, termShuttle.getPosition().getY(), EPSILON);
        Assert.assertEquals(0.0, termShuttle.getSpeed().getHorizontalSpeed(), EPSILON);
        Assert.assertEquals(-11.133, termShuttle.getSpeed().getVerticalSpeed(), EPSILON);
        Assert.assertEquals(200, termShuttle.getFuel());
        Assert.assertEquals(0.0, termShuttle.getControl().getRotate(), EPSILON);
        Assert.assertEquals(0.0, termShuttle.getControl().getPower(), EPSILON);
        Assert.assertEquals(Status.FLYING, termShuttle.getStatus());
    }


    @Test
    public void simulateOutcomeFreeFallToCrashTest() {

        MarsLanderImpl mars = new MarsLanderImpl();
        Surface surface = getBasicSurface();
        Shuttle shuttle = getStaticShuttle();
        Control requested = new Control(0, 0);
        Shuttle termShuttle = mars.simulateOutcome(surface, shuttle,
            new ControlChain(Arrays.asList(requested, requested, requested, requested, requested, requested, requested, requested, requested, requested, requested, requested, requested, requested, requested, requested, requested)));
        System.out.println("termShuttle = " + termShuttle);
    }

    private Shuttle getStaticShuttle() {

        return new Shuttle(
            new Position(500, 500),
            new Speed(0, 0),
            200,
            new Control(0, 0),
            Status.FLYING
        );
    }

    private Surface getBasicSurface() {

        return new Surface(Arrays.asList(
            new Point(0,0), new Point(3999, 0)
        ));
    }

}
