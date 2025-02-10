'use client'
import { Separator } from '@/components/ui/separator'
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs'
import { CSSProperties, FC, PropsWithChildren, ReactNode, useEffect, useMemo, useState } from 'react'
import { internalsSymbol, useNodeId, useStore } from 'reactflow'
import { DiagramDataGroup } from './DataGroupComponents/DiagramDataGroup'
import React from 'react'
import { NodeRun, TaskRun } from 'littlehorse-client/proto'
import { Duration } from './DataGroupComponents/Duration'
import { Entry } from './DataGroupComponents/Entry'
import { ErrorMessage } from './DataGroupComponents/ErrorMessage'
import { Status } from './DataGroupComponents/Status'
import { DiagramDataGroupIndexer } from './DataGroupComponents/DiagramDataGroupIndexer'

type Props = PropsWithChildren<{ nodeRunList: NodeRun[] | undefined, nodeRunsIndex?: number, setNodeRunsIndex?: (index: number) => void }>

export const NodeDetails: FC<Props> = ({ children, nodeRunList, nodeRunsIndex, setNodeRunsIndex }) => {
  const [nodeRunsIndexInternal, setNodeRunsIndexInternal] = useState(nodeRunsIndex ?? 0);

  const contextNodeId = useNodeId()
  const nodes = useStore(state => state.getNodes())
  const setNodes = useStore(state => state.setNodes)
  const selectedNode = useMemo(
    () => nodes.find(node => node.selected && node.id === contextNodeId),
    [contextNodeId, nodes]
  )

  useEffect(() => {
    if (selectedNode && selectedNode.zIndex !== 9999) {
      setNodes(
        nodes.map(node => {
          if (node.selected) {
            return { ...node, zIndex: 9999 }
          } else {
            return { ...node, zIndex: 1 }
          }
        })
      )
    }
  }, [nodes, selectedNode, setNodes])

  useEffect(() => {
    if (nodeRunsIndex !== undefined && setNodeRunsIndex !== undefined) {
      setNodeRunsIndex(nodeRunsIndexInternal);
    }
  }, [nodeRunsIndexInternal, setNodeRunsIndex]);

  const zIndex: number = Math.max(...nodes.map(node => (node[internalsSymbol]?.z || 1) + 10))
  if (!selectedNode) {
    return null
  }

  const wrapperStyle: CSSProperties = {
    position: 'absolute',
    bottom: selectedNode.height!,
    transform: `translate(${selectedNode.width! / 2}px, 0px) translate(-50%, 0%)`,
    zIndex,
  }

  const diagramDataGroups = React.Children.toArray(children).flatMap(child => {
    if (React.isValidElement(child)) {
      if (child.type === DiagramDataGroup) {
        return [child];
      } else if (child.type === React.Fragment) {
        return React.Children.toArray(child.props.children).filter(
          fragmentChild => React.isValidElement(fragmentChild) && fragmentChild.type === DiagramDataGroup
        );
      }
    }
    return [];
  }) as React.ReactElement[];


  if (!nodeRunList || nodeRunList.length === 0) {
    return null;
  }

  return (
    <div style={wrapperStyle} className="flex gap-4 justify-center drop-shadow mb-6 items-start select-none">
      {nodeRunList && (
        <DiagramDataGroup label={nodeRunList.length > 1 ? `NodeRun #${nodeRunsIndexInternal}` : "NodeRun"} >
          <DiagramDataGroupIndexer index={nodeRunsIndexInternal} setIndex={setNodeRunsIndexInternal} indexes={nodeRunList.length} />
          <Entry label="Status:">
            <Status status={nodeRunList[nodeRunsIndexInternal].status} />
          </Entry>
          <Entry label="Error Message:">
            <ErrorMessage errorMessage={nodeRunList[nodeRunsIndexInternal].errorMessage} />
          </Entry>
          <Entry separator>
            <Duration arrival={nodeRunList[nodeRunsIndexInternal].arrivalTime} ended={nodeRunList[nodeRunsIndexInternal].endTime} />
          </Entry>
        </DiagramDataGroup>
      )}
      {diagramDataGroups.map((element, i) => (
        <span key={i}>
          {element}
        </span>
      ))}
    </div>
  )
}