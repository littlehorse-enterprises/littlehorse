'use client'

import { useState } from 'react'
import { WfSpec, WfRun } from 'littlehorse-client/proto'
import { TreeNodeComponent } from '../../tree-node-component'
import { NodeSearchBar } from '../../node-search-bar'
import { TreeNode } from '@/types'

interface WfSpecTabProps {
  wfSpec: WfSpec
  wfRun?: WfRun
}

export default function WfSpecTab({ wfSpec }: WfSpecTabProps) {
  const [searchTerm, setSearchTerm] = useState('')

  const nodeTree = buildNodeTree(wfSpec, wfSpec.entrypointThreadName)
  const sortedNodeTree = sortNodeTree(nodeTree)

  return (
    <div className="flex-1 overflow-y-auto p-2">
      {/* Search bar */}
      <NodeSearchBar searchTerm={searchTerm} onSearchChange={setSearchTerm} />

      {/* Render each node in the flat structure */}
      <div>
        {sortedNodeTree.map(node => (
          <div key={node.id} className="mb-1">
            <TreeNodeComponent node={node} isRoot searchTerm={searchTerm} />
          </div>
        ))}
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
