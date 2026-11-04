import { RunWfRequest, StructDefId, StructField, VariableValue } from 'littlehorse-client/proto'
import {
  createContext,
  FC,
  MutableRefObject,
  ReactNode,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
} from 'react'

export const DOT_REPLACEMENT_PATTERN = '*-/:DOT_REPLACE_PATTERN'
export const STRUCT_FORM_FIELD_PREFIX = 'structValues'

type StructPath = string[]
export type VariableCase = Exclude<VariableValue['value']['oneofKind'], undefined>

export interface StructFormContextValue {
  registerStructPath: (path: StructPath, structDefId?: StructDefId, includeEmptyStruct?: boolean) => void
  unregisterStructPath: (path: StructPath) => void
  setPrimitiveFieldValue: (
    path: StructPath,
    structDefId: StructDefId | undefined,
    fieldName: string,
    variableCase: VariableCase,
    value: unknown
  ) => void
  setFieldValue: (
    path: StructPath,
    structDefId: StructDefId | undefined,
    fieldName: string,
    value: VariableValue
  ) => void
  clearFieldValue: (path: StructPath, fieldName: string) => void
  getStructVariables: () => RunWfRequest['variables']
}

const STRUCT_PATH_KEY_SEPARATOR = '__STRUCT_PATH__'

const structPathKey = (path: StructPath) => path.join(STRUCT_PATH_KEY_SEPARATOR)

const createStructVariableValue = (structDefId?: StructDefId): VariableValue => ({
  value: {
    oneofKind: 'struct',
    struct: {
      ...(structDefId ? { structDefId } : {}),
      struct: {
        fields: {},
      },
    },
  },
})

const cloneVariableValue = (value: VariableValue): VariableValue => JSON.parse(JSON.stringify(value))

const ensureStructFields = (value: VariableValue, structDefId?: StructDefId): Record<string, StructField> => {
  if (!value.value || value.value.oneofKind !== 'struct') {
    value.value = {
      oneofKind: 'struct',
      struct: {
        ...(structDefId ? { structDefId } : {}),
        struct: {
          fields: {},
        },
      },
    }
  } else if (!value.value.struct.struct) {
    value.value.struct.struct = { fields: {} }
  }

  const structUnion = value.value
  if (!structUnion || structUnion.oneofKind !== 'struct' || !structUnion.struct.struct) {
    return {}
  }

  return structUnion.struct.struct.fields
}

const readStructFields = (value?: VariableValue): Record<string, StructField> | undefined => {
  if (!value || !value.value || value.value.oneofKind !== 'struct') {
    return undefined
  }

  const inlineStruct = value.value.struct.struct
  if (!inlineStruct) return undefined

  return inlineStruct.fields
}

const StructFormContext = createContext<StructFormContextValue | null>(null)

export const useStructFormContext = () => {
  const context = useContext(StructFormContext)
  if (!context) {
    throw new Error('StructFormContext is not available')
  }
  return context
}

interface StructFormProviderProps {
  children: ReactNode
  contextRef?: MutableRefObject<StructFormContextValue | null>
}

