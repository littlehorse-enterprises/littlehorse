import { formatTime } from '../../../../../utils'
import { VariableAssignment, VariableValue } from 'littlehorse-client/proto'

const renderVariableValue = (value: VariableValue) => {
  if (value.jsonObj) return <span>{value.jsonObj}</span>
  if (value.jsonArr) return <span>{value.jsonArr}</span>
  if (value.double !== undefined) return <span>{formatTime(value.double)}</span>
  if (value.bool !== undefined) return <span>{value.bool.toString()}</span>
  if (value.str) return <span>{value.str}</span>
  if (value.int !== undefined) return <span> {formatTime(value.int)}</span>

  return null
}

export const renderVariableAssignment = (assignment: VariableAssignment) => {
  return (
    <div>
      {assignment.jsonPath && <span>Time: {assignment.jsonPath}</span>}
      {assignment.variableName && <span>Time (Variable Name): {assignment.variableName}</span>}
      {assignment.literalValue && <span>Time: {renderVariableValue(assignment.literalValue)}</span>}
    </div>
  )
}
