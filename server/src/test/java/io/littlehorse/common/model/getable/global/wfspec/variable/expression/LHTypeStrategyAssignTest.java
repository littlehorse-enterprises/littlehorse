package io.littlehorse.common.model.getable.global.wfspec.variable.expression;

import static org.junit.jupiter.api.Assertions.*;

import io.littlehorse.common.exceptions.validation.InvalidExpressionException;
import io.littlehorse.common.model.getable.global.structdef.InlineArrayDefModel;
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

    @Test
    void arrayRejectsExtendWithMismatchedTypeAsValidationError() {
        // Array<INT> extended with a STR is invalid; it must surface as a checked
        // InvalidExpressionException (INVALID_ARGUMENT), not an UnsupportedOperationException
        // that would bubble up as an INTERNAL server error at PutWfSpec.
        ArrayReturnTypeStrategy arrayOfInt =
                new ArrayReturnTypeStrategy(new InlineArrayDefModel(new TypeDefinitionModel(VariableType.INT)));

        assertThrows(InvalidExpressionException.class, () -> arrayOfInt.extend(null, new StrReturnTypeStrategy()));
    }

    @Test
    void arrayAcceptsExtendWithElementType() throws Exception {
        ArrayReturnTypeStrategy arrayOfInt =
                new ArrayReturnTypeStrategy(new InlineArrayDefModel(new TypeDefinitionModel(VariableType.INT)));

        Optional<TypeDefinitionModel> out = arrayOfInt.extend(null, new IntReturnTypeStrategy());
        assertTrue(out.isPresent());
    }

    @Test
    void arrayRejectsAddAndSubtractAsValidationError() {
        ArrayReturnTypeStrategy arrayOfInt =
                new ArrayReturnTypeStrategy(new InlineArrayDefModel(new TypeDefinitionModel(VariableType.INT)));

        assertThrows(InvalidExpressionException.class, () -> arrayOfInt.add(null, new IntReturnTypeStrategy()));
        assertThrows(InvalidExpressionException.class, () -> arrayOfInt.subtract(null, new IntReturnTypeStrategy()));
    }
}
