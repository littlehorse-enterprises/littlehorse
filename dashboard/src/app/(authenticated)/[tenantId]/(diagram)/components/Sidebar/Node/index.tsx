import { FC } from 'react'
import { useDiagram } from '../../../hooks/useDiagram'
import { TaskNode } from './TaskNode'
import { ExitNode } from './ExitNode'
import { ExternalEventNode } from './ExternalEventNode'
import { StartThreadNode } from './StartThreadNode'

export const Node: FC = () => {
  const { selectedNode } = useDiagram()

  if (!selectedNode || !selectedNode.data.node) return

  const { $case, value } = selectedNode.data.node

  if ($case === "task") return (<TaskNode node={value} />)
  if ($case === "exit") return (<ExitNode node={value} />)
  if ($case === "externalEvent") return (<ExternalEventNode node={value} />)
  if ($case === "startThread") return (<StartThreadNode node={value} />)

  return <></>
}
