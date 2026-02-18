package io.littlehorse.common.model.getable.global.wfspec.node;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.common.model.getable.objectId.StructDefIdModel;
import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.VariableType;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class LegacyEdgeConditionModelTest {
    @ParameterizedTest
    @MethodSource("provideEdgeConditionArguments")
    public void shouldPedro(Object lhs, Comparator comparator, Object rhs, boolean isValid) {
        TypeDefinitionModel lhsType = LegacyEdgeConditionModelTest.getTypeDefinitionFromObject(lhs);
        TypeDefinitionModel rhsType = LegacyEdgeConditionModelTest.getTypeDefinitionFromObject(rhs);

        Optional<String> expectedErrorMessage =
                LegacyEdgeConditionModel.checkTypeComparisonIncompatibility(lhsType, comparator, rhsType);

        assertThat(expectedErrorMessage.isEmpty()).isEqualTo(isValid);
    }

    private static Stream<Arguments> provideEdgeConditionArguments() {
        return Stream.of(
                Arguments.of(VariableType.BOOL, Comparator.EQUALS, VariableType.BOOL, true),
                Arguments.of(VariableType.BOOL, Comparator.EQUALS, VariableType.TIMESTAMP, true),
                Arguments.of(VariableType.BOOL, Comparator.GREATER_THAN, VariableType.INT, true),
                Arguments.of(VariableType.BOOL, Comparator.EQUALS, VariableType.JSON_OBJ, false),
                Arguments.of(VariableType.BOOL, Comparator.IN, VariableType.INT, false),
                Arguments.of(VariableType.STR, Comparator.IN, VariableType.JSON_OBJ, true),
                Arguments.of(VariableType.BOOL, Comparator.IN, VariableType.JSON_OBJ, false),
                Arguments.of(VariableType.INT, Comparator.IN, VariableType.JSON_ARR, true),
                Arguments.of(VariableType.STR, Comparator.IN, new StructDefIdModel(), true),
                Arguments.of(VariableType.INT, Comparator.IN, new StructDefIdModel(), false),
                Arguments.of(VariableType.JSON_OBJ, Comparator.EQUALS, VariableType.JSON_OBJ, true),
                Arguments.of(VariableType.JSON_OBJ, Comparator.LESS_THAN, VariableType.JSON_OBJ, false),
                Arguments.of(VariableType.TIMESTAMP, Comparator.EQUALS, VariableType.TIMESTAMP, true),
                Arguments.of(VariableType.TIMESTAMP, Comparator.LESS_THAN, VariableType.TIMESTAMP, true),
                Arguments.of(VariableType.TIMESTAMP, Comparator.LESS_THAN, VariableType.DOUBLE, true),
                Arguments.of(VariableType.TIMESTAMP, Comparator.LESS_THAN, VariableType.BOOL, true),
                Arguments.of(VariableType.JSON_ARR, Comparator.EQUALS, VariableType.JSON_ARR, true),
                Arguments.of(VariableType.JSON_ARR, Comparator.LESS_THAN, VariableType.JSON_ARR, false),
                Arguments.of(VariableType.JSON_OBJ, Comparator.LESS_THAN, VariableType.JSON_ARR, false),
                Arguments.of(VariableType.STR, Comparator.EQUALS, null, true),
                Arguments.of(new StructDefIdModel(), Comparator.EQUALS, new StructDefIdModel(), true),
                Arguments.of(new StructDefIdModel("car", 0), Comparator.EQUALS, new StructDefIdModel("car", 0), true),
                Arguments.of(
                        new StructDefIdModel("car", 0), Comparator.EQUALS, new StructDefIdModel("duck", 1), false));
    }

    private static TypeDefinitionModel getTypeDefinitionFromObject(Object typeObject) {
        if (typeObject instanceof VariableType) {
            VariableType variableType = (VariableType) typeObject;
            return new TypeDefinitionModel(variableType);
        } else if (typeObject instanceof StructDefIdModel) {
            StructDefIdModel structDefIdModel = (StructDefIdModel) typeObject;
            return new TypeDefinitionModel(structDefIdModel);
        }
        return new TypeDefinitionModel();
    }
}
