package io.littlehorse.server.monitoring.metrics;

import io.micrometer.core.instrument.config.MeterFilterReply;
import java.util.List;

public final class ServerFilterRules {

    private ServerFilterRules() {}

    // relevance ordered
    public static final List<ServerFilterRule> INFO_RULES = List.of(
            // TODO: Wait for KIP-869 and gather state restoration metrics.
            accept("lh_in_memory_task_queue_size"),
            accept("lh_cache_size"),

            // Kafka Streams State Stuff
            accept("kafka_stream_state_compaction_pending"),
            accept("kafka_stream_state_write_stall"),
            accept("kafka_stream_state_bytes"),
            accept("kafka_stream_state_total_sst"),
            accept("kafka_stream_state_live_sst"),
            accept("kafka_stream_state_put_latency"),
            accept("kafka_stream_state_get_latency"),

            // Kafka Consumer Metrics
            accept("kafka_consumer_coordinator_rebalance"),
            accept("kafka_consumer_coordinator_last_rebalance_seconds_ago"),
            accept("kafka_consumer_incoming_byte_rate"),
            accept("kafka_consumer_fetch_manager_records_lag"),
            accept("kafka_consumer_fetch_manager_fetch_throttle_time"),
            accept("kafka_consumer_fetch_manager_fetch_latency_avg"),
            accept("kafka_consumer_request"),
            accept("kafka_consumer_time_between_poll"),

            // Producer Metrics
            accept("kafka_producer_batch_size"),
            accept("kafka_producer_request"),
            accept("kafka_producer_outgoing_byte"),
            accept("kafka_producer_record_error"),
            accept("kafka_stream_processor_record_e2e"),

            // Filter metrics to reduce
            deny("kafka_stream_state_"),
            accept("kafka_stream_state"),
            deny("kafka_stream_task"),
            deny("kafka_stream_processor"),
            deny("kafka_producer"),
            deny("kafka_consumer"),
            deny("kafka_admin"));

    // relevance ordered
    public static final List<ServerFilterRule> DEBUG_RULES = List.of(
            // TODO: Wait for KIP-869 and gather state restoration metrics.
            accept("lh_in_memory_task_queue_size"),
            accept("lh_cache_size"),

            // Kafka Streams State Stuff
            accept("kafka_stream_state_compaction_pending"),
            accept("kafka_stream_state_write_stall"),
            accept("kafka_stream_state_bytes"),
            accept("kafka_stream_state_total_sst"),
            accept("kafka_stream_state_live_sst"),
            accept("kafka_stream_state_put_latency"),
            accept("kafka_stream_state_get_latency"),

            // Kafka Consumer Metrics
            accept("kafka_consumer_coordinator_rebalance"),
            accept("kafka_consumer_coordinator_last_rebalance_seconds_ago"),
            accept("kafka_consumer_incoming_byte_rate"),
            accept("kafka_consumer_fetch_manager_records_lag"),
            accept("kafka_consumer_fetch_manager_fetch_throttle_time"),
            accept("kafka_consumer_fetch_manager_fetch_latency_avg"),
            accept("kafka_consumer_request"),
            accept("kafka_consumer_time_between_poll"),

            // Producer Metrics
            accept("kafka_producer_batch_size"),
            accept("kafka_producer_request"),
            accept("kafka_producer_outgoing_byte"),
            accept("kafka_producer_record_error"),

            // Filter metrics to reduce
            deny("kafka_stream_state"),
            accept("kafka_stream_state_"),
            accept("kafka_stream_task"),
            deny("kafka_stream_processor"),
            deny("kafka_producer"),
            deny("kafka_consumer"),
            deny("kafka_admin"));

    public static final List<ServerFilterRule> TRACE_RULES = List.of();

    public static ServerFilterRule accept(String prefix) {
        return new ServerFilterRule(prefix, MeterFilterReply.ACCEPT);
    }

    public static ServerFilterRule deny(String prefix) {
        return new ServerFilterRule(prefix, MeterFilterReply.DENY);
    }

    public static List<ServerFilterRule> fromLevel(String level) {
        if (level.equals("TRACE")) {
            return TRACE_RULES;
        } else if (level.equals("DEBUG")) {
            return DEBUG_RULES;
        } else {
            return INFO_RULES;
        }
    }
}
