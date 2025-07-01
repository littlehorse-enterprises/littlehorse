'use client'

import { useState } from 'react'
import { WfSpec, WfRun, ThreadVarDef, VariableType, WfRunVariableAccessLevel } from 'littlehorse-client/proto'
import { buildNodeTree, sortNodeTree } from '@/utils/data/node-tree'
import { TreeNodeComponent } from '../../tree-node-component'
import { NodeSearchBar } from '../../node-search-bar'
import { Separator } from '@littlehorse-enterprises/ui-library/separator'
import { cn } from '@/utils/ui/utils'
import { accessLevelLabels } from '@/utils/data/accessLevels'

interface WfSpecTabProps {
  wfSpec: WfSpec
  wfRun?: WfRun
}

const varTypeColors: Record<VariableType, string> = {
  [VariableType.INT]: 'bg-blue-100 text-blue-600',
  [VariableType.DOUBLE]: 'bg-green-100 text-green-600',
  [VariableType.BOOL]: 'bg-purple-100 text-purple-600',
  [VariableType.STR]: 'bg-yellow-100 text-yellow-600',
  [VariableType.BYTES]: 'bg-red-100 text-red-600',
  [VariableType.JSON_OBJ]: 'bg-orange-100 text-orange-600',
  [VariableType.JSON_ARR]: 'bg-rose-100 text-rose-600',
  [VariableType.UNRECOGNIZED]: 'bg-gray-100 text-gray-600',
}

const accessLevelColors: Record<WfRunVariableAccessLevel, string> = {
  [WfRunVariableAccessLevel.PUBLIC_VAR]: 'bg-blue-100 text-blue-600',
  [WfRunVariableAccessLevel.INHERITED_VAR]: 'bg-green-100 text-green-600',
  [WfRunVariableAccessLevel.PRIVATE_VAR]: 'bg-pink-100 text-pink-600',
  [WfRunVariableAccessLevel.UNRECOGNIZED]: 'bg-gray-100 text-gray-600',
}

function VariableDefComponent(threadVarDef: ThreadVarDef) {
  if (!threadVarDef.varDef?.typeDef) return null
  const variableType = threadVarDef.varDef.typeDef.type
  const accessLevel = threadVarDef.accessLevel

  return (
    <div className="flex items-center gap-2 bg-gray-100 rounded-lg">
      <div className={cn("text-xs px-1 font-bold border-2 rounded-md w-fit", varTypeColors[variableType])}>{variableType}</div>
      <div className={cn("text-xs px-1 font-bold border-2 rounded-md w-fit", accessLevelColors[accessLevel])}>{accessLevelLabels[accessLevel]}</div>
      <div className="text-nowrap text-ellipsis overflow-hidden">{threadVarDef.varDef?.name}</div>
    </div>
  )
}

export default function WfSpecTab({ wfSpec }: WfSpecTabProps) {
  const [searchTerm, setSearchTerm] = useState('')

  const nodeTree = buildNodeTree(wfSpec, wfSpec.entrypointThreadName)
  const sortedNodeTree = sortNodeTree(nodeTree)
  const threadVarDefs = wfSpec.threadSpecs[wfSpec.entrypointThreadName].variableDefs

  return (
    <div className="flex flex-col h-full p-2">
      {/* Search bar */}
      <NodeSearchBar searchTerm={searchTerm} onSearchChange={setSearchTerm} />

      {/* Tree nodes section with its own scroll */}
      <div className="flex-1 overflow-y-auto min-h-0 px-1">
        {sortedNodeTree.map(node => (
          <div key={node.id} className="mb-1">
            <TreeNodeComponent node={node} isRoot searchTerm={searchTerm} />
          </div>
        ))}
      </div>

      <Separator className="my-2 flex-shrink-0" />

      {/* Variables section with its own scroll */}
      <div className="flex-1 overflow-y-auto min-h-0">
        <div className="space-y-2 px-4">
          {threadVarDefs.map((threadVarDef) => (
            <VariableDefComponent key={threadVarDef.varDef?.name} {...threadVarDef} />
          ))}
        </div>
      </div>
    </div>
  )
}
