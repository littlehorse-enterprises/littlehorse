'use client'

import { useState } from 'react'
import { WfSpec, WfRun } from 'littlehorse-client/proto'
import { buildNodeTree } from '@/utils/data/buildNodeTree'
import { sortNodeTree } from '@/utils/data/sortNodeTree'
import { TreeNodeComponent } from '../../tree-node-component'
import { NodeSearchBar } from '../../node-search-bar'

interface WfSpecTabProps {
  wfSpec: WfSpec
  wfRun?: WfRun
}

export default function WfSpecTab({ wfSpec }: WfSpecTabProps) {
  const nodes = wfSpec.threadSpecs.entrypoint.nodes
  const [searchTerm, setSearchTerm] = useState('')

  const nodeTree = buildNodeTree(nodes)
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
