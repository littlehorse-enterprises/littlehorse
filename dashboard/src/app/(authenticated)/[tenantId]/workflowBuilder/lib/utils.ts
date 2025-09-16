import { ClassValue, clsx } from "clsx";
import { twMerge } from "tailwind-merge";
import { VariableType } from 'littlehorse-client/proto';
import { NodeType } from '../types';
import type { PutWfSpecRequest, PutTaskDefRequest, TaskDef } from 'littlehorse-client/proto';
import type { WorkflowState } from '../types';
import type { Node as ReactFlowNode } from 'reactflow';

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function extractTasksInfo(spec: PutWfSpecRequest): Array<TaskDef> {
  const tasksMap = new Map<string, TaskDef>();
  
  Object.values(spec.threadSpecs).forEach(threadSpec => {
    Object.values(threadSpec.nodes).forEach(node => {
      if (node.node?.$case === 'task' && node.node.value.taskToExecute?.$case === 'taskDefId') {
        const taskName = node.node.value.taskToExecute.value.name;
        const variables = node.node.value.variables || [];
        
        // TODO: ask Mijail about these type changes
        const inputVars = variables
          .filter(variable => variable.source?.$case === 'variableName')
          .map(variable => ({
            name: variable.source!.value as string,
            typeDef: {
              type: VariableType.STR,
              masked: false
            }
          }));
        
        if (!tasksMap.has(taskName)) {
          tasksMap.set(taskName, {
            id: { name: taskName },
            inputVars,
            createdAt: undefined
          });
        }
      }
    });
  });
  
  return Array.from(tasksMap.values());
}

export function createTaskDefRequest(taskInfo: TaskDef): PutTaskDefRequest {
  return {
    name: taskInfo.id?.name || '',
    inputVars: taskInfo.inputVars,
    returnType: {
      returnType: {
        type: VariableType.STR,
        masked: false
      }
    }
  };
}

export function generateNodeId(): string {
  return `node${Date.now()}${Math.floor(Math.random() * 1000)}`;
}

export function convertNodes(workflowState: WorkflowState): ReactFlowNode[] {
  const nodes: ReactFlowNode[] = [];
  const workflowNodes = workflowState.spec.threadSpecs.entrypoint.nodes;

  if (workflowNodes.entrypoint) {
    nodes.push({
      id: 'entrypoint',
      type: NodeType.ENTRY_POINT,
      position: { x: 100, y: 100 },
      data: {
        label: 'Entry Point'
      }
    });
  }

  /*Object.entries(workflowNodes).forEach(([nodeId, workflowNode], index) => {
    if (nodeId !== 'entrypoint' && nodeId !== 'exit' && workflowNode.task) {
      nodes.push({
        id: nodeId,
        type: NodeType.TASK_NODE,
        position: { x: 100 + (index * 200), y: 200 },
        data: {
          label: workflowNode.task.taskDefId?.name || 'Unknown Task',
          taskName: workflowNode.task.taskDefId?.name,
          varName: workflowNode.task.variables?.[0]?.variableName
        }
      });
    }
  });*/
  Object.entries(workflowNodes).forEach(([nodeId, workflowNode], index) => {
    if (nodeId !== 'entrypoint' && nodeId !== 'exit' && workflowNode.node?.$case === 'task') {
      const taskNode = workflowNode.node.value;
      const taskName = taskNode.taskToExecute?.$case === 'taskDefId' 
        ? taskNode.taskToExecute.value.name 
        : 'Unknown Task';
      
      const varName = taskNode.variables?.[0]?.source?.$case === 'variableName'
        ? taskNode.variables[0].source.value
        : undefined;

      nodes.push({
        id: nodeId,
        type: NodeType.TASK_NODE,
        position: { x: 100 + (index * 200), y: 200 },
        data: {
          label: taskName,
          taskName: taskName,
          varName: varName
        }
      });
    }
  });

  if (workflowNodes.exit) {
    const taskNodeCount = Object.keys(workflowNodes).filter(id => 
      id !== 'entrypoint' && id !== 'exit'
    ).length;
    nodes.push({
      id: 'exit',
      type: NodeType.EXIT_POINT,
      position: { x: 300 + (taskNodeCount * 200), y: 100 },
      data: {
        label: 'Exit Point'
      }
    });
  }

  return nodes;
}
