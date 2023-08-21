package io.littlehorse.server.streams.util;

import io.littlehorse.common.model.getable.core.taskworkergroup.HostModel;
import java.util.Optional;
import java.util.Set;

public class InternalHosts {

    private Set<HostModel> previousHosts;
    private Set<HostModel> currentHosts;

    public InternalHosts(Set<HostModel> previousHosts, Set<HostModel> currentHosts) {
        this.previousHosts = Optional.ofNullable(previousHosts).orElse(Set.of());
        this.currentHosts = Optional.ofNullable(currentHosts).orElse(Set.of());
    }

    public boolean hasChanges() {
        return !previousHosts.equals(currentHosts);
    }

    public Set<HostModel> getHosts() {
        return Set.copyOf(currentHosts);
    }
}
