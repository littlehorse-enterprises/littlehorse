import { Label } from "../label";
import { Section } from "../section";
import { VariableMutation } from "littlehorse-client/proto";
import { getVariable, getVariableValue } from "@/utils/data/variables";

export default function VariableMutationComponent(variableMutation: VariableMutation) {
    return <Section title="VariableMutation">
        <Label label="LhsName">
            {variableMutation.lhsName}
        </Label>
        {variableMutation.lhsJsonPath && <Label label="LhsJsonPath">{variableMutation.lhsJsonPath}</Label>}
        <Label label="Operation">{variableMutation.operation}</Label>

        {variableMutation.rhsAssignment && <Label label="RhsAssignment">
            {getVariable(variableMutation.rhsAssignment)}
        </Label>}
        {variableMutation.literalValue && <Label label="LiteralValue">
            {getVariableValue(variableMutation.literalValue)}
        </Label>}
        {variableMutation.nodeOutput && <Section title="NodeOutput">
            <Label label="jsonPath">{variableMutation.nodeOutput.jsonpath}</Label>
        </Section>}
    </Section>
}