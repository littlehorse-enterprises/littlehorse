package io.littlehorse.server;

import org.apache.kafka.streams.Topology;
import io.littlehorse.common.LHConfig;

public class ServerTopology {
    public static String wfSpecSource = "wfSpecSource";
    public static String taskDefSource = "taskDefSource";
    public static String wfRunSource = "wfRunSource";

    public static Topology initTopology(LHConfig config) {
        Topology topo = new Topology();

        return topo;
    }
}
