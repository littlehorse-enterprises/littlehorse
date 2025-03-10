package io.littlehorse.common.model.getable.objectId;

import io.littlehorse.sdk.common.proto.MetricType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MetricIdTest {

    @Test
    public void shouldSerializeToStringForWfSpecMetrics() {
        WfSpecIdModel wfSpecId = new WfSpecIdModel("test-wf", 0, 1);
        MetricSpecIdModel expected = new MetricSpecIdModel(wfSpecId, MetricType.AVG);
        MetricSpecIdModel deserialized = new MetricSpecIdModel();
        deserialized.initFromString(expected.toString());
        Assertions.assertEquals(expected, deserialized);
    }

    @Test
    public void shouldSerializeToStringForMeasurableObject() {
        WfSpecIdModel wfSpecId = new WfSpecIdModel("test-wf", 0, 1);
        MetricSpecIdModel expected = new MetricSpecIdModel(wfSpecId, MetricType.AVG);
        MetricSpecIdModel deserialized = new MetricSpecIdModel();
        deserialized.initFromString(expected.toString());
        Assertions.assertEquals(expected, deserialized);
    }
}
