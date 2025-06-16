'use client'

import { EntrypointNode, ExitNode, ExternalEventNode, Node, NopNode, SleepNode, StartMultipleThreadsNode, StartThreadNode, TaskNode, ThrowEventNode, UserTaskNode, WaitForConditionNode, WaitForThreadsNode } from "littlehorse-client/proto"
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
    <div className="pt-2">
      {node.task && <TaskNodeComponent taskNode={node as Node & { task: TaskNode }} />}
      {node.externalEvent && <ExternalEventNodeComponent externalEventNode={node as Node & { externalEvent: ExternalEventNode }} />}
      {node.entrypoint && <EntrypointNodeComponent entrypointNode={node as Node & { entrypoint: EntrypointNode }} />}
      {node.exit && <ExitNodeComponent exitNode={node as Node & { exit: ExitNode }} />}
      {node.startThread && <StartThreadNodeComponent startThreadNode={node as Node & { startThread: StartThreadNode }} />}
      {node.waitForThreads && <WaitForThreadsNodeComponent waitForThreadsNode={node as Node & { waitForThreads: WaitForThreadsNode }} />}
      {node.sleep && <SleepNodeComponent sleepNode={node as Node & { sleep: SleepNode }} />}
      {node.userTask && <UserTaskNodeComponent userTaskNode={node as Node & { userTask: UserTaskNode }} />}
      {node.startMultipleThreads && <StartMultipleThreadsNodeComponent startMultipleThreadsNode={node as Node & { startMultipleThreads: StartMultipleThreadsNode }} />}
      {node.nop && <NopNodeComponent nopNode={node as Node & { nop: NopNode }} />}
      {node.throwEvent && <ThrowEventNodeComponent throwEventNode={node as Node & { throwEvent: ThrowEventNode }} />}
      {node.waitForCondition && <WaitForConditionNodeComponent waitForConditionNode={node as Node & { waitForCondition: WaitForConditionNode }} />}
    </div>
  )
} 
