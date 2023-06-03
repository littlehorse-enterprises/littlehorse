package io.littlehorse.server.streamsimpl.util;

import io.littlehorse.common.model.meta.Host;
import java.util.Optional;
import java.util.Set;

public class InternalHosts {

    private Set<Host> previousHosts;
    private Set<Host> currentHosts;

    public InternalHosts(Set<Host> previousHosts, Set<Host> currentHosts) {
        this.previousHosts = Optional.ofNullable(previousHosts).orElse(Set.of());
        this.currentHosts = Optional.ofNullable(currentHosts).orElse(Set.of());
    }

    public boolean hasChanges() {
        return !previousHosts.equals(currentHosts);
    }

    public Set<Host> getHosts() {
        return Set.copyOf(currentHosts);
    }
}
