import { VariableType } from 'littlehorse-client/dist/proto/common_enums'

export const VARIABLE_TYPES: { [key in VariableType]: string } = {
  JSON_OBJ: 'JSON Object',
  JSON_ARR: 'JSON Array',
  DOUBLE: 'Double',
  BOOL: 'Boolean',
  STR: 'String',
  INT: 'Integer',
  BYTES: 'Bytes',
  UNRECOGNIZED: 'Unrecognized',
}
