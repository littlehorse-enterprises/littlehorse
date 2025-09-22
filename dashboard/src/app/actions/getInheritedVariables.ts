'use server'

import { ThreadVarDef, Variable, WfRunId, WfRunVariableAccessLevel } from 'littlehorse-client/proto'
import { lhClient } from '../lhClient'

export const getInheritedVariables = async (
  wfRunId: WfRunId,
  variableDefs: ThreadVarDef[],
  tenantId: string
): Promise<Variable[]> => {
  const client = await lhClient({ tenantId })

  const inheritedDefs = variableDefs.filter(v => v.accessLevel === WfRunVariableAccessLevel.INHERITED_VAR)

  const namesToFind = new Set(inheritedDefs.map(v => v.varDef?.name).filter(n => !!n))
  if (namesToFind.size === 0) return []

  const foundByName = new Map<string, Variable>()
  let current: WfRunId | undefined = wfRunId.parentWfRunId

  while (current && namesToFind.size > 0) {
    try {
      const { results } = await client.listVariables({ wfRunId: current })
      for (const v of results ?? []) {
        const name = v.id?.name
        if (!name || !namesToFind.has(name)) continue
        foundByName.set(name, v)
        namesToFind.delete(name)
      }
    } catch (e) {
      console.error('Error getting variables for wfRunId', current, e)
    }
    current = current.parentWfRunId
  }

  return Array.from(foundByName.values())
}
