import { FC } from 'react'
import { useDiagram } from '../../../hooks/useDiagram'
import { ExitNode } from './ExitNode'
import { ExternalEventNode } from './ExternalEventNode'
import { StartThreadNode } from './StartThreadNode'
import { TaskNode } from './TaskNode'
import { ThrowEventNode } from './ThrowEventNode'
import { WaitForThreadsNode } from './WaitForThreadsNode'
import { SleepNode } from './SleepNode'
import { WaitForCondition } from './WaitForConditionNode'
import { UserTaskNode } from './UserTaskNode/UserTaskNode'
import { ChildWFNode } from './ChildWFNode'
import { WaitForChildWfNode } from './WaitForChildWfNode'

export const Node: FC = () => {
  const { selectedNode } = useDiagram()

  if (!selectedNode || selectedNode.data.node?.oneofKind === undefined) return
  const node = selectedNode.data.node

  if (node.oneofKind === 'task') return <TaskNode node={node.task} />
  if (node.oneofKind === 'exit') return <ExitNode node={node.exit} />
  if (node.oneofKind === 'externalEvent') return <ExternalEventNode node={node.externalEvent} />
  if (node.oneofKind === 'startThread') return <StartThreadNode node={node.startThread} />
  if (node.oneofKind === 'waitForThreads') return <WaitForThreadsNode node={node.waitForThreads} />
  if (node.oneofKind === 'throwEvent') return <ThrowEventNode node={node.throwEvent} />
  if (node.oneofKind === 'sleep') return <SleepNode node={node.sleep} />
  if (node.oneofKind === 'waitForCondition') return <WaitForCondition node={node.waitForCondition} />
  if (node.oneofKind === 'userTask') return <UserTaskNode node={node.userTask} />
  if (node.oneofKind === 'runChildWf') return <ChildWFNode node={node.runChildWf} />
  if (node.oneofKind === 'waitForChildWf') return <WaitForChildWfNode node={node.waitForChildWf} />

  return <></>
}
