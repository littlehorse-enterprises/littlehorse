'use client'

import { Node } from 'littlehorse-client/proto'
import { EntrypointNodeComponent } from './proto-components/nodes/entrypoint'
import { ExitNodeComponent } from './proto-components/nodes/exit'
import { ExternalEventNodeComponent } from './proto-components/nodes/external-event'
import { NopNodeComponent } from './proto-components/nodes/nop'
import { SleepNodeComponent } from './proto-components/nodes/sleep'
import { StartMultipleThreadsNodeComponent } from './proto-components/nodes/start-multiple-threads'
import { StartThreadNodeComponent } from './proto-components/nodes/start-thread'
import { TaskNodeComponent } from './proto-components/nodes/task'
import { ThrowEventNodeComponent } from './proto-components/nodes/throw-event'
import { UserTaskNodeComponent } from './proto-components/nodes/user-task'
import { WaitForConditionNodeComponent } from './proto-components/nodes/wait-for-condition'
import { WaitForThreadsNodeComponent } from './proto-components/nodes/wait-for-threads'

interface NodeDefinitionProps {
  node: Node
}

export default function NodeDefinition({ node }: NodeDefinitionProps) {
  const nodeCase = node.node?.$case

  return (
    <div className="pt-2">
      {nodeCase === 'task' && <TaskNodeComponent {...node.node.task} />}
      {nodeCase === 'externalEvent' && <ExternalEventNodeComponent {...node.node.externalEvent} />}
      {nodeCase === 'entrypoint' && <EntrypointNodeComponent {...node.node.entrypoint} />}
      {nodeCase === 'exit' && <ExitNodeComponent {...node.node.exit} />}
      {nodeCase === 'startThread' && <StartThreadNodeComponent {...node.node.startThread} />}
      {nodeCase === 'waitForThreads' && <WaitForThreadsNodeComponent {...node.node.waitForThreads} />}
      {nodeCase === 'sleep' && <SleepNodeComponent {...node.node.sleep} />}
      {nodeCase === 'userTask' && <UserTaskNodeComponent {...node.node.userTask} />}
      {nodeCase === 'startMultipleThreads' && <StartMultipleThreadsNodeComponent {...node.node.startMultipleThreads} />}
      {nodeCase === 'nop' && <NopNodeComponent {...node.node.nop} />}
      {nodeCase === 'throwEvent' && <ThrowEventNodeComponent {...node.node.throwEvent} />}
      {nodeCase === 'waitForCondition' && <WaitForConditionNodeComponent {...node.node.waitForCondition} />}
    </div>
  )
}
