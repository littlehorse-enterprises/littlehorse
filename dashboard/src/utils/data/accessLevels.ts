import { WfRunVariableAccessLevel } from 'littlehorse-client/proto'

export const accessLevelLabels: { [key in WfRunVariableAccessLevel]: string } = {
  PUBLIC_VAR: 'Public',
  INHERITED_VAR: 'Inherited',
  PRIVATE_VAR: 'Private',
  UNRECOGNIZED: '',
}
