package io.littlehorse.server.streamsbackend.storeinternals.index;

import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public abstract class DiscreteTag {

    protected List<Pair<String, String>> attributes;

    public DiscreteTag() {}
}
