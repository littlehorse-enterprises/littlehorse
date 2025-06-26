'use client'

import {
  Node,
  TaskNode,
  ExternalEventNode,
  EntrypointNode,
  ExitNode,
  StartThreadNode,
  WaitForThreadsNode,
  SleepNode,
  UserTaskNode,
  StartMultipleThreadsNode,
  NopNode,
  ThrowEventNode,
  WaitForConditionNode
} from "littlehorse-client/proto"
import { getNodeType } from "@/utils/data/node"
import { EntrypointNodeComponent } from "./proto-components/nodes/entrypoint"
import { ExitNodeComponent } from "./proto-components/nodes/exit"
import { ExternalEventNodeComponent } from "./proto-components/nodes/external-event"
import { NopNodeComponent } from "./proto-components/nodes/nop"
import { SleepNodeComponent } from "./proto-components/nodes/sleep"
import { StartMultipleThreadsNodeComponent } from "./proto-components/nodes/start-multiple-threads"
import { StartThreadNodeComponent } from "./proto-components/nodes/start-thread"
import { TaskNodeComponent } from "./proto-components/nodes/task"
import { ThrowEventNodeComponent } from "./proto-components/nodes/throw-event"
import { UserTaskNodeComponent } from "./proto-components/nodes/user-task"
import { WaitForConditionNodeComponent } from "./proto-components/nodes/wait-for-condition"
import { WaitForThreadsNodeComponent } from "./proto-components/nodes/wait-for-threads"

interface NodeDefinitionProps {
  node: Node
}

export default function NodeDefinition({ node }: NodeDefinitionProps) {
  const { node: nodeOneOf, type } = getNodeType(node)

  return (
    <div className="pt-2 w-full space-y-3">
      {
        (() => {
          switch (type) {
            case 'TASK':
              return <TaskNodeComponent {...(nodeOneOf.task as TaskNode)} />
            case 'EXTERNAL_EVENT':
              return <ExternalEventNodeComponent {...(nodeOneOf.externalEvent as ExternalEventNode)} />
            case 'ENTRYPOINT':
              return <EntrypointNodeComponent {...(nodeOneOf.entrypoint as EntrypointNode)} />
            case 'EXIT':
              return <ExitNodeComponent {...(nodeOneOf.exit as ExitNode)} />
            case 'START_THREAD':
              return <StartThreadNodeComponent {...(nodeOneOf.startThread as StartThreadNode)} />
            case 'WAIT_FOR_THREADS':
              return <WaitForThreadsNodeComponent {...(nodeOneOf.waitForThreads as WaitForThreadsNode)} />
            case 'SLEEP':
              return <SleepNodeComponent {...(nodeOneOf.sleep as SleepNode)} />
            case 'USER_TASK':
              return <UserTaskNodeComponent {...(nodeOneOf.userTask as UserTaskNode)} />
            case 'START_MULTIPLE_THREADS':
              return <StartMultipleThreadsNodeComponent {...(nodeOneOf.startMultipleThreads as StartMultipleThreadsNode)} />
            case 'NOP':
              return <NopNodeComponent {...(nodeOneOf.nop as NopNode)} />
            case 'THROW_EVENT':
              return <ThrowEventNodeComponent {...(nodeOneOf.throwEvent as ThrowEventNode)} />
            case 'WAIT_FOR_CONDITION':
              return <WaitForConditionNodeComponent {...(nodeOneOf.waitForCondition as WaitForConditionNode)} />
            default:
              return <div>Unknown node type</div>
          }
        })()
      }
    </div>
  )
}
