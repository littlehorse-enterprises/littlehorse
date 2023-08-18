package io.littlehorse.server.metrics;

import io.micrometer.core.instrument.config.MeterFilterReply;
import java.util.List;

public final class ServerFilterRules {

    private ServerFilterRules() {}

    // relevance ordered
    public static final List<ServerFilterRule> RULES =
            List.of(
                    accept("kafka_stream_state_compaction_pending"),
                    accept("kafka_stream_state_write_stall"),
                    accept("kafka_stream_state_bytes"),
                    accept("kafka_stream_state_total_sst"),
                    accept("kafka_stream_state_live_sst"),
                    accept("kafka_stream_state_restoration"),
                    accept("kafka_consumer_coordinator_rebalance"),
                    accept("kafka_producer_request"),
                    accept("kafka_producer_outgoing_byte"),
                    accept("kafka_producer_record_error"),
                    deny("kafka_stream_state"),
                    deny("kafka_stream_task"),
                    deny("kafka_stream_processor"),
                    deny("kafka_producer"),
                    deny("kafka_consumer"),
                    deny("kafka_admin"));

    public static ServerFilterRule accept(String prefix) {
        return new ServerFilterRule(prefix, MeterFilterReply.ACCEPT);
    }

    public static ServerFilterRule deny(String prefix) {
        return new ServerFilterRule(prefix, MeterFilterReply.DENY);
    }
}