export const StructFormProvider: FC<StructFormProviderProps> = ({ children, contextRef }) => {
  const structValuesRef = useRef<RunWfRequest['variables']>({})
  const structRegistryRef = useRef<Map<string, { structDefId?: StructDefId }>>(new Map())

  const ensureStructAtPath = useCallback((path: StructPath): Record<string, StructField> | undefined => {
    if (path.length === 0) return undefined

    const [topLevelName] = path
    const topKey = structPathKey([topLevelName])
    const topRegistration = structRegistryRef.current.get(topKey)
    if (!topRegistration) return undefined

    const structValues = structValuesRef.current
    if (!structValues[topLevelName]) {
      structValues[topLevelName] = createStructVariableValue(topRegistration.structDefId)
    }

    const topValue = structValues[topLevelName]
    let currentFields = ensureStructFields(topValue, topRegistration.structDefId)

    for (let index = 1; index < path.length; index++) {
      const segment = path[index]
      const segmentKey = structPathKey(path.slice(0, index + 1))
      const segmentRegistration = structRegistryRef.current.get(segmentKey)
      if (!segmentRegistration) return undefined

      const existingField = currentFields[segment]
      if (!existingField) {
        currentFields[segment] = {
          value: createStructVariableValue(segmentRegistration.structDefId),
          masked: false,
        }
      } else if (!existingField.value) {
        existingField.value = createStructVariableValue(segmentRegistration.structDefId)
      }

      const fieldValue = currentFields[segment].value
      if (!fieldValue) return undefined
      currentFields = ensureStructFields(fieldValue, segmentRegistration.structDefId)
    }

    return currentFields
  }, [])

  const getExistingStructFields = useCallback((path: StructPath): Record<string, StructField> | undefined => {
    if (path.length === 0) return undefined

    const structValues = structValuesRef.current
    const topValue = structValues[path[0]]
    if (!topValue) return undefined

    let currentFields = readStructFields(topValue)
    if (!currentFields) return undefined

    if (path.length === 1) return currentFields

    for (let index = 1; index < path.length; index++) {
      const segment = path[index]
      const field = currentFields[segment]
      if (!field) return undefined

      currentFields = readStructFields(field.value)
      if (!currentFields) return undefined
    }

    return currentFields
  }, [])

  const cleanupStructHierarchy = useCallback(
    (path: StructPath) => {
      for (let depth = path.length; depth > 0; depth--) {
        const currentPath = path.slice(0, depth)
        const currentFields = getExistingStructFields(currentPath)
        if (!currentFields || Object.keys(currentFields).length > 0) {
          break
        }

        if (depth === 1) {
          delete structValuesRef.current[currentPath[0]]
        } else {
          const parentFields = getExistingStructFields(currentPath.slice(0, -1))
          if (parentFields) {
            delete parentFields[currentPath[currentPath.length - 1]]
          }
        }
      }
    },
    [getExistingStructFields]
  )

  const registerStructPath = useCallback(
    (path: StructPath, structDefId?: StructDefId, includeEmptyStruct = false) => {
      structRegistryRef.current.set(structPathKey(path), { structDefId })
      if (includeEmptyStruct) {
        ensureStructAtPath(path)
      }
    },
    [ensureStructAtPath]
  )

  const unregisterStructPath = useCallback(
    (path: StructPath) => {
      const targetKey = structPathKey(path)
      for (const key of Array.from(structRegistryRef.current.keys())) {
        if (key === targetKey || key.startsWith(`${targetKey}${STRUCT_PATH_KEY_SEPARATOR}`)) {
          structRegistryRef.current.delete(key)
        }
      }

      if (path.length === 0) return

      if (path.length === 1) {
        delete structValuesRef.current[path[0]]
      } else {
        const parentFields = getExistingStructFields(path.slice(0, -1))
        if (parentFields) {
          delete parentFields[path[path.length - 1]]
        }
      }

      cleanupStructHierarchy(path.slice(0, -1))
    },
    [cleanupStructHierarchy, getExistingStructFields]
  )

  const setPrimitiveFieldValue = useCallback(
    (
      path: StructPath,
      structDefId: StructDefId | undefined,
      fieldName: string,
      variableCase: VariableCase,
      value: unknown
    ) => {
      if (path.length === 0) return
      structRegistryRef.current.set(structPathKey(path), { structDefId })
      const fields = ensureStructAtPath(path)
      if (!fields) return
      fields[fieldName] = {
        value: {
          value: {
            oneofKind: variableCase,
            [variableCase]: value,
          } as unknown as VariableValue['value'],
        },
        masked: false,
      }
    },
    [ensureStructAtPath]
  )

  const setFieldValue = useCallback(
    (path: StructPath, structDefId: StructDefId | undefined, fieldName: string, value: VariableValue) => {
      if (path.length === 0) return
      structRegistryRef.current.set(structPathKey(path), { structDefId })
      const fields = ensureStructAtPath(path)
      if (!fields) return
      fields[fieldName] = { value, masked: false }
    },
    [ensureStructAtPath]
  )

  const clearFieldValue = useCallback(
    (path: StructPath, fieldName: string) => {
      const fields = getExistingStructFields(path)
      if (!fields) return
      delete fields[fieldName]
      cleanupStructHierarchy(path)
    },
    [cleanupStructHierarchy, getExistingStructFields]
  )

  const getStructVariables = useCallback(
    () =>
      Object.entries(structValuesRef.current).reduce(
        (acc, [key, variable]) => {
          acc[key] = cloneVariableValue(variable)
          return acc
        },
        {} as RunWfRequest['variables']
      ),
    []
  )

  const contextValue = useMemo<StructFormContextValue>(
    () => ({
      registerStructPath,
      unregisterStructPath,
      setPrimitiveFieldValue,
      setFieldValue,
      clearFieldValue,
      getStructVariables,
    }),
    [
      registerStructPath,
      unregisterStructPath,
      setPrimitiveFieldValue,
      setFieldValue,
      clearFieldValue,
      getStructVariables,
    ]
  )

  useEffect(() => {
    if (!contextRef) return
    contextRef.current = contextValue
    return () => {
      contextRef.current = null
    }
  }, [contextRef, contextValue])

  return <StructFormContext.Provider value={contextValue}>{children}</StructFormContext.Provider>
}
