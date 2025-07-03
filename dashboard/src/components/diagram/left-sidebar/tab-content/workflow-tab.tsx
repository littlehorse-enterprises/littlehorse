'use client'

import { useMemo, useState } from 'react'
import { WfSpec, WfRun, ThreadVarDef, VariableType } from 'littlehorse-client/proto'
import { TreeNodeComponent } from '../../tree-node-component'
import { NodeSearchBar } from '../../node-search-bar'
import { Separator } from '@littlehorse-enterprises/ui-library/separator'
import { cn } from '@/utils/ui/utils'
import { TreeNode } from '@/types'
import { Section } from '../../right-sidebar/section'
import { Label } from '../../right-sidebar/label'

interface WorkflowTabProps {
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
    [VariableType.WF_RUN_ID]: ''
}

function VariableDefComponent(threadVarDef: ThreadVarDef) {
    if (!threadVarDef.varDef?.typeDef) return null
    const variableType = threadVarDef.varDef.typeDef.type

    return (
        <div className="flex items-center gap-2 bg-gray-100 rounded-md py-1 px-2">
            <div className={cn("text-xs px-1 font-bold border-2 rounded-md w-fit", varTypeColors[variableType])}>{variableType}</div>
            <div className="text-nowrap text-ellipsis overflow-hidden text-sm">{threadVarDef.varDef?.name}</div>
        </div>
    )
}

export default function WorkflowTab({ wfSpec }: WorkflowTabProps) {
    const [searchTerm, setSearchTerm] = useState('')

    const nodeTree = buildNodeTree(wfSpec, wfSpec.entrypointThreadName)
    const sortedNodeTree = sortNodeTree(nodeTree)

    const threadSpecsWithVariableDefs = useMemo(() => Object.entries(wfSpec.threadSpecs).filter((threadSpec) => threadSpec[1].variableDefs.length > 0).sort((a) => a[0] === 'entrypoint' ? -1 : 1), [wfSpec.threadSpecs])

    return (
        <div className="flex flex-col h-full p-2">
            <NodeSearchBar searchTerm={searchTerm} onSearchChange={setSearchTerm} />

            <div className="flex-1 overflow-y-auto min-h-0 px-1">
                {sortedNodeTree.map(node => (
                    <div key={node.id} className="mb-1">
                        <TreeNodeComponent node={node} isRoot searchTerm={searchTerm} />
                    </div>
                ))}
            </div>

            <Separator className="mt-2 flex-shrink-0" />

            <div className="border-b border-gray-200 p-2 text-xs text-[#656565]">
                <span className="font-medium">Workflow Variables</span>
            </div>
            <div className="flex-1 overflow-y-auto min-h-0">
                <div className="space-y-2 p-2">
                    {threadSpecsWithVariableDefs.map(([threadName, threadSpec]) =>
                        <Section key={threadName} title={threadName}>
                            {threadSpec.variableDefs.map((threadVarDef) => (
                                <Section key={threadVarDef.varDef?.name} title={<VariableDefComponent {...threadVarDef} />} isCollapsedDefault={false} >
                                    <Label label="Required" >{`${threadVarDef.required}`}</Label>
                                    <Label label="Searchable" >{`${threadVarDef.searchable}`}</Label>
                                    <Label label="AccessLevel" >{`${threadVarDef.accessLevel}`}</Label>

                                    <Section title="VariableDef">
                                        {threadVarDef.varDef?.type && <Label label="VariableType">{`${threadVarDef.varDef?.type}`}</Label>}
                                        <Label label="Name" >{`${threadVarDef.varDef?.name}`}</Label>
                                        <Label label="DefaultValue" >{`${threadVarDef.varDef?.defaultValue}`}</Label>
                                        {threadVarDef.varDef?.maskedValue && <Label label="MaskedValue">{`${threadVarDef.varDef?.maskedValue}`}</Label>}
                                        {threadVarDef.varDef?.typeDef && <Section title="TypeDef">
                                            <Label label="Type" >{`${threadVarDef.varDef?.typeDef?.type}`}</Label>
                                            <Label label="Masked" >{`${threadVarDef.varDef?.typeDef?.masked}`}</Label>
                                        </Section>}
                                    </Section>
                                    {threadVarDef.jsonIndexes.length > 0 && <Section title="JsonIndexes">
                                        {threadVarDef.jsonIndexes.map((jsonIndex, i) => (
                                            <Section key={JSON.stringify(jsonIndex) + i} title={`JsonIndex ${i}`} >
                                                <Label label="FieldPath" >{jsonIndex.fieldPath}</Label>
                                                <Label label="FieldType" >{`${jsonIndex.fieldType}`}</Label>
                                            </Section>
                                        ))}
                                    </Section>
                                    }
                                </Section>
                            ))}
                        </Section>
                    )}
                </div>
            </div>
        </div>
    )
}

function buildNodeTree(wfSpec: WfSpec, threadSpecName: string): TreeNode[] {
    const result: TreeNode[] = []

    Object.entries(wfSpec.threadSpecs[threadSpecName].nodes).forEach(([nodeId]) => {
        result.push({
            id: `${nodeId}:${threadSpecName}`,
            label: nodeId,
            type: undefined,
            status: undefined,
            children: [],
            level: 0,
        })
    })

    return result
}

function sortNodeTree(nodeTree: TreeNode[]): TreeNode[] {
    return [...nodeTree].sort((a, b) => {
        const aFirstPart = a.id.split('-')[0]
        const bFirstPart = b.id.split('-')[0]

        // If the first part is a number, sort numerically in ascending order
        if (!isNaN(Number(aFirstPart)) && !isNaN(Number(bFirstPart))) {
            return Number(aFirstPart) - Number(bFirstPart)
        }

        // Fallback to alphabetical sorting if not numbers
        return bFirstPart.localeCompare(aFirstPart)
    })
}
