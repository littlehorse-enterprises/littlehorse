import { Label } from '../label'
import { Section } from '../section'
import { VariableMutation } from 'littlehorse-client/proto'
import { getVariable, getVariableValue } from '@/utils/data/variables'

export default function VariableMutationComponent(variableMutation: VariableMutation) {
  const rhsValueCase = variableMutation.rhsValue?.$case
  return (
    <Section title="VariableMutation">
      <Label label="LhsName">{variableMutation.lhsName}</Label>
      {variableMutation.lhsJsonPath && <Label label="LhsJsonPath">{variableMutation.lhsJsonPath}</Label>}
      <Label label="Operation">{variableMutation.operation}</Label>

      {rhsValueCase === 'rhsAssignment' && (
        <Label label="RhsAssignment">{getVariable(variableMutation.rhsValue?.rhsAssignment)}</Label>
      )}
      {rhsValueCase === 'literalValue' && (
        <Label label="LiteralValue">{getVariableValue(variableMutation.rhsValue?.literalValue)}</Label>
      )}
      {rhsValueCase === 'nodeOutput' && (
        <Section title="NodeOutput">
          <Label label="jsonPath">{variableMutation.rhsValue?.nodeOutput?.jsonpath}</Label>
        </Section>
      )}
    </Section>
  )
}
