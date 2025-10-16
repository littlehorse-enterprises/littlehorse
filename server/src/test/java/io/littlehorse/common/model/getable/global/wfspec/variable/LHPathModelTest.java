package io.littlehorse.common.model.getable.global.wfspec.variable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import io.littlehorse.sdk.common.proto.LHPath;
import io.littlehorse.sdk.common.proto.LHPath.Selector;
import org.junit.jupiter.api.Test;

public class LHPathModelTest {
    @Test
    public void shouldCreateJsonPathStrFromIndexSelector() {
        LHPath lhPath = LHPath.newBuilder()
                .addPath(Selector.newBuilder().setIndex(0).build())
                .build();

        LHPathModel lhPathModel = LHPathModel.fromProto(lhPath, mock());

        String actualJsonPathStr = lhPathModel.toJsonPathStr();
        String expectedJsonPathStr = "$[0]";

        assertThat(actualJsonPathStr).isEqualTo(expectedJsonPathStr);
    }

    @Test
    public void shouldCreateJsonPathStrFromKeySelector() {
        LHPath lhPath = LHPath.newBuilder()
                .addPath(Selector.newBuilder().setKey("car").build())
                .build();

        LHPathModel lhPathModel = LHPathModel.fromProto(lhPath, mock());

        String actualJsonPathStr = lhPathModel.toJsonPathStr();
        String expectedJsonPathStr = "$.car";

        assertThat(actualJsonPathStr).isEqualTo(expectedJsonPathStr);
    }

    @Test
    public void shouldCreateJsonPathStrFromSelectorList() {
        LHPath lhPath = LHPath.newBuilder()
                .addPath(Selector.newBuilder().setKey("car").build())
                .addPath(Selector.newBuilder().setKey("features").build())
                .addPath(Selector.newBuilder().setIndex(10).build())
                .addPath(Selector.newBuilder().setKey("other").build())
                .build();

        LHPathModel lhPathModel = LHPathModel.fromProto(lhPath, mock());

        String actualJsonPathStr = lhPathModel.toJsonPathStr();
        String expectedJsonPathStr = "$.car.features[10].other";

        assertThat(actualJsonPathStr).isEqualTo(expectedJsonPathStr);
    }
}
