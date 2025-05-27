import { type Node, type Edge, MarkerType } from "reactflow";
import { Node as LittlehorseNode } from "littlehorse-client/proto";

// Define time ranges for filtering
export const TIME_RANGES = [
  { value: "all", label: "All time", minutes: null },
  { value: "5m", label: "Last 5 minutes", minutes: 5 },
  { value: "15m", label: "Last 15 minutes", minutes: 15 },
  { value: "30m", label: "Last 30 minutes", minutes: 30 },
  { value: "1h", label: "Last 1 hour", minutes: 60 },
  { value: "3h", label: "Last 3 hours", minutes: 180 },
  { value: "6h", label: "Last 6 hours", minutes: 360 },
  { value: "12h", label: "Last 12 hours", minutes: 720 },
  { value: "24h", label: "Last 24 hours", minutes: 1440 },
  { value: "7d", label: "Last 7 days", minutes: 10080 },
  { value: "30d", label: "Last 30 days", minutes: 43200 },
];

// Define the types for our task data
export interface TaskRunData {
  status: string;
  timeout: number;
  maxAttempts: number;
  inputVariables: Record<string, any>[];
}

export interface TaskAttemptData {
  status: string;
  result: {
    type: "Output" | "Exception" | "Error";
    message: string;
  };
  workerLogOutput: string;
  arrivalTime: string;
  endTime: string;
}

// Define the types for our node data
export interface TaskNodeData {
  label: string;
  status: "completed" | "error" | "running" | "pending";
  taskRun?: TaskRunData;
  taskAttempt?: TaskAttemptData;
}

export interface NodeData {
  label: string;
  status?: "completed" | "error" | "running" | "pending";
  taskRun?: TaskRunData;
  taskAttempt?: TaskAttemptData;
}

// Initial nodes with enhanced task data
export const initialNodes: Node<NodeData>[] = [
  {
    id: "1",
    type: "start",
    data: { label: "Start" },
    position: { x: 250, y: 5 },
  },
  {
    id: "2",
    type: "task",
    data: {
      label: "Validate Order",
      status: "completed",
      taskRun: {
        status: "TASK_SUCCESS",
        timeout: 30000,
        maxAttempts: 3,
        inputVariables: [
          { name: "orderId", type: "STRING", value: "ORD-12345" },
          { name: "customerId", type: "STRING", value: "CUST-789" },
        ],
      },
      taskAttempt: {
        status: "TASK_SUCCESS",
        result: {
          type: "Output",
          message: '{ "isValid": true, "validationScore": 95 }',
        },
        workerLogOutput:
          "INFO: Validating order ORD-12345\nINFO: Customer verification passed\nINFO: Order validation complete",
        arrivalTime: "2023-05-03T14:32:40.123Z",
        endTime: "2023-05-03T14:32:41.456Z",
      },
    },
    position: { x: 250, y: 100 },
  },
  {
    id: "3",
    type: "decision",
    data: { label: "Is Valid?" },
    position: { x: 250, y: 200 },
  },
  {
    id: "4",
    type: "task",
    data: {
      label: "Process Payment",
      status: "completed",
      taskRun: {
        status: "TASK_SUCCESS",
        timeout: 60000,
        maxAttempts: 5,
        inputVariables: [
          { name: "orderId", type: "STRING", value: "ORD-12345" },
          { name: "amount", type: "DOUBLE", value: 129.99 },
          { name: "paymentMethod", type: "STRING", value: "CREDIT_CARD" },
        ],
      },
      taskAttempt: {
        status: "TASK_SUCCESS",
        result: {
          type: "Output",
          message: '{ "transactionId": "TXN-78901", "status": "APPROVED" }',
        },
        workerLogOutput:
          "INFO: Processing payment for order ORD-12345\nINFO: Amount: $129.99\nINFO: Payment approved\nINFO: Transaction ID: TXN-78901",
        arrivalTime: "2023-05-03T14:32:45.123Z",
        endTime: "2023-05-03T14:32:46.456Z",
      },
    },
    position: { x: 100, y: 300 },
  },
  {
    id: "5",
    type: "task",
    data: {
      label: "Reject Order",
      status: "error",
      taskRun: {
        status: "TASK_FAILURE",
        timeout: 15000,
        maxAttempts: 2,
        inputVariables: [
          { name: "orderId", type: "STRING", value: "ORD-12345" },
          { name: "reason", type: "STRING", value: "VALIDATION_FAILED" },
        ],
      },
      taskAttempt: {
        status: "TASK_FAILURE",
        result: {
          type: "Error",
          message: "Failed to notify customer about order rejection",
        },
        workerLogOutput:
          "INFO: Rejecting order ORD-12345\nERROR: Failed to send notification\nERROR: Customer notification service unavailable",
        arrivalTime: "2023-05-03T14:32:50.123Z",
        endTime: "2023-05-03T14:32:51.456Z",
      },
    },
    position: { x: 400, y: 300 },
  },
  {
    id: "6",
    type: "task",
    data: {
      label: "Prepare Shipment",
      status: "running",
      taskRun: {
        status: "TASK_RUNNING",
        timeout: 120000,
        maxAttempts: 1,
        inputVariables: [
          { name: "orderId", type: "STRING", value: "ORD-12345" },
          { name: "warehouseId", type: "STRING", value: "WH-001" },
          { name: "priority", type: "STRING", value: "STANDARD" },
        ],
      },
      taskAttempt: {
        status: "TASK_RUNNING",
        result: {
          type: "Output",
          message: "In progress",
        },
        workerLogOutput:
          "INFO: Preparing shipment for order ORD-12345\nINFO: Assigned to warehouse WH-001\nINFO: Items being collected...",
        arrivalTime: "2023-05-03T14:32:55.123Z",
        endTime: "", // Still running
      },
    },
    position: { x: 100, y: 400 },
  },
  {
    id: "7",
    type: "end",
    data: { label: "End" },
    position: { x: 250, y: 500 },
  },
];

