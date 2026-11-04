package io.littlehorse.sdk.wfsdk;

import java.io.Serializable;

/**
 * Marker interface representing a spawned child workflow handle.
 */
public interface SpawnedChildWf {

    /** Sets the ID assigned to the child WfRun. */
    SpawnedChildWf withChildId(Serializable childId);
}
