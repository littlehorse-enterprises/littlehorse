import { ThreadVarDef } from "littlehorse-client/proto"
import { FieldValues, FormState, UseFormRegister } from "react-hook-form"

export type FormFieldProp = {
  variables?: ThreadVarDef
  custom?: boolean
  formState: FormState<FieldValues>
  register: UseFormRegister<FieldValues>
}
