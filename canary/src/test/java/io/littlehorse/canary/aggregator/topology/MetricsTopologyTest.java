package io.littlehorse.canary.aggregator.topology;

import io.littlehorse.canary.proto.*;

class MetricsTopologyTest {
    //    private TopologyTestDriver testDriver;
    //    private TestInputTopic<BeatKey, BeatValue> inputTopic;
    //    private KeyValueStore<MetricKey, Double> store;
    //
    //    private static BeatValue newTaskRunBeat() {
    //        return BeatValue.newBuilder()
    //                .setTime(Timestamps.now())
    //                .build();
    //    }
    //
    //    private static BeatKey newTaskRunBeatKey(
    //            String expectedHost, int expectedPort, String idempotencyKey, int attemptNumber) {
    //        return BeatKey.newBuilder()
    //                .setServerHost(expectedHost)
    //                .setServerPort(expectedPort)
    //                .build();
    //    }
    //
    //    private static Beat newLatencyBeat(int latency) {
    //        return Beat.newBuilder()
    //                .setTime(Timestamps.now())
    //                .setLatencyBeat(LatencyBeat.newBuilder().setLatency(latency))
    //                .build();
    //    }
    //
    //    private static BeatKey newLatencyBeatKey(String expectedHost, int expectedPort, String expectedMetricName) {
    //        return BeatKey.newBuilder()
    //                .setServerHost(expectedHost)
    //                .setServerPort(expectedPort)
    //                .setLatencyBeatKey(LatencyBeatKey.newBuilder().setName(expectedMetricName))
    //                .build();
    //    }
    //
    //    private static MetricKey newMetricKey(String expectedHost, int expectedPort, String expectedMetricName) {
    //        return MetricKey.newBuilder()
    //                .setServerHost(expectedHost)
    //                .setServerPort(expectedPort)
    //                .setId(expectedMetricName)
    //                .build();
    //    }
    //
    //    @BeforeEach
    //    void beforeEach() throws IOException {
    //        String inputTopicName = "metrics";
    //
    //        Consumed<MetricKey, Double> serdes = Consumed.with(ProtobufSerdes.MetricKey(), Serdes.Double());
    //
    //        MetricsTopology metricsTopology =
    //                new MetricsTopology(inputTopicName, Duration.ofMinutes(2).toMillis());
    //
    //        Properties properties = new Properties();
    //        properties.put(
    //                StreamsConfig.STATE_DIR_CONFIG,
    //                Files.createTempDirectory("canaryStreamUnitTest").toString());
    //
    //        testDriver = new TopologyTestDriver(metricsTopology.toTopology(), properties);
    //        inputTopic = testDriver.createInputTopic(
    //                inputTopicName,
    //                ProtobufSerdes.BeatKey().serializer(),
    //                ProtobufSerdes.Beat().serializer());
    //
    //        store = testDriver.getKeyValueStore(METRICS_STORE);
    //    }
    //
    //    @AfterEach
    //    void afterEach() {
    //        testDriver.close();
    //    }
    //
    //    @Test
    //    void calculateLatency() {
    //        String metricName = "my_metric";
    //        String host = "localhost";
    //        int port = 2023;
    //
    //        BeatKey beatKey = newLatencyBeatKey(host, port, metricName);
    //
    //        List<Beat> beats = List.of(newLatencyBeat(20), newLatencyBeat(40), newLatencyBeat(10),
    // newLatencyBeat(10));
    //        beats.forEach(metric -> inputTopic.pipeInput(beatKey, metric));
    //
    //        assertThat(store.get(newMetricKey(host, port, metricName + "_avg"))).isEqualTo(20.);
    //        assertThat(store.get(newMetricKey(host, port, metricName + "_max"))).isEqualTo(40.);
    //    }
    //
    //    @Test
    //    void countDuplicated() {
    //        String host = "localhost";
    //        int port = 2023;
    //
    //        BeatKey beatKey = newTaskRunBeatKey(host, port, UUID.randomUUID().toString(), 0);
    //
    //        List<Beat> beats = List.of(newTaskRunBeat(), newTaskRunBeat());
    //        beats.forEach(metric -> inputTopic.pipeInput(beatKey, metric));
    //
    //        assertThat(store.get(newMetricKey(host, port, "duplicated_task_run_max_count")))
    //                .isEqualTo(1.);
    //    }
    //
    //    @Test
    //    void countDuplicatedForTwoTasks() {
    //        String host = "localhost";
    //        int port = 2023;
    //
    //        BeatKey beatKey1 = newTaskRunBeatKey(host, port, UUID.randomUUID().toString(), 0);
    //        BeatKey beatKey2 = newTaskRunBeatKey(host, port, UUID.randomUUID().toString(), 0);
    //
    //        List<Beat> beats = List.of(newTaskRunBeat(), newTaskRunBeat());
    //        beats.forEach(metric -> inputTopic.pipeInput(beatKey1, metric));
    //        beats.forEach(metric -> inputTopic.pipeInput(beatKey2, metric));
    //
    //        assertThat(store.get(newMetricKey(host, port, "duplicated_task_run_max_count")))
    //                .isEqualTo(2.);
    //    }
    //
    //    @Test
    //    void countDuplicatedForTwoServers() {
    //        String host1 = "localhost";
    //        String host2 = "localhost2";
    //        int port1 = 2023;
    //        int port2 = 2024;
    //
    //        BeatKey beatKey1 = newTaskRunBeatKey(host1, port1, UUID.randomUUID().toString(), 0);
    //        BeatKey beatKey2 = newTaskRunBeatKey(host2, port2, UUID.randomUUID().toString(), 0);
    //
    //        List<Beat> beats = List.of(newTaskRunBeat(), newTaskRunBeat());
    //        beats.forEach(metric -> inputTopic.pipeInput(beatKey1, metric));
    //        beats.forEach(metric -> inputTopic.pipeInput(beatKey2, metric));
    //
    //        assertThat(store.get(newMetricKey(host1, port1, "duplicated_task_run_max_count")))
    //                .isEqualTo(1.);
    //        assertThat(store.get(newMetricKey(host2, port2, "duplicated_task_run_max_count")))
    //                .isEqualTo(1.);
    //    }
}
