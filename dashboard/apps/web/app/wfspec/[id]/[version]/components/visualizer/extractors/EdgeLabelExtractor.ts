import { Comparator } from '../../../../../../../littlehorse-public-api/common_wfspec'
import type { Edge, EdgeCondition } from '../../../../../../../littlehorse-public-api/wf_spec'

enum Side {
    LEFT = 'left',
    RIGHT = 'right'
}

const signPerOperator = {
    [Comparator.LESS_THAN]: '<',
    [Comparator.LESS_THAN_EQ]: '<=',
    [Comparator.GREATER_THAN]: '>',
    [Comparator.GREATER_THAN_EQ]: '>=',
    [Comparator.EQUALS]: '=',
    [Comparator.NOT_EQUALS]: '!=',
    [Comparator.IN]: 'IN',
    [Comparator.NOT_IN]: 'NOT IN'
}

const extract = (edge: Edge): string => {
    const edgeCondition = edge.condition

    if (edgeCondition === undefined) {
        return ''
    }

    const conditionLeftSide = edgeCondition.left
    const conditionRightSide = edgeCondition.right

    const hasVariablesOnBothSides = conditionLeftSide?.variableName !== undefined && conditionRightSide?.variableName !== undefined

    if (hasVariablesOnBothSides) {
        return buildLabelForConditionalWithVariablesOnBothSides(edgeCondition)
    }

    const hasVariableOnlyOnTheLeftSide = conditionLeftSide?.variableName !== undefined && conditionRightSide?.variableName === undefined
    if (hasVariableOnlyOnTheLeftSide) {
        return buildLabelForConditionalWithVariablesOnTheLeftSide(edgeCondition)
    }

    const literalValueOnBothSides = conditionLeftSide?.literalValue !== undefined && conditionRightSide?.literalValue !== undefined
    if (literalValueOnBothSides) {
        return buildLabelForConditionalWithLiteralValuesOnBothSides(edgeCondition)
    }

    return buildLabelForConditionalWithVariableOnlyOnTheRightSide(edgeCondition)
}

const buildLabelForConditionalWithVariablesOnBothSides = (edgeCondition: EdgeCondition): string => {
    const conditionLeftSide = edgeCondition.left
    const conditionRightSide = edgeCondition.right

    const jsonPathOnBothSides = conditionLeftSide?.jsonPath !== undefined && conditionRightSide?.jsonPath !== undefined

    if (jsonPathOnBothSides) {
        return `${conditionLeftSide?.jsonPath} within ${conditionLeftSide.variableName} ${signPerOperator[edgeCondition.comparator]} ${conditionRightSide?.jsonPath} within ${conditionRightSide.variableName}`
    }

    return `${conditionLeftSide?.variableName} ${signPerOperator[edgeCondition.comparator]} ${conditionRightSide?.variableName}`
}

const buildLabelForConditionalWithVariablesOnTheLeftSide = (edgeCondition: EdgeCondition): string => {
    const jsonPathOnTheLeftSide = edgeCondition.left?.jsonPath !== undefined
    const conditionLeftSide = edgeCondition.left

    if (jsonPathOnTheLeftSide) {
        return `${conditionLeftSide?.jsonPath} within ${conditionLeftSide?.variableName} ${signPerOperator[edgeCondition.comparator]} ${getLiteralValue(edgeCondition, Side.RIGHT)}`
    }

    return `${conditionLeftSide?.variableName} ${signPerOperator[edgeCondition.comparator]} ${getLiteralValue(edgeCondition, Side.RIGHT)}`
}

const buildLabelForConditionalWithLiteralValuesOnBothSides = (edgeCondition: EdgeCondition): string => {
    return `${getLiteralValue(edgeCondition, Side.LEFT)} ${signPerOperator[edgeCondition.comparator]} ${getLiteralValue(edgeCondition, Side.RIGHT)}`
}

const buildLabelForConditionalWithVariableOnlyOnTheRightSide = (edgeCondition: EdgeCondition): string => {
    if (edgeCondition.right?.jsonPath !== undefined) {
        return `${getLiteralValue(edgeCondition, Side.LEFT)} ${signPerOperator[edgeCondition.comparator]} ${edgeCondition.right?.jsonPath} within ${edgeCondition.right.variableName}`
    }

    return `${getLiteralValue(edgeCondition, Side.LEFT)} ${signPerOperator[edgeCondition.comparator]} ${edgeCondition.right?.variableName}`
}

const getLiteralValue = (edgeCondition: EdgeCondition, side: Side) => {
    const isItANumericalComparator = edgeCondition.comparator === Comparator.GREATER_THAN ||
        edgeCondition.comparator === Comparator.GREATER_THAN_EQ ||
        edgeCondition.comparator === Comparator.LESS_THAN ||
        edgeCondition.comparator === Comparator.LESS_THAN_EQ ||
        edgeCondition.comparator === Comparator.EQUALS ||
        edgeCondition.comparator === Comparator.NOT_EQUALS

    if (isItANumericalComparator) {
        return getLiteralValueForNumericalComparator(edgeCondition, side)
    }

    return getLiteralValueForInConditional(edgeCondition, side)
}

const getLiteralValueForInConditional = (edgeCondition: EdgeCondition, side: Side) => {
    if (edgeCondition[side]?.literalValue?.jsonArr !== undefined) {
        return edgeCondition[side]?.literalValue?.jsonArr
    }

    if (edgeCondition[side]?.literalValue?.str !== undefined) {
        return edgeCondition[side]?.literalValue?.str
    }

    if (edgeCondition[side]?.literalValue?.jsonObj !== undefined) {
        return edgeCondition[side]?.literalValue?.jsonObj
    }

    if (edgeCondition[side]?.literalValue?.double !== undefined) {
        return edgeCondition[side]?.literalValue?.double
    }

    return edgeCondition[side]?.literalValue?.int
}

const getLiteralValueForNumericalComparator = (edgeCondition: EdgeCondition, side: Side) => {
    if (edgeCondition[side]?.literalValue?.bool !== undefined) {
        return edgeCondition[side]?.literalValue?.bool
    }

    if (edgeCondition[side]?.literalValue?.int !== undefined) {
        return edgeCondition[side]?.literalValue?.int
    }

    if (edgeCondition[side]?.literalValue?.double !== undefined) {
        return edgeCondition[side]?.literalValue?.double
    }

    if (edgeCondition[side]?.literalValue?.str === undefined) {
        return 'NULL'
    }

    return edgeCondition[side]?.literalValue?.str
}

const EdgeLabelExtractor = {
    extract
}

export default EdgeLabelExtractor
