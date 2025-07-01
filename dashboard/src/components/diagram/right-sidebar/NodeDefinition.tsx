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
  const nodeCase = node.node?.$case

  return (
    <div className="pt-2">
      {nodeCase === 'task' && <TaskNodeComponent taskNode={node as Node & { task: TaskNode }} />}
      {nodeCase === 'externalEvent' && <ExternalEventNodeComponent externalEventNode={node as Node & { externalEvent: ExternalEventNode }} />}
      {nodeCase === 'entrypoint' && <EntrypointNodeComponent entrypointNode={node as Node & { entrypoint: EntrypointNode }} />}
      {nodeCase === 'exit' && <ExitNodeComponent exitNode={node as Node & { exit: ExitNode }} />}
      {nodeCase === 'startThread' && <StartThreadNodeComponent startThreadNode={node as Node & { startThread: StartThreadNode }} />}
      {nodeCase === 'waitForThreads' && <WaitForThreadsNodeComponent waitForThreadsNode={node as Node & { waitForThreads: WaitForThreadsNode }} />}
      {nodeCase === 'sleep' && <SleepNodeComponent sleepNode={node as Node & { sleep: SleepNode }} />}
      {nodeCase === 'userTask' && <UserTaskNodeComponent userTaskNode={node as Node & { userTask: UserTaskNode }} />}
      {nodeCase === 'startMultipleThreads' && <StartMultipleThreadsNodeComponent startMultipleThreadsNode={node as Node & { startMultipleThreads: StartMultipleThreadsNode }} />}
      {nodeCase === 'nop' && <NopNodeComponent nopNode={node as Node & { nop: NopNode }} />}
      {nodeCase === 'throwEvent' && <ThrowEventNodeComponent throwEventNode={node as Node & { throwEvent: ThrowEventNode }} />}
      {nodeCase === 'waitForCondition' && <WaitForConditionNodeComponent waitForConditionNode={node as Node & { waitForCondition: WaitForConditionNode }} />}
    </div>
  )
}