// Initial edges
export const initialEdges: Edge[] = [
  {
    id: "e1-2",
    source: "1",
    target: "2",
    animated: true,
    markerEnd: {
      type: MarkerType.ArrowClosed,
    },
  },
  {
    id: "e2-3",
    source: "2",
    target: "3",
    animated: true,
    markerEnd: {
      type: MarkerType.ArrowClosed,
    },
  },
  {
    id: "e3-4",
    source: "3",
    target: "4",
    animated: true,
    label: "Yes",
    markerEnd: {
      type: MarkerType.ArrowClosed,
    },
  },
  {
    id: "e3-5",
    source: "3",
    target: "5",
    animated: true,
    label: "No",
    markerEnd: {
      type: MarkerType.ArrowClosed,
    },
  },
  {
    id: "e4-6",
    source: "4",
    target: "6",
    animated: true,
    markerEnd: {
      type: MarkerType.ArrowClosed,
    },
  },
  {
    id: "e6-7",
    source: "6",
    target: "7",
    animated: true,
    markerEnd: {
      type: MarkerType.ArrowClosed,
    },
  },
  {
    id: "e5-7",
    source: "5",
    target: "7",
    animated: true,
    markerEnd: {
      type: MarkerType.ArrowClosed,
    },
  },
];

// Helper function to generate dynamic timestamps for testing
function getTimeAgo(minutes: number): string {
  const date = new Date(Date.now() - minutes * 60 * 1000);
  return date.toISOString();
}

// Mock data for workflow runs
export interface WorkflowRun {
  id: string;
  status: "RUNNING" | "COMPLETED" | "FAILED" | "PENDING";
  startTime: string;
}

