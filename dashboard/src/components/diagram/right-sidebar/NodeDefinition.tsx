'use client'

import { Node } from "littlehorse-client/proto"
import { getNodeType } from "@/utils/data/node"
import { EntrypointNodeComponent } from "./nodes/entrypoint"
import { ExitNodeComponent } from "./nodes/exit"
import { ExternalEventNodeComponent } from "./nodes/external-event"
import { NopNodeComponent } from "./nodes/nop"
import { SleepNodeComponent } from "./nodes/sleep"
import { StartMultipleThreadsNodeComponent } from "./nodes/start-multiple-threads"
import { StartThreadNodeComponent } from "./nodes/start-thread"
import { TaskNodeComponent } from "./nodes/task"
import { ThrowEventNodeComponent } from "./nodes/throw-event"
import { UserTaskNodeComponent } from "./nodes/user-task"
import { WaitForConditionNodeComponent } from "./nodes/wait-for-condition"
import { WaitForThreadsNodeComponent } from "./nodes/wait-for-threads"

interface NodeDefinitionProps {
  node: Node
}

export default function NodeDefinition({ node }: NodeDefinitionProps) {
  return (
    <div className="pt-2 w-full space-y-3">
      {
        (() => {
          switch (nodeInfo.type) {
            case 'TASK':
              return <TaskNodeComponent taskNode={nodeInfo.node} />
            case 'EXTERNAL_EVENT':
              return <ExternalEventNodeComponent node={nodeInfo.node} />
            case 'ENTRYPOINT':
              return <EntrypointNodeComponent entrypointNode={nodeInfo.node} />
            case 'EXIT':
              return <ExitNodeComponent exitNode={nodeInfo.node} />
            case 'START_THREAD':
              return <StartThreadNodeComponent startThreadNode={nodeInfo.node} />
            case 'WAIT_FOR_THREADS':
              return <WaitForThreadsNodeComponent waitForThreadsNode={nodeInfo.node} />
            case 'SLEEP':
              return <SleepNodeComponent sleepNode={nodeInfo.node} />
            case 'USER_TASK':
              return <UserTaskNodeComponent userTaskNode={nodeInfo.node} />
            case 'START_MULTIPLE_THREADS':
              return <StartMultipleThreadsNodeComponent startMultipleThreadsNode={nodeInfo.node} />
            case 'NOP':
              return <NopNodeComponent nopNode={nodeInfo.node} />
            case 'THROW_EVENT':
              return <ThrowEventNodeComponent throwEventNode={nodeInfo.node} />
            case 'WAIT_FOR_CONDITION':
              return <WaitForConditionNodeComponent waitForConditionNode={nodeInfo.node} />
            default:
              return <div>Unknown node type</div>
          }
        })()
      }
    </div>
  )
}
