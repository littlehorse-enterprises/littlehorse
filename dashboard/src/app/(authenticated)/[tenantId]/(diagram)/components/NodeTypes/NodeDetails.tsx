import { Separator } from '@/components/ui/separator'
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs'
import { CSSProperties, FC, PropsWithChildren, ReactNode, useEffect, useMemo } from 'react'
import { internalsSymbol, useNodeId, useStore } from 'reactflow'
import { DiagramDataGroup } from './DataGroupComponents/DiagramDataGroup'
import React from 'react'
import { NodeRun, TaskRun } from 'littlehorse-client/proto'
import { Duration } from './DataGroupComponents/Duration'
import { Entry } from './DataGroupComponents/Entry'
import { ErrorMessage } from './DataGroupComponents/ErrorMessage'
import { Status } from './DataGroupComponents/Status'

type Props = PropsWithChildren<{ nodeRun: NodeRun | undefined }>

export const NodeDetails: FC<Props> = ({ children, nodeRun }) => {
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

  const tabsContentClassName = "flex gap-4"


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

  const groupedDiagramDataGroups = diagramDataGroups.reduce((acc, diagramDataGroup) => {
    const tabName = diagramDataGroup.props.tab || 'default';
    if (!acc[tabName]) {
      acc[tabName] = [];
    }
    acc[tabName].push(diagramDataGroup);
    return acc;
  }, {} as Record<string, React.ReactElement[]>) as Record<string, React.ReactElement[]>;

  return (
    <div style={wrapperStyle} className="flex gap-4 justify-center drop-shadow mb-6 items-center">
      <Tabs defaultValue="node" className="flex flex-col items-center ">
        <TabsList className="mb-7">
          <TabsTrigger value="node">Node</TabsTrigger>
          {Object.keys(groupedDiagramDataGroups).map((tabName, index) => (
            <TabsTrigger key={index} value={tabName}>{tabName}</TabsTrigger>
          ))}
        </TabsList>
        <TabsContent value="node" className={tabsContentClassName}>
          {nodeRun && (
            <DiagramDataGroup tab="Node" label="NodeRun">
              <Entry label="Status:">
                <Status status={nodeRun.status} />
              </Entry>
              <Entry label="Error Message:">
                <ErrorMessage errorMessage={nodeRun.errorMessage} />
              </Entry>
              <Entry separator>
                <Duration arrival={nodeRun.arrivalTime} ended={nodeRun.endTime} />
              </Entry>
            </DiagramDataGroup>
          )}
        </TabsContent>
        {Object.keys(groupedDiagramDataGroups).map((tabName, index) => (
          <TabsContent key={index} value={tabName} className={tabsContentClassName}>
            {groupedDiagramDataGroups[tabName].map((element, i) => (
              <span key={i}>
                {element}
              </span>
            ))}
          </TabsContent>
        ))}
      </Tabs>
    </div>
  )
}