'use client'

import { EntrypointNode, ExitNode, ExternalEventNode, Node, SleepNode, StartMultipleThreadsNode, StartThreadNode, TaskNode, UserTaskNode, WaitForThreadsNode } from "littlehorse-client/proto"
import { EntrypointNodeComponent } from "./nodes/entrypoint"
import { ExitNodeComponent } from "./nodes/exit"
import { ExternalEventNodeComponent } from "./nodes/external-event"
import { TaskNodeComponent } from "./nodes/task"
import { StartThreadNodeComponent } from "./nodes/start-thread"
import { WaitForThreadsNodeComponent } from "./nodes/wait-for-threads"
import { SleepNodeComponent } from "./nodes/sleep"
import { UserTaskNodeComponent } from "./nodes/user-task"
import { StartMultipleThreadsNodeComponent } from "./nodes/start-multiple-threads"


interface NodeDefinitionProps {
  node: Node
}

export default function NodeDefinition({ node }: NodeDefinitionProps) {
  return (
    <div className="p-4">
      <h3 className="mb-2 font-medium">Node Definition</h3>
      {node.task && <TaskNodeComponent taskNode={node as Node & { task: TaskNode }} />}
      {node.externalEvent && <ExternalEventNodeComponent externalEventNode={node as Node & { externalEvent: ExternalEventNode }} />}
      {node.entrypoint && <EntrypointNodeComponent entrypointNode={node as Node & { entrypoint: EntrypointNode }} />}
      {node.exit && <ExitNodeComponent exitNode={node as Node & { exit: ExitNode }} />}
      {node.startThread && <StartThreadNodeComponent startThreadNode={node as Node & { startThread: StartThreadNode }} />}
      {node.waitForThreads && <WaitForThreadsNodeComponent waitForThreadsNode={node as Node & { waitForThreads: WaitForThreadsNode }} />}
      {node.sleep && <SleepNodeComponent sleepNode={node as Node & { sleep: SleepNode }} />}
      {node.userTask && <UserTaskNodeComponent userTaskNode={node as Node & { userTask: UserTaskNode }} />}
      {node.startMultipleThreads && <StartMultipleThreadsNodeComponent startMultipleThreadsNode={node as Node & { startMultipleThreads: StartMultipleThreadsNode }} />}
    </div>
  )
} 
