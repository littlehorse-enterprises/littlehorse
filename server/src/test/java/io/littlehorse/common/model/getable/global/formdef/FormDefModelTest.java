package io.littlehorse.common.model.getable.global.formdef;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.Timestamp;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.outputtopic.MetadataOutputTopicRecordModel;
import io.littlehorse.sdk.common.proto.FormDef;
import io.littlehorse.sdk.common.proto.FormDefId;
import io.littlehorse.sdk.common.proto.FormFieldDef;
import io.littlehorse.sdk.common.proto.FormMetadataValue;
import io.littlehorse.sdk.common.proto.LHPath;
import io.littlehorse.sdk.common.proto.MetadataOutputTopicRecord;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.VariableValue;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class FormDefModelTest {

    private final FormDef proto = FormDef.newBuilder()
            .setId(FormDefId.newBuilder().setName("review").setVersion(2))
            .setResultStructDefId(StructDefId.newBuilder().setName("order").setVersion(1))
            .setCreatedAt(Timestamp.newBuilder().setSeconds(1_700_000_000))
            .putMetadata("title", literal("Review"))
            .addFields(FormFieldDef.newBuilder()
                    .setFieldPath(LHPath.newBuilder()
                            .addPath(LHPath.Selector.newBuilder().setKey("approved")))
                    .putMetadata(
                            "label",
                            FormMetadataValue.newBuilder()
                                    .setPlaceholder("greeting")
                                    .build())
                    .putMetadata("widget", literal("checkbox")))
            .build();

    @Test
    void shouldRoundTripThroughMetadataOutputTopic() {
        FormDefModel formDef = LHSerializable.fromProto(proto, FormDefModel.class, null);

        MetadataOutputTopicRecord record =
                new MetadataOutputTopicRecordModel(formDef).toProto().build();

        assertThat(record.getFormDef()).isEqualTo(proto);
    }

    @Test
    void shouldResolvePlaceholdersAndKeepLiterals() {
        FormDefModel formDef = LHSerializable.fromProto(proto, FormDefModel.class, null);

        var form = formDef.resolve(Map.of("greeting", new VariableValueModel("Hello Jake")))
                .toProto();

        assertThat(formDef.getPlaceholderNames()).containsExactly("greeting");
        assertThat(form.getFormDefId()).isEqualTo(proto.getId());
        assertThat(form.getMetadataOrThrow("title").getStr()).isEqualTo("Review");
        assertThat(form.getFields(0).getMetadataOrThrow("label").getStr()).isEqualTo("Hello Jake");
        assertThat(form.getFields(0).getMetadataOrThrow("widget").getStr()).isEqualTo("checkbox");
    }

    @Test
    void shouldOmitUnsuppliedPlaceholders() {
        FormDefModel formDef = LHSerializable.fromProto(proto, FormDefModel.class, null);

        var form = formDef.resolve(Map.of()).toProto();

        assertThat(form.getFields(0).getMetadataMap()).containsOnlyKeys("widget");
    }

    private static FormMetadataValue literal(String value) {
        return FormMetadataValue.newBuilder()
                .setLiteral(VariableValue.newBuilder().setStr(value))
                .build();
    }
}
