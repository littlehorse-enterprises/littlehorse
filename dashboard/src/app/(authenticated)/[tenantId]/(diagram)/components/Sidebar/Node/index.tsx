import { FC } from 'react'
import { useDiagram } from '../../../hooks/useDiagram'
import { TaskNode } from './TaskNode'

export const Node: FC = () => {
  const { selectedNode } = useDiagram()

  if (!selectedNode || !selectedNode.data.node) return

  const { $case, value } = selectedNode.data.node

  if ($case === "task") return (<TaskNode node= { value } />)

  return <></>
}
