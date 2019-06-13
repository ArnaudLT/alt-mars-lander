package org.arnaudlt.api;

public interface MarsLander {


    Shuttle simulateOutcome(Surface surface, Shuttle initialShuttle, ControlChain requested);

}