export const mockWorkflowRuns: WorkflowRun[] = [
  {
    id: "wfr-12345-abcde-67890",
    status: "COMPLETED",
    startTime: getTimeAgo(4), // 4 minutes ago (within 5m range)
  },
  {
    id: "wfr-23456-bcdef-78901",
    status: "RUNNING",
    startTime: getTimeAgo(10), // 10 minutes ago (within 15m range)
  },
  {
    id: "wfr-34567-cdefg-89012",
    status: "FAILED",
    startTime: getTimeAgo(20), // 20 minutes ago (within 30m range)
  },
  {
    id: "wfr-45678-defgh-90123",
    status: "COMPLETED",
    startTime: getTimeAgo(45), // 45 minutes ago (within 1h range)
  },
  {
    id: "wfr-56789-efghi-01234",
    status: "COMPLETED",
    startTime: getTimeAgo(120), // 2 hours ago (within 3h range)
  },
  {
    id: "wfr-67890-fghij-12345",
    status: "PENDING",
    startTime: getTimeAgo(300), // 5 hours ago (within 6h range)
  },
  {
    id: "wfr-78901-ghijk-23456",
    status: "RUNNING",
    startTime: getTimeAgo(600), // 10 hours ago (within 12h range)
  },
  {
    id: "wfr-89012-hijkl-34567",
    status: "COMPLETED",
    startTime: getTimeAgo(1200), // 20 hours ago (within 24h range)
  },
  {
    id: "wfr-90123-ijklm-45678",
    status: "FAILED",
    startTime: getTimeAgo(5000), // ~3.5 days ago (within 7d range)
  },
  {
    id: "wfr-01234-jklmn-56789",
    status: "COMPLETED",
    startTime: getTimeAgo(20000), // ~2 weeks ago (within 30d range)
  },
];

// Mock data for scheduled workflow runs
export interface ScheduledWorkflowRun {
  id: string;
  cronExpression: string;
  nextRunTime: string;
}

export const mockScheduledWorkflowRuns: ScheduledWorkflowRun[] = [
  // {
  //   id: "swr-12345-abcde-67890",
  //   cronExpression: "0 0 * * *",
  //   nextRunTime: getTimeAgo(-60), // 1 hour in the future
  // },
  // {
  //   id: "swr-23456-bcdef-78901",
  //   cronExpression: "0 */2 * * *",
  //   nextRunTime: getTimeAgo(-30), // 30 minutes in the future
  // },
  // {
  //   id: "swr-34567-cdefg-89012",
  //   cronExpression: "0 12 * * 1-5",
  //   nextRunTime: getTimeAgo(-120), // 2 hours in the future
  // },
  // {
  //   id: "swr-45678-defgh-90123",
  //   cronExpression: "30 9 * * *",
  //   nextRunTime: getTimeAgo(2), // 2 minutes ago
  // },
  // {
  //   id: "swr-56789-efghi-01234",
  //   cronExpression: "0 0 1 * *",
  //   nextRunTime: getTimeAgo(15), // 15 minutes ago
  // },
  // {
  //   id: "swr-67890-fghij-12345",
  //   cronExpression: "*/15 * * * *",
  //   nextRunTime: getTimeAgo(50), // 50 minutes ago
  // },
  // {
  //   id: "swr-78901-ghijk-23456",
  //   cronExpression: "0 18 * * 0",
  //   nextRunTime: getTimeAgo(200), // ~3.3 hours ago
  // },
  // {
  //   id: "swr-89012-hijkl-34567",
  //   cronExpression: "0 3 * * *",
  //   nextRunTime: getTimeAgo(800), // ~13.3 hours ago
  // },
];

// Helper function to build a tree structure from nodes and edges
// key is the name of the node
export function buildNodeTree(nodes: {
  [key: string]: LittlehorseNode;
}): TreeNode[] {
  if (!nodes) return [];
  const result: TreeNode[] = [];

  // Convert each LittlehorseNode to a TreeNode
  Object.entries(nodes).forEach(([nodeId, node]) => {
    // Use type assertion for accessing properties
    const littlehorseNode = node as any;

    let nodeStatus: string | undefined;

    // Try to determine node status from node properties
    if (littlehorseNode.status) {
      nodeStatus = littlehorseNode.status.toString();
    } else if (littlehorseNode.endTime) {
      nodeStatus = "completed";
    } else if (littlehorseNode.arrivalTime && !littlehorseNode.endTime) {
      nodeStatus = "running";
    } else {
      nodeStatus = "pending";
    }

    result.push({
      id: nodeId,
      label: littlehorseNode.displayName || littlehorseNode.name || nodeId,
      type: littlehorseNode.nodeType || littlehorseNode.type || "unknown",
      status: nodeStatus,
      children: [], // All nodes have empty children since we're not nesting
      level: 0, // All nodes are at root level
    });
  });

  return result;
}

export interface TreeNode {
  id: string;
  label: string;
  type?: string;
  status?: string;
  children: TreeNode[];
  level: number;
}
