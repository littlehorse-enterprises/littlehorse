import { getVariableDefType, wfRunIdFromFlattenedId, wfRunIdToPath } from '@/app/utils'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { RunWfRequest, ThreadVarDef, VariableValue, WfRunVariableAccessLevel, WfSpec } from 'littlehorse-client/proto'
import { useParams, useRouter } from 'next/navigation'
import { FC, useMemo, useRef } from 'react'
import { toast } from 'sonner'
import { Modal } from '../../context'
import { useModal } from '../../hooks/useModal'
import { runWfSpec } from '../../wfSpec/[...props]/actions/runWfSpec'
import { FormValues, WfRunForm } from '../Forms/WfRunForm'

export const DOT_REPLACEMENT_PATTERN = '*-/:DOT_REPLACE_PATTERN'
export const STRUCT_FIELD_DATA_SEPARATOR_PATTERN = '*-/:FIELD_DATA_SEPARATOR_PATTERN'
export const STRUCT_PATH_SEPARATOR = '*-/:STRUCT_PATH_SEPARATOR_PATTERN'

export const ExecuteWorkflowRun: FC<Modal<WfSpec>> = ({ data: wfSpec }) => {
  const { showModal, setShowModal } = useModal()
  const tenantId = useParams().tenantId as string
  const formRef = useRef<HTMLFormElement | null>(null)
  const router = useRouter()
  const wfSpecVariables = useMemo(() => {
    if (!wfSpec.threadSpecs[wfSpec.entrypointThreadName]) return []
    return (
      wfSpec.threadSpecs[wfSpec.entrypointThreadName].variableDefs.map(variable => {
        const newVariable = { ...variable }
        if (newVariable.varDef?.name) {
          newVariable.varDef.name = newVariable.varDef.name.replace(/\./g, DOT_REPLACEMENT_PATTERN)
        }
        return newVariable
      }) ?? []
    )
  }, [wfSpec])

  const formatVariablesPayload = (values: FormValues) => {
    const transformedObj = Object.keys(values).reduce((acc: RunWfRequest['variables'], key) => {
      if (values[key] === undefined) return acc
      const transformedKey = key.replace(DOT_REPLACEMENT_PATTERN, '.')

      if (
        wfSpecVariables.some(
          variable =>
            variable.accessLevel === WfRunVariableAccessLevel.INHERITED_VAR && variable.varDef?.name === transformedKey
        )
      ) {
        return acc
      }

      if (key.includes(STRUCT_FIELD_DATA_SEPARATOR_PATTERN)) {
        const [fieldName, variableCase, nestedStructPath, structDefName, structDefVersion] = key.split(
          STRUCT_FIELD_DATA_SEPARATOR_PATTERN
        )
        const structPath = nestedStructPath.split(STRUCT_PATH_SEPARATOR)

        const topLevelPath = structPath[0]
        if (acc[topLevelPath] === undefined) {
          acc[topLevelPath] = {
            value: {
              $case: 'struct',
              value: {
                structDefId: {
                  name: structDefName,
                  version: parseInt(structDefVersion),
                },
                struct: {
                  fields: {},
                },
              },
            },
          } satisfies VariableValue
        }

        const topLevelValue = acc[topLevelPath].value
        if (topLevelValue?.$case !== 'struct' || !topLevelValue.value.struct?.fields) return acc

        let currentFields = topLevelValue.value.struct.fields

        for (let i = 1; i < structPath.length; i++) {
          const nestedStructName = structPath[i]

          if (!currentFields[nestedStructName]) {
            currentFields[nestedStructName] = {
              value: {
                value: {
                  $case: 'struct',
                  value: {
                    structDefId: {
                      name: structDefName,
                      version: parseInt(structDefVersion),
                    },
                    struct: {
                      fields: {},
                    },
                  },
                },
              },
            }
          }

          const nestedField = currentFields[nestedStructName]?.value
          if (nestedField?.value?.$case === 'struct' && nestedField.value.value.struct?.fields) {
            currentFields = nestedField.value.value.struct.fields
          } else {
            return acc
          }
        }

        currentFields[fieldName] = {
          value: {
            value: {
              // typecast necessary here
              $case: variableCase as any,
              value: values[key] as any,
            },
          },
        }

        return acc
      }

      acc[transformedKey] = VariableValue.fromJSON({ [matchVariableType(transformedKey)]: values[key] })
      return acc
    }, {})

    return transformedObj
  }

  const matchVariableType = (key: string) => {
    const variable = wfSpecVariables.find((variable: ThreadVarDef) => variable.varDef?.name === key)
    if (!variable || !variable.varDef) return ''

    return getVariableDefType(variable.varDef)
  }

  const handleFormSubmit = async (values: FormValues) => {
    const customWfRunId = values.customWfRunId as string
    const parentWfRunId = values.parentWfRunId as string
    delete values.customWfRunId
    delete values.parentWfRunId
    if (!wfSpec.id || (wfSpec.parentWfSpec && !parentWfRunId)) return
    let variables = formatVariablesPayload(values)
    console.log(variables)

    try {
      const wfRun = await runWfSpec({
        tenantId,
        parentWfRunId: wfSpec.parentWfSpec ? wfRunIdFromFlattenedId(parentWfRunId) : undefined,
        wfSpecName: wfSpec.id.name,
        majorVersion: wfSpec.id.majorVersion,
        revision: wfSpec.id.revision,
        id: customWfRunId || undefined,
        variables,
      })
      if (!wfRun.id) return
      toast.success('Workflow has been executed')
      setShowModal(false)
      router.push(`/${tenantId}/wfRun/${wfRunIdToPath(wfRun.id)}`)
    } catch (error: any) {
      if (error.message) {
        toast.error(error.message.split(':')[1])
      } else {
        toast.error('An error occurred while executing the workflow')
      }
    }
  }

  return (
    <Dialog open={showModal} onOpenChange={open => setShowModal(open)}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Execute {wfSpec.id?.name}</DialogTitle>
          <DialogDescription className="text-primary/50">
            You can leave all optional fields blank if desired.
          </DialogDescription>
        </DialogHeader>

        <WfRunForm wfSpecVariables={wfSpecVariables} wfSpec={wfSpec} onSubmit={handleFormSubmit} ref={formRef} />

        <DialogFooter>
          <DialogClose className="mr-4">Cancel</DialogClose>
          <Button
            onClick={() => {
              formRef.current?.requestSubmit()
            }}
            type="submit"
          >
            Execute Workflow
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
