package io.littlehorse.common.model.getable.global.wfspec.variable.expression;

import static org.junit.jupiter.api.Assertions.*;

import io.littlehorse.common.exceptions.validation.InvalidExpressionException;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.sdk.common.proto.VariableType;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class LHTypeStrategyAssignTest {

    @Test
    void doubleAcceptsIntAssign() throws Exception {
        DoubleReturnTypeStrategy doubleStrat = new DoubleReturnTypeStrategy();
        IntReturnTypeStrategy intStrat = new IntReturnTypeStrategy();

        Optional<TypeDefinitionModel> out = doubleStrat.assign(intStrat);
        assertTrue(out.isPresent());
        assertEquals(VariableType.DOUBLE, out.get().getPrimitiveType());
    }

    @Test
    void intRejectsDoubleAssign() {
        IntReturnTypeStrategy intStrat = new IntReturnTypeStrategy();
        DoubleReturnTypeStrategy doubleStrat = new DoubleReturnTypeStrategy();

        assertThrows(InvalidExpressionException.class, () -> intStrat.assign(doubleStrat));
    }

    @Test
    void typeDefinitionCompatibilityDirectCheck() {
        TypeDefinitionModel targetDouble = new TypeDefinitionModel(VariableType.DOUBLE);
        TypeDefinitionModel sourceInt = new TypeDefinitionModel(VariableType.INT);

        assertTrue(targetDouble.isCompatibleWith(sourceInt));
        assertFalse(new TypeDefinitionModel(VariableType.INT)
                .isCompatibleWith(new TypeDefinitionModel(VariableType.DOUBLE)));
    }
}
